package com.coffee.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.cache.CacheKeyConstants;
import com.coffee.common.context.UserContext;
import com.coffee.common.dto.MemberStatusDTO;
import com.coffee.common.dto.PageParam;
import com.coffee.common.dto.UpdatePasswordDTO;
import com.coffee.common.util.MinioUtil;
import com.coffee.system.domain.entity.UmsMember;
import com.coffee.system.domain.entity.UmsMemberLevel;
import com.coffee.system.domain.entity.UmsRole;
import com.coffee.system.domain.entity.UmsUserRole;
import com.coffee.system.domain.vo.MemberVO;
import com.coffee.common.dto.UmsMemberUpdateDTO;
import com.coffee.system.domain.entity.SmsCouponHistory;
import com.coffee.system.mapper.SmsCouponHistoryMapper;
import com.coffee.system.mapper.UmsMemberLevelMapper;
import com.coffee.system.mapper.UmsMemberMapper;
import com.coffee.system.mapper.UmsUserRoleMapper;
import com.coffee.system.service.OrderService;
import com.coffee.system.service.UmsMemberService;
import com.coffee.system.domain.entity.OmsOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UmsMemberServiceImpl extends ServiceImpl<UmsMemberMapper, UmsMember> implements UmsMemberService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UmsUserRoleMapper userRoleMapper;

    @Autowired
    private UmsMemberLevelMapper memberLevelMapper;

    @Autowired
    private SmsCouponHistoryMapper couponHistoryMapper;

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    @Lazy
    private OrderService orderService;

    /**
     * 注入自身代理对象，用于解决内部调用 @CacheEvict 失效的问题
     * 必须加 @Lazy 防止循环依赖
     */
    @Autowired
    @Lazy
    private UmsMemberService self;


    /**
     * 重写 getById，增加缓存
     * 这是核心：所有内部调用 getById 的地方（如 getUserInfo）都会自动走缓存
     */
    @Override
    @Cacheable(value = CacheKeyConstants.User.INFO, key = "#id")
    public UmsMember getById(Serializable id) {
        // log.info("【UmsMemberService】查询用户信息，未命中缓存，正在查库... userId={}", id);
        return super.getById(id);
    }

    /**
     * 重写 updateById，清理缓存
     * 确保任何通过 ID 更新用户信息的操作都能让缓存失效
     */
    @Override
    @CacheEvict(value = CacheKeyConstants.User.INFO, key = "#entity.id")
    public boolean updateById(UmsMember entity) {
        return super.updateById(entity);
    }

    /**
     * 重写 removeById，清理缓存
     */
    @Override
    @CacheEvict(value = CacheKeyConstants.User.INFO, key = "#id")
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    @Override
    public Page<UmsMember> getList(PageParam pageParam, String phone) {
        Page<UmsMember> memberPage = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<UmsMember> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(phone)) {
            wrapper.like(UmsMember::getPhone, phone);
        }
        wrapper.orderByDesc(UmsMember::getCreateTime);
        return this.page(memberPage, wrapper);
    }

    @Override
    public Page<MemberVO> getListWithRoles(PageParam pageParam, String phone) {
        // 1. 先查询会员列表
        Page<UmsMember> memberPage = getList(pageParam, phone);

        // 2. 转换为 MemberVO 并填充角色信息
        Page<MemberVO> voPage = new Page<>(memberPage.getCurrent(), memberPage.getSize(), memberPage.getTotal());
        List<MemberVO> voList = memberPage.getRecords().stream().map(member -> {
            MemberVO vo = new MemberVO();
            BeanUtil.copyProperties(member, vo);
            // 查询用户角色
            List<UmsRole> roles = userRoleMapper.selectRolesByUserId(member.getId());
            vo.setRoles(roles);
            // 查询会员等级
            if (member.getLevelId() != null) {
                UmsMemberLevel level = memberLevelMapper.selectById(member.getLevelId());
                vo.setLevel(level);
            }
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public UmsMember getByPhone(String phone) {
        return this.getOne(new QueryWrapper<UmsMember>().eq("phone", phone));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheKeyConstants.User.INFO, key = "#dto.id")
    public boolean updateStatus(MemberStatusDTO dto) {
        return this.update(new LambdaUpdateWrapper<UmsMember>()
                .eq(UmsMember::getId, dto.getId())
                .set(UmsMember::getStatus, dto.getStatus()));
    }

    @Override
    public UmsMember getUserInfo() {
        Long userId = UserContext.getUserId();
        if (Objects.isNull(userId)) {
            // 这里其实也可以不抛异常，返回null让Controller处理，看你业务风格
            throw new RuntimeException("用户未登录");
        }
        UmsMember member = self.getById(userId);

        // 脱敏处理：不返回密码
        if (member != null) {
            member.setPassword(null);
        }
        return member;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserInfo(UmsMemberUpdateDTO param) {
        Long userId = UserContext.getUserId();
        if (Objects.isNull(userId)) {
            throw new RuntimeException("用户未登录");
        }

        // 1. 查出数据库里的原始数据
        UmsMember exist = this.getById(userId);
        if (exist == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 【核心修复】使用 Hutool 进行拷贝，并配置忽略 null 值
        // 这样前端只传 nickname 时，不会把 avatar 覆盖为空
        BeanUtil.copyProperties(param, exist, CopyOptions.create().setIgnoreNullValue(true));

        return self.updateById(exist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(UpdatePasswordDTO param) {
        Long userId = UserContext.getUserId();
        if (Objects.isNull(userId)) {
            throw new RuntimeException("用户未登录");
        }
        UmsMember member = this.getById(userId);
        if (Objects.isNull(member)) {
            throw new RuntimeException("用户不存在");
        }
        if (StrUtil.isBlank(param.getOldPassword())) {
            throw new RuntimeException("旧密码为空");
        }
        if (Objects.equals(member.getPassword(), param.getOldPassword())) {
            throw new RuntimeException("新旧密码一致");
        }
        //1.校验：检查旧密码是否正确 (passwordEncoder.matches)。
        if (!passwordEncoder.matches(param.getOldPassword(), member.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        //2.加密：使用 passwordEncoder.encode 加密新密码。
        member.setPassword(passwordEncoder.encode(param.getNewPassword()));
        //3.更新：写入数据库。
        boolean success = self.update(new LambdaUpdateWrapper<UmsMember>()
                .eq(UmsMember::getId, userId)
                .set(UmsMember::getPassword, member.getPassword()));
        if (success){
            self.clearUserCache(userId);
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean logoff() {
        Long userId = UserContext.getUserId();
        if (Objects.isNull(userId)) {
            throw new RuntimeException("用户未登录");
        }
        // 使用 UpdateWrapper 只需要操作一次数据库，比先查再改更高效
        boolean success = self.update(new LambdaUpdateWrapper<UmsMember>()
                .eq(UmsMember::getId, userId)
                .set(UmsMember::getStatus, 0));
        if (success) {
            self.clearUserCache(userId);
        }
        return success; // 0 表示禁用/注销
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean allocRoles(Long userId, List<Long> roleIds) {
        // 1. 删除用户原有的所有角色
        LambdaQueryWrapper<UmsUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UmsUserRole::getUserId, userId);
        userRoleMapper.delete(wrapper);

        // 2. 批量添加新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UmsUserRole userRole = new UmsUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
        return true;
    }

    /**
     * 上传头像
     *
     * @param file
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadAvatar(MultipartFile file) {
        Long userId = UserContext.getUserId();
        if (Objects.isNull(userId)) {
            throw new RuntimeException("用户未登录");
        }

        // 1. 查出数据库里的原始数据
        UmsMember exist = self.getById(userId);
        if (exist == null) {
            throw new RuntimeException("用户不存在");
        }
        String url = minioUtil.uploadAvatar(file);
        boolean success = self.update(new LambdaUpdateWrapper<UmsMember>()
                .eq(UmsMember::getId, userId)
                .set(UmsMember::getAvatar, url));
        if (success) {
            self.clearUserCache(userId);
        }
    }

    @Override
    public Map<String, Object> getProfileStatistics() {
        Long userId = UserContext.getUserId();
        if (Objects.isNull(userId)) {
            throw new RuntimeException("用户未登录");
        }

        // 1. 获取用户信息
        UmsMember member = self.getById(userId);
        if (member == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 获取会员等级信息
        UmsMemberLevel level = null;
        if (member.getLevelId() != null) {
            level = memberLevelMapper.selectById(member.getLevelId());
        }
        // 如果用户没有等级ID，查询最低等级的会员等级作为默认值
        if (level == null) {
            List<UmsMemberLevel> defaultLevels = memberLevelMapper.selectList(
                    new LambdaQueryWrapper<UmsMemberLevel>()
                            .eq(UmsMemberLevel::getStatus, 1)
                            .orderByAsc(UmsMemberLevel::getGrowthPoint)
                            .last("LIMIT 1")
            );
            if (!defaultLevels.isEmpty()) {
                level = defaultLevels.get(0);
            }
        }

        // 3. 统计各状态订单数量
        Map<String, Long> orderCounts = new HashMap<>();
        orderCounts.put("pendingPayment", orderService.count(new LambdaQueryWrapper<OmsOrder>()
                .eq(OmsOrder::getMemberId, userId)
                .eq(OmsOrder::getStatus, 0))); // 待付款
        orderCounts.put("making", orderService.count(new LambdaQueryWrapper<OmsOrder>()
                .eq(OmsOrder::getMemberId, userId)
                .eq(OmsOrder::getStatus, 2))); // 制作中（仅状态2）
        orderCounts.put("pendingPickup", orderService.count(new LambdaQueryWrapper<OmsOrder>()
                .eq(OmsOrder::getMemberId, userId)
                .eq(OmsOrder::getStatus, 3))); // 待取餐
        orderCounts.put("completed", orderService.count(new LambdaQueryWrapper<OmsOrder>()
                .eq(OmsOrder::getMemberId, userId)
                .eq(OmsOrder::getStatus, 4))); // 已完成

        // 4. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("integration", member.getIntegration() != null ? member.getIntegration() : 0); // 积分

        // 查询当前用户可用的优惠券数量（useStatus=0 未使用）
        Long couponCount = couponHistoryMapper.selectCount(new LambdaQueryWrapper<SmsCouponHistory>()
                .eq(SmsCouponHistory::getMemberId, userId)
                .eq(SmsCouponHistory::getUseStatus, 0));
        result.put("couponCount", couponCount); // 优惠券数量

        result.put("growth", member.getGrowth() != null ? member.getGrowth() : 0); // 成长值
        result.put("levelId", member.getLevelId()); // 会员等级ID
        result.put("levelName", level != null ? level.getName() : "普通会员"); // 会员等级名称
        result.put("orderCounts", orderCounts); // 订单统计

        // 5. 计算距离下一等级的成长值
        if (level != null && member.getGrowth() != null) {
            // 查询下一等级
            List<UmsMemberLevel> nextLevels = memberLevelMapper.selectList(
                    new LambdaQueryWrapper<UmsMemberLevel>()
                            .gt(UmsMemberLevel::getGrowthPoint, member.getGrowth())
                            .eq(UmsMemberLevel::getStatus, 1)
                            .orderByAsc(UmsMemberLevel::getGrowthPoint)
                            .last("LIMIT 1")
            );
            if (!nextLevels.isEmpty()) {
                UmsMemberLevel nextLevel = nextLevels.get(0);
                int needGrowth = nextLevel.getGrowthPoint() - member.getGrowth();
                result.put("nextLevelName", nextLevel.getName());
                result.put("needGrowth", needGrowth);
            } else {
                result.put("nextLevelName", null);
                result.put("needGrowth", 0);
            }
        } else {
            result.put("nextLevelName", null);
            result.put("needGrowth", 0);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addGrowth(Long userId, Integer growth) {
        if (userId == null || growth == null || growth <= 0) {
            return false;
        }
        UmsMember member = self.getById(userId);
        if (member == null) {
            return false;
        }
        int currentGrowth = member.getGrowth() != null ? member.getGrowth() : 0;
        int newGrowth = currentGrowth + growth;
        boolean success = self.update(new LambdaUpdateWrapper<UmsMember>()
                .eq(UmsMember::getId, userId)
                .set(UmsMember::getGrowth, newGrowth));
        if (success) {
            self.clearUserCache(userId);
        }

        // 成长值增加后，检查是否需要升级
        checkAndUpdateLevel(userId);
        return true;
    }

    /**
     * 增加积分 (并清理缓存)
     *
     * @param userId
     * @param integration
     */
    @Override
    public boolean addIntegration(Long userId, Integer integration) {
        if (userId == null || integration == null || integration == 0) {
            return false;
        }

        // 1. 简单做法：直接在数据库层做加法 (原子性更好，防止并发覆盖)
        // update ums_member set integration = integration + ? where id = ?
        boolean success = self.update(new LambdaUpdateWrapper<UmsMember>()
                .eq(UmsMember::getId, userId)
                .setSql("integration = integration + " + integration));
        // 2. 【核心】清理缓存
        if (success) {
            self.clearUserCache(userId);
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkAndUpdateLevel(Long userId) {
        UmsMember member = self.getById(userId);
        if (member == null || member.getGrowth() == null) {
            return false;
        }

        int currentGrowth = member.getGrowth();

        // 查询所有启用的会员等级，按成长值门槛降序排列
        List<UmsMemberLevel> levels = memberLevelMapper.selectList(
                new LambdaQueryWrapper<UmsMemberLevel>()
                        .eq(UmsMemberLevel::getStatus, 1)
                        .orderByDesc(UmsMemberLevel::getGrowthPoint)
        );

        // 找到用户当前成长值能达到的最高等级
        UmsMemberLevel targetLevel = null;
        for (UmsMemberLevel level : levels) {
            if (currentGrowth >= level.getGrowthPoint()) {
                targetLevel = level;
                break;
            }
        }

        // 如果找到了新等级，且与当前等级不同，则更新
        if (targetLevel != null &&
                (member.getLevelId() == null || !targetLevel.getId().equals(member.getLevelId()))) {
            boolean Success = self.update(new LambdaUpdateWrapper<UmsMember>()
                    .eq(UmsMember::getId, userId)
                    .set(UmsMember::getLevelId, targetLevel.getId()));
            if (Success) {
                self.clearUserCache(userId);
            }
            return true; // 升级了
        }

        return false; // 没有升级
    }

    /**
     * 专门用于清理缓存的空方法
     */
    @CacheEvict(value = CacheKeyConstants.User.INFO, key = "#userId")
    public void clearUserCache(Long userId) {
        // 什么都不用做，注解会帮你删缓存
        log.info("触发缓存清理: {}", userId);
    }
}
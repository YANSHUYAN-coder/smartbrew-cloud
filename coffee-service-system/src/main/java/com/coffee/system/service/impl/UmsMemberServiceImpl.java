package com.coffee.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.coffee.system.mapper.UmsMemberLevelMapper;
import com.coffee.system.mapper.UmsMemberMapper;
import com.coffee.system.mapper.UmsUserRoleMapper;
import com.coffee.system.service.OrderService;
import com.coffee.system.service.UmsMemberService;
import com.coffee.system.domain.entity.OmsOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UmsMemberServiceImpl extends ServiceImpl<UmsMemberMapper, UmsMember> implements UmsMemberService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UmsUserRoleMapper userRoleMapper;

    @Autowired
    private UmsMemberLevelMapper memberLevelMapper;

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    @Lazy
    private OrderService orderService;

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
        UmsMember member = this.getById(userId);

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

        return this.updateById(exist);
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
        return this.update(new LambdaUpdateWrapper<UmsMember>()
                .eq(UmsMember::getId, userId)
                .set(UmsMember::getPassword, member.getPassword()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean logoff() {
        Long userId = UserContext.getUserId();
        if (Objects.isNull(userId)) {
            throw new RuntimeException("用户未登录");
        }
        // 使用 UpdateWrapper 只需要操作一次数据库，比先查再改更高效
        return this.update(new LambdaUpdateWrapper<UmsMember>()
                .eq(UmsMember::getId, userId)
                .set(UmsMember::getStatus, 0)); // 0 表示禁用/注销
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
        UmsMember exist = this.getById(userId);
        if (exist == null) {
            throw new RuntimeException("用户不存在");
        }
        String url = minioUtil.uploadAvatar(file);
        this.update(new LambdaUpdateWrapper<UmsMember>()
                .eq(UmsMember::getId, userId)
                .set(UmsMember::getAvatar, url));
    }

    @Override
    public Map<String, Object> getProfileStatistics() {
        Long userId = UserContext.getUserId();
        if (Objects.isNull(userId)) {
            throw new RuntimeException("用户未登录");
        }

        // 1. 获取用户信息
        UmsMember member = this.getById(userId);
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
        result.put("couponCount", 0); // 优惠券数量（暂时返回0，后续可对接优惠券系统）
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
        UmsMember member = this.getById(userId);
        if (member == null) {
            return false;
        }
        int currentGrowth = member.getGrowth() != null ? member.getGrowth() : 0;
        int newGrowth = currentGrowth + growth;
        this.update(new LambdaUpdateWrapper<UmsMember>()
                .eq(UmsMember::getId, userId)
                .set(UmsMember::getGrowth, newGrowth));
        
        // 成长值增加后，检查是否需要升级
        checkAndUpdateLevel(userId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkAndUpdateLevel(Long userId) {
        UmsMember member = this.getById(userId);
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
            this.update(new LambdaUpdateWrapper<UmsMember>()
                    .eq(UmsMember::getId, userId)
                    .set(UmsMember::getLevelId, targetLevel.getId()));
            return true; // 升级了
        }
        
        return false; // 没有升级
    }
}
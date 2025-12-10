package com.coffee.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.context.UserContext;
import com.coffee.common.dto.MemberStatusDTO;
import com.coffee.common.dto.UpdatePasswordDTO;
import com.coffee.system.domain.UmsMember;
import com.coffee.system.domain.dto.UmsMemberUpdateDTO;
import com.coffee.system.mapper.UmsMemberMapper;
import com.coffee.system.service.UmsMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class UmsMemberServiceImpl extends ServiceImpl<UmsMemberMapper, UmsMember> implements UmsMemberService {
    @Autowired
    private PasswordEncoder passwordEncoder;

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
        if(StrUtil.isBlank(param.getOldPassword())){
            throw new RuntimeException("旧密码为空");
        }
        if (Objects.equals(member.getPassword(), param.getOldPassword())){
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
}
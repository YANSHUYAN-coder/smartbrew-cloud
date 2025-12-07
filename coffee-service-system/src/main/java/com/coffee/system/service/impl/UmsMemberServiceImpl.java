package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.system.domain.UmsMember;
import com.coffee.system.mapper.UmsMemberMapper;
import com.coffee.system.service.UmsMemberService;
import org.springframework.stereotype.Service;

@Service
public class UmsMemberServiceImpl extends ServiceImpl<UmsMemberMapper, UmsMember> implements UmsMemberService {
    
    @Override
    public UmsMember getByPhone(String phone) {
        // 使用MyBatis-Plus的条件构造器查询
        return this.getOne(new QueryWrapper<UmsMember>().eq("phone", phone));
    }
    
    // 用户模块业务实现
}
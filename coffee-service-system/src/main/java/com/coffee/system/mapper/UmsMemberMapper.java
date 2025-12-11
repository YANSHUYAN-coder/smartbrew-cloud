package com.coffee.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffee.system.domain.entity.UmsMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UmsMemberMapper extends BaseMapper<UmsMember> {
    // MyBatis-Plus 已内置 CRUD，无需手写 SQL
}
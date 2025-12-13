package com.coffee.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffee.system.domain.entity.UmsMemberLevel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级 Mapper
 */
@Mapper
public interface UmsMemberLevelMapper extends BaseMapper<UmsMemberLevel> {
    // MyBatis-Plus 已内置 CRUD，无需手写 SQL
}


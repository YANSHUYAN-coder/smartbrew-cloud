package com.coffee.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffee.system.domain.entity.UmsMemberSignIn;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户签到记录 Mapper
 * 使用MyBatis-Plus的BaseMapper，简单查询通过Service层使用LambdaQueryWrapper实现
 */
@Mapper
public interface UmsMemberSignInMapper extends BaseMapper<UmsMemberSignIn> {
}


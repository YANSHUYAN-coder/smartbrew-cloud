package com.coffee.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffee.system.domain.UmsPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UmsPermissionMapper extends BaseMapper<UmsPermission> {
    List<String> selectPermsByUserId(@Param("userId") Long userId);
}
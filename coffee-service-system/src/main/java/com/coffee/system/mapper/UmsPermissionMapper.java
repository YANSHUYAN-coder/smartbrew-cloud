package com.coffee.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffee.system.domain.entity.UmsPermission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UmsPermissionMapper extends BaseMapper<UmsPermission> {
    List<String>  selectPermsByUserId(Long userId);
}

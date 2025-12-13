package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.UmsMember;
import com.coffee.system.domain.entity.UmsPermission;
import com.coffee.system.mapper.UmsPermissionMapper;
import com.coffee.system.service.UmsPermissionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UmsPermissionServiceImpl extends ServiceImpl<UmsPermissionMapper, UmsPermission> implements UmsPermissionService {
    @Override
    public Page<UmsPermission> getList(PageParam pageParam) {
        Page<UmsPermission> page = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<UmsPermission> wrapper = new LambdaQueryWrapper<UmsPermission>().orderByDesc(UmsPermission::getCreateTime);
        return this.page(page, wrapper);
    }
}


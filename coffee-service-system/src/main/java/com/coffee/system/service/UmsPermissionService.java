package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.UmsPermission;

import java.util.List;

public interface UmsPermissionService extends IService<UmsPermission> {
    Page<UmsPermission> getList(PageParam pageParam);
}


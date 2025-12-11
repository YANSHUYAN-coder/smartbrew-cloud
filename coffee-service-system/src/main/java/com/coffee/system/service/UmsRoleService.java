package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.UmsRole;

import java.util.List;
import java.util.Map;

public interface UmsRoleService extends IService<UmsRole> {
    /**
     * 获取所有角色
     */
    Page<UmsRole> getAllRoles(PageParam pageParam, String keyword);

    /**
     * 分配菜单(权限)
     */
    boolean allocMenu(Map<String, Object> params);

    /**
     * 获取角色拥有的菜单ID集合
     */
    List<Long> getMenuIds(Long roleId);
}


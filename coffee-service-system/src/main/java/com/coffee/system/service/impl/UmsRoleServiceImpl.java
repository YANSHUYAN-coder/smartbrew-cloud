package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.UmsRole;
import com.coffee.system.domain.entity.UmsRolePermission;
import com.coffee.system.mapper.UmsRoleMapper;
import com.coffee.system.mapper.UmsRolePermissionMapper;
import com.coffee.system.service.UmsRoleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UmsRoleServiceImpl extends ServiceImpl<UmsRoleMapper, UmsRole> implements UmsRoleService {

    private final UmsRolePermissionMapper rolePermissionMapper;

    /**
     * 获取所有角色
     *
     */
    @Override
    public Page<UmsRole> getAllRoles(PageParam pageParam, String keyword) {
        Page<UmsRole> page = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<UmsRole> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null) {
            wrapper.like(UmsRole::getName, keyword);
        }
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean allocMenu(Map<String, Object> params) {
        Long roleId = Long.valueOf(params.get("roleId").toString());
        List<Long> menuIds = (List<Long>) params.get("menuIds");
        // 1. 删除原有关系
        LambdaQueryWrapper<UmsRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UmsRolePermission::getRoleId, roleId);
        rolePermissionMapper.delete(wrapper);

        // 2. 批量添加新关系
        for (Long menuId : menuIds) {
            UmsRolePermission relation = new UmsRolePermission();
            relation.setRoleId(roleId);
            relation.setPermissionId(menuId);
            rolePermissionMapper.insert(relation);
        }
        return true;
    }

    @Override
    public List<Long> getMenuIds(Long roleId) {
        LambdaQueryWrapper<UmsRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UmsRolePermission::getRoleId, roleId);
        List<UmsRolePermission> list = rolePermissionMapper.selectList(wrapper);
        return list.stream().map(UmsRolePermission::getPermissionId).collect(Collectors.toList());
    }
}


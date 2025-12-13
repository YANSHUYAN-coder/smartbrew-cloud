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
        // 处理 roleId：前端传递的可能是 Integer 类型，需要转换为 Long
        Object roleIdObj = params.get("roleId");
        Long roleId;
        if (roleIdObj instanceof Long) {
            roleId = (Long) roleIdObj;
        } else if (roleIdObj instanceof Integer) {
            roleId = ((Integer) roleIdObj).longValue();
        } else {
            roleId = Long.valueOf(roleIdObj.toString());
        }
        
        // 处理 menuIds：前端传递的可能是 Integer 类型，需要转换为 Long
        @SuppressWarnings("unchecked")
        List<Object> menuIdsObj = (List<Object>) params.get("menuIds");
        List<Long> menuIds = menuIdsObj.stream()
                .map(id -> {
                    if (id instanceof Long) {
                        return (Long) id;
                    } else if (id instanceof Integer) {
                        return ((Integer) id).longValue();
                    } else {
                        return Long.valueOf(id.toString());
                    }
                })
                .collect(Collectors.toList());
        
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


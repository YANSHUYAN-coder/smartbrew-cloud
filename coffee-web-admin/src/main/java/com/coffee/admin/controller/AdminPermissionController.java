package com.coffee.admin.controller;

import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.UmsPermission;
import com.coffee.system.service.UmsPermissionService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/permission")
@AllArgsConstructor
public class AdminPermissionController {

    private final UmsPermissionService permissionService;

    @GetMapping("/list")
//    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<UmsPermission>> list() {
        // 简单返回所有权限列表，前端自行构建树
        return Result.success(permissionService.list());
    }

    @PostMapping("/create")
//    @PreAuthorize("hasAuthority('system:menu:add')")
    public Result<String> create(@RequestBody UmsPermission permission) {
        boolean success = permissionService.save(permission);
        return success ? Result.success("创建成功") : Result.failed("创建失败");
    }

    @PostMapping("/update")
//    @PreAuthorize("hasAuthority('system:menu:update')")
    public Result<String> update(@RequestBody UmsPermission permission) {
        boolean success = permissionService.updateById(permission);
        return success ? Result.success("更新成功") : Result.failed("更新失败");
    }

    @DeleteMapping("/delete/{id}")
//    @PreAuthorize("hasAuthority('system:menu:delete')")
    public Result<String> delete(@PathVariable("id") Long id) {
        boolean success = permissionService.removeById(id);
        return success ? Result.success("删除成功") : Result.failed("删除失败");
    }
}


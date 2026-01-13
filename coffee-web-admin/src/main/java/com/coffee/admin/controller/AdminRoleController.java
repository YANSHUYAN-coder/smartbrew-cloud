package com.coffee.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.common.dto.PageParam;
import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.UmsRole;
import com.coffee.system.service.UmsRoleService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/role")
@AllArgsConstructor
public class AdminRoleController {

    private final UmsRoleService roleService;

    @GetMapping("/list")
//    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<Page<UmsRole>> list(@ModelAttribute PageParam pageParam,
                                      @RequestParam(name = "keyword",required = false) String keyword) {
        return Result.success(roleService.getAllRoles(pageParam, keyword));
    }

    @PostMapping("/create")
//    @PreAuthorize("hasAuthority('system:role:add')")
    public Result<String> create(@RequestBody UmsRole role) {
        boolean success = roleService.save(role);
        return success ? Result.success("创建成功") : Result.failed("创建失败");
    }

    @PostMapping("/update")
//    @PreAuthorize("hasAuthority('system:role:update')")
    public Result<String> update(@RequestBody UmsRole role) {
        boolean success = roleService.updateById(role);
        return success ? Result.success("更新成功") : Result.failed("更新失败");
    }

    @DeleteMapping("/delete/{id}")
//    @PreAuthorize("hasAuthority('system:role:delete')")
    public Result<String> delete(@PathVariable("id") Long id) {
        boolean success = roleService.removeById(id);
        return success ? Result.success("删除成功") : Result.failed("删除失败");
    }

    @GetMapping("/menus/{roleId}")
//    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<List<Long>> getRoleMenus(@PathVariable("roleId") Long roleId) {
        return Result.success(roleService.getMenuIds(roleId));
    }

    @PostMapping("/allocMenu")
//    @PreAuthorize("hasAuthority('system:role:alloc')")
    public Result<String> allocMenu(@RequestBody Map<String, Object> params) {
        boolean success = roleService.allocMenu(params);
        return success ? Result.success("分配成功") : Result.failed("分配失败");
    }
}

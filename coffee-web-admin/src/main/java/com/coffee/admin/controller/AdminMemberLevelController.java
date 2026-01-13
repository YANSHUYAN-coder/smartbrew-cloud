package com.coffee.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.common.dto.PageParam;
import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.UmsMemberLevel;
import com.coffee.system.service.UmsMemberLevelService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 会员等级管理控制器
 */
@RestController
@RequestMapping("/admin/member-level")
@AllArgsConstructor
public class AdminMemberLevelController {

    private final UmsMemberLevelService memberLevelService;

    /**
     * 获取会员等级列表（分页）
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ums:member-level:list')")
    public Result<Page<UmsMemberLevel>> list(@ModelAttribute PageParam pageParam,
                                             @RequestParam(name = "keyword",required = false) String keyword) {
        Page<UmsMemberLevel> page = memberLevelService.getList(pageParam, keyword);
        return Result.success(page);
    }

    /**
     * 获取会员等级详情
     */
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAuthority('ums:member-level:detail')")
    public Result<UmsMemberLevel> detail(@PathVariable("id") Long id) {
        UmsMemberLevel level = memberLevelService.getById(id);
        if (level == null) {
            return Result.failed("等级不存在");
        }
        return Result.success(level);
    }

    /**
     * 新增会员等级
     */
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ums:member-level:add')")
    public Result<String> add(@RequestBody UmsMemberLevel level) {
        boolean success = memberLevelService.save(level);
        return success ? Result.success("添加成功") : Result.failed("添加失败");
    }

    /**
     * 更新会员等级
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('ums:member-level:update')")
    public Result<String> update(@RequestBody UmsMemberLevel level) {
        boolean success = memberLevelService.updateById(level);
        return success ? Result.success("更新成功") : Result.failed("更新失败");
    }

    /**
     * 删除会员等级
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ums:member-level:delete')")
    public Result<String> delete(@PathVariable("id") Long id) {
        boolean success = memberLevelService.removeById(id);
        return success ? Result.success("删除成功") : Result.failed("删除失败");
    }

    /**
     * 修改会员等级状态
     */
    @PostMapping("/updateStatus")
    @PreAuthorize("hasAuthority('ums:member-level:update')")
    public Result<String> updateStatus(@RequestBody UmsMemberLevel level) {
        UmsMemberLevel existLevel = memberLevelService.getById(level.getId());
        if (existLevel == null) {
            return Result.failed("等级不存在");
        }
        existLevel.setStatus(level.getStatus());
        boolean success = memberLevelService.updateById(existLevel);
        return success ? Result.success("状态更新成功") : Result.failed("状态更新失败");
    }
}


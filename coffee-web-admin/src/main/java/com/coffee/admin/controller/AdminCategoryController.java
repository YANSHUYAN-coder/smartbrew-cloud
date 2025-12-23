package com.coffee.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.common.dto.PageParam;
import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.Category;
import com.coffee.system.service.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/category")
@AllArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * 获取分类列表 - 分页查询
     */
    @GetMapping("/list")
    // @PreAuthorize("hasAuthority('pms:category:list')")
    public Result<Page<Category>> list(@ModelAttribute PageParam pageParam) {
        Page<Category> page = categoryService.getList(pageParam);
        return Result.success(page);
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/detail/{id}")
    // @PreAuthorize("hasAuthority('pms:category:detail')")
    public Result<Category> detail(@PathVariable("id") Long id) {
        Category category = categoryService.getById(id);
        return Result.success(category);
    }

    /**
     * 添加分类
     */
    @PostMapping("/add")
    // @PreAuthorize("hasAuthority('pms:category:add')")
    public Result<String> add(@RequestBody Category category) {
        boolean success = categoryService.save(category);
        return success ? Result.success("添加成功") : Result.failed("添加失败");
    }

    /**
     * 修改分类
     */
    @PostMapping("/update")
    // @PreAuthorize("hasAuthority('pms:category:update')")
    public Result<String> update(@RequestBody Category category) {
        boolean success = categoryService.updateById(category);
        return success ? Result.success("修改成功") : Result.failed("修改失败");
    }

    /**
     * 修改启用/禁用状态
     */
    @PostMapping("/updateStatus")
    // @PreAuthorize("hasAuthority('pms:category:status')")
    public Result<String> updateStatus(@RequestBody Category category) {
        // 只需要传 id 和 status
        boolean success = categoryService.updateById(category);
        return success ? Result.success("状态更新成功") : Result.failed("状态更新失败");
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/delete/{id}")
    // @PreAuthorize("hasAuthority('pms:category:delete')")
    public Result<String> delete(@PathVariable("id") Long id) {
        // 检查是否有商品使用该分类
        // TODO: 可以添加业务校验，如果有商品使用该分类，不允许删除
        boolean success = categoryService.removeById(id);
        return success ? Result.success("删除成功") : Result.failed("删除失败");
    }
}


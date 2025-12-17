package com.coffee.app.controller;

import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.Category;
import com.coffee.system.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C端获取分类列表
 */
@RestController
@RequestMapping("/app/categories")
@Tag(name = "C端-商品分类接口", description = "提供前端点单页使用的启用分类列表")
public class AppCategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    @Operation(summary = "获取启用的商品分类列表", description = "返回状态为启用的分类，用于点单页左侧分类栏展示")
    public Result<List<Category>> list() {
        return Result.success(categoryService.getEnabledCategories());
    }
}

package com.coffee.app.controller;

import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.Category;
import com.coffee.system.service.CategoryService;
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
public class AppCategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.getEnabledCategories());
    }
}

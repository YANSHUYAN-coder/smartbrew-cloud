package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.system.domain.entity.Category;
import com.coffee.system.mapper.CategoryMapper;
import com.coffee.system.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> getEnabledCategories() {
        return this.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByDesc(Category::getSort));
    }
}


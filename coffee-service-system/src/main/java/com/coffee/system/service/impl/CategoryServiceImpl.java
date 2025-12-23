package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.dto.PageParam;
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
    
    @Override
    public Page<Category> getList(PageParam pageParam) {
        Page<Category> categoryPage = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Category::getSort)
               .orderByDesc(Category::getCreateTime);
        return this.page(categoryPage, wrapper);
    }
}


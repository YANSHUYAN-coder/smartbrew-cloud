package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {
    /**
     * 获取所有启用的分类列表（按排序权重降序）
     */
    List<Category> getEnabledCategories();
    
    /**
     * 分页查询分类列表（管理端使用）
     */
    Page<Category> getList(PageParam pageParam);
}


package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.cache.CacheKeyConstants;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.Category;
import com.coffee.system.mapper.CategoryMapper;
import com.coffee.system.service.CategoryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    /**
     * 获取启用的分类列表（缓存）
     * 缓存原因：查询频率极高，数据变化少
     */
    @Cacheable(value = CacheKeyConstants.Category.ENABLED, key = "'all'")
    @Override
    public List<Category> getEnabledCategories() {
        return this.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByDesc(Category::getSort));
    }
    
    /**
     * 分页查询分类列表（管理端使用）
     * 注意：此方法用于管理端，查询频率相对较低，参数组合多（page, pageSize），
     * 缓存命中率可能不高，可根据实际情况决定是否启用缓存
     * 
     * 如果启用缓存，建议使用 KeyGenerator 处理分页参数
     */
    @Override
    public Page<Category> getList(PageParam pageParam) {
        Page<Category> categoryPage = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Category::getSort)
               .orderByDesc(Category::getCreateTime);
        return this.page(categoryPage, wrapper);
    }
    
    /**
     * 更新分类时清除缓存
     */
    @CacheEvict(value = CacheKeyConstants.Category.ENABLED, allEntries = true)
    @Override
    public boolean updateById(Category entity) {
        return super.updateById(entity);
    }
    
    /**
     * 新增分类时清除缓存
     */
    @CacheEvict(value = CacheKeyConstants.Category.ENABLED, allEntries = true)
    @Override
    public boolean save(Category entity) {
        return super.save(entity);
    }
    
    /**
     * 删除分类时清除缓存
     */
    @CacheEvict(value = CacheKeyConstants.Category.ENABLED, allEntries = true)
    @Override
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }
}


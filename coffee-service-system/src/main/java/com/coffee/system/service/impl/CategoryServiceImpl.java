package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.cache.CacheKeyConstants;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.Category;
import com.coffee.system.mapper.CategoryMapper;
import com.coffee.system.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    /**
     * 获取启用的分类列表（缓存）
     * 缓存原因：查询频率极高，数据变化少
     */
    @Cacheable(value = CacheKeyConstants.Category.ENABLED, key = "'all'")
    @Override
    public List<Category> getEnabledCategories() {
        log.info("【CategoryService】缓存未命中，正在查询数据库获取启用分类列表...");
        return this.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByDesc(Category::getSort));
    }

    /**
     * 分页查询分类列表（管理端使用）
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
     * 更新分类
     * 修复重点：分类名称变更会影响 [商品列表] 和 [商品详情]，因此需要级联清理
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheKeyConstants.Category.ENABLED, allEntries = true), // 清除分类列表缓存
            @CacheEvict(value = CacheKeyConstants.Product.MENU, allEntries = true),     // 清除菜单缓存
            @CacheEvict(value = CacheKeyConstants.Product.LIST, allEntries = true),     // 清除商品列表缓存(包含分类名)
            @CacheEvict(value = CacheKeyConstants.Product.DETAIL, allEntries = true),   // 清除管理端详情缓存
            @CacheEvict(value = CacheKeyConstants.Product.APP_DETAIL, allEntries = true)// 清除C端详情缓存(包含分类名)
    })
    public boolean updateById(Category entity) {
        return super.updateById(entity);
    }

    /**
     * 新增分类
     * 新增分类暂时不会影响已存在的商品详情，所以只需清除分类列表和菜单即可
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheKeyConstants.Category.ENABLED, allEntries = true),
            @CacheEvict(value = CacheKeyConstants.Product.MENU, allEntries = true)
    })
    public boolean save(Category entity) {
        return super.save(entity);
    }

    /**
     * 删除分类
     * 删除分类可能导致商品关联失效，必须级联清理
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheKeyConstants.Category.ENABLED, allEntries = true),
            @CacheEvict(value = CacheKeyConstants.Product.MENU, allEntries = true),
            @CacheEvict(value = CacheKeyConstants.Product.LIST, allEntries = true),
            @CacheEvict(value = CacheKeyConstants.Product.DETAIL, allEntries = true),
            @CacheEvict(value = CacheKeyConstants.Product.APP_DETAIL, allEntries = true)
    })
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }
}
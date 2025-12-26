package com.coffee.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.cache.CacheKeyConstants;
import com.coffee.common.dto.PageParam;
import com.coffee.common.dto.ProductSearchParam;
import com.coffee.common.vo.MenuVO;
import com.coffee.common.vo.ProductDetailVO;
import com.coffee.system.domain.entity.Category;
import com.coffee.system.domain.entity.Product;
import com.coffee.system.domain.entity.SkuStock;
import com.coffee.system.domain.dto.ProductDTO;
import com.coffee.system.mapper.ProductMapper;
import com.coffee.system.service.CategoryService;
import com.coffee.system.service.ProductService;
import com.coffee.system.service.SkuStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = "product")
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private SkuStockService skuStockService;

    @Autowired
    private CategoryService categoryService;

    @Override
    public Page<Product> getList(PageParam pageParam) {
        // 如果 pageParam 是 ProductSearchParam 类型，支持筛选
        ProductSearchParam searchParam = null;
        if (pageParam instanceof ProductSearchParam) {
            searchParam = (ProductSearchParam) pageParam;
        }

        Page<Product> productPage = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（商品名称或描述）
        if (searchParam != null && searchParam.getKeyword() != null && !searchParam.getKeyword().trim().isEmpty()) {
            String keyword = searchParam.getKeyword().trim();
            wrapper.and(w -> w.like(Product::getName, keyword)
                    .or()
                    .like(Product::getDescription, keyword));
        }

        // 状态筛选
        if (searchParam != null && searchParam.getStatus() != null) {
            wrapper.eq(Product::getStatus, searchParam.getStatus());
        }

        // 分类筛选
        if (searchParam != null && searchParam.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, searchParam.getCategoryId());
        }

        wrapper.orderByDesc(Product::getCreateTime);
        Page<Product> result = this.page(productPage, wrapper);

        // 填充分类名称（优化：只查询当前商品列表中涉及到的分类，避免全表扫描）
        if (result != null && CollUtil.isNotEmpty(result.getRecords())) {
            // 收集所有商品涉及到的分类ID（去重）
            List<Long> categoryIds = result.getRecords().stream()
                    .map(Product::getCategoryId)
                    .filter(categoryId -> categoryId != null)
                    .distinct()
                    .collect(Collectors.toList());

            if (!categoryIds.isEmpty()) {
                // 只查询涉及到的分类，构建 ID -> 名称的映射
                List<Category> categories = categoryService.listByIds(categoryIds);
                Map<Long, String> categoryMap = categories.stream()
                        .collect(Collectors.toMap(Category::getId, Category::getName));

                // 为每个商品填充分类名称
                result.getRecords().forEach(product -> {
                    if (product.getCategoryId() != null && categoryMap.containsKey(product.getCategoryId())) {
                        product.setCategory(categoryMap.get(product.getCategoryId()));
                    }
                });
            }
        }

        return result;
    }

    @Override
    public Page<Product> search(ProductSearchParam searchParam) {
        Page<Product> productPage = new Page<>(searchParam.getPage(), searchParam.getPageSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（商品名称或描述）
        if (searchParam.getKeyword() != null && !searchParam.getKeyword().trim().isEmpty()) {
            String keyword = searchParam.getKeyword().trim();
            wrapper.and(w -> w.like(Product::getName, keyword)
                    .or()
                    .like(Product::getDescription, keyword));
        }

        // 状态筛选（管理端使用，C端不传此参数）
        if (searchParam.getStatus() != null) {
            wrapper.eq(Product::getStatus, searchParam.getStatus());
        }

        // 分类筛选（可选）
        if (searchParam.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, searchParam.getCategoryId());
        }

        // 排序：按创建时间倒序
        wrapper.orderByDesc(Product::getCreateTime);

        Page<Product> result = this.page(productPage, wrapper);

        // 填充分类名称（优化：只查询当前商品列表中涉及到的分类，避免全表扫描）
        if (result != null && CollUtil.isNotEmpty(result.getRecords())) {
            // 收集所有商品涉及到的分类ID（去重）
            List<Long> categoryIds = result.getRecords().stream()
                    .map(Product::getCategoryId)
                    .filter(categoryId -> categoryId != null)
                    .distinct()
                    .collect(Collectors.toList());

            if (!categoryIds.isEmpty()) {
                // 只查询涉及到的分类，构建 ID -> 名称的映射
                List<Category> categories = categoryService.listByIds(categoryIds);
                Map<Long, String> categoryMap = categories.stream()
                        .collect(Collectors.toMap(Category::getId, Category::getName));

                // 为每个商品填充分类名称
                result.getRecords().forEach(product -> {
                    if (product.getCategoryId() != null && categoryMap.containsKey(product.getCategoryId())) {
                        product.setCategory(categoryMap.get(product.getCategoryId()));
                    }
                });
            }
        }

        return result;
    }

    /**
     * 获取商品详情（缓存）
     * 缓存原因：查询频率高，数据变化少
     */
    @Cacheable(value = CacheKeyConstants.Product.DETAIL, key = "#id")
    @Override
    public ProductDTO getDetail(Long id) {
        Product product = this.getById(id);
        if (product == null) return null;

        ProductDTO dto = new ProductDTO();
        BeanUtil.copyProperties(product, dto);

        // 查询 SKU
        List<SkuStock> skuList = skuStockService.listByProductId(id);
        dto.setSkuStockList(skuList);

        return dto;
    }


    /**
     * 创建商品（清除菜单和列表缓存）
     */
    @CacheEvict(value = {
            CacheKeyConstants.Product.MENU,
            CacheKeyConstants.Product.LIST
    }, allEntries = true)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(ProductDTO productDTO) {
        // 1. 保存商品基本信息
        Product product = new Product();
        BeanUtil.copyProperties(productDTO, product);
        // 初始化默认值
        product.setId(null);
        product.setSales(0);
        boolean success = this.save(product);

        // 2. 保存 SKU 信息
        Long productId = product.getId(); // 获取回填的主键ID
        saveSkuList(productId, productDTO.getSkuStockList());

        return success;
    }

    /**
     * 更新商品（清除相关缓存）
     */
    @CacheEvict(value = {
            CacheKeyConstants.Product.MENU,
            CacheKeyConstants.Product.DETAIL,
            CacheKeyConstants.Product.APP_DETAIL,
            CacheKeyConstants.Product.LIST
    }, allEntries = true)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(ProductDTO productDTO) {
        // 1. 更新商品基本信息
        Product product = new Product();
        BeanUtil.copyProperties(productDTO, product);
        boolean success = this.updateById(product);

        // 2. 更新 SKU 信息 (简单策略：先删后加)
        // 实际生产中可能需要对比 ID 进行更新，这里为了演示方便直接重置
        Long productId = product.getId();

        // 2.1 删除旧 SKU
        skuStockService.remove(new LambdaQueryWrapper<SkuStock>().eq(SkuStock::getProductId, productId));

        // 2.2 插入新 SKU
        saveSkuList(productId, productDTO.getSkuStockList());

        return success;
    }

    // 辅助方法：批量保存 SKU
    private void saveSkuList(Long productId, List<SkuStock> skuList) {
        if (CollUtil.isEmpty(skuList)) return;

        for (SkuStock sku : skuList) {
            sku.setProductId(productId);
            // 如果没有生成 SKU 编码，可以自动生成一个
            if (sku.getSkuCode() == null) {
                sku.setSkuCode(System.currentTimeMillis() + ""); // 简单模拟
            }
        }
        skuStockService.saveBatch(skuList);
    }

    @Override
    public Map<String, List<Product>> getMenu() {
        // 1. 查询所有上架商品 (status=1)
        List<Product> allProducts = this.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getSales)); // 按销量排序

        // 2. 查询所有启用的分类，构建 ID -> 名称的映射
        List<Category> categories = categoryService.getEnabledCategories();
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        // 3. 按分类名称分组（通过 categoryId 查找分类名称）
        return allProducts.stream()
                .filter(product -> product.getCategoryId() != null && categoryMap.containsKey(product.getCategoryId()))
                .collect(Collectors.groupingBy(product -> categoryMap.get(product.getCategoryId())));
    }

    /**
     * 获取菜单数据（缓存）
     */
    @Cacheable(value = CacheKeyConstants.Product.MENU, key = "'menu'")
    @Override
    public MenuVO getMenuVO() {
        // 1. 查询所有上架商品 (status=1)
        List<Product> allProducts = this.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getSales));

        // 2. 查询所有启用的分类（已缓存）
        List<Category> categories = categoryService.getEnabledCategories();

        // 3. 构建分类列表
        List<MenuVO.CategoryVO> categoryList = categories.stream()
                .map(category -> {
                    MenuVO.CategoryVO categoryVO = new MenuVO.CategoryVO();
                    categoryVO.setId(category.getId());
                    categoryVO.setName(category.getName());
                    return categoryVO;
                })
                .collect(Collectors.toList());

        // 4. 构建商品列表
        List<MenuVO.ProductVO> productList = allProducts.stream()
                .filter(product -> product.getCategoryId() != null)
                .map(product -> convertToVO(product, product.getCategoryId()))
                .collect(Collectors.toList());

        // 5. 组装返回对象
        MenuVO menuVO = new MenuVO();
        menuVO.setCategories(categoryList);
        menuVO.setProducts(productList);
        return menuVO;
    }

    private MenuVO.ProductVO convertToVO(Product product, Long categoryId) {
        MenuVO.ProductVO productVO = new MenuVO.ProductVO();
        BeanUtil.copyProperties(product, productVO);
        productVO.setPicUrl(product.getPicUrl());
        productVO.setDescription(product.getDescription());
        productVO.setCategoryId(categoryId);
        // 传递新品和推荐状态给前端
        productVO.setNewStatus(product.getNewStatus());
        productVO.setRecommendStatus(product.getRecommendStatus());

        if (productVO.getRating() == null) {
            // 生成 4.0 到 5.0 之间的随机评分，保留一位小数
            double randomRating = 3.5 + Math.random();
            productVO.setRating(Math.round(randomRating * 10.0) / 10.0);
        }
        return productVO;
    }

    /**
     * 获取 C端 商品详情（新增缓存）
     * 修复重点：添加 @Cacheable，使用独立的 Key 避免与管理端冲突
     */
    @Override
    @Cacheable(value = CacheKeyConstants.Product.APP_DETAIL, key = "#id")
    public ProductDetailVO getMenuDetail(Long id) {
        // 1. 查商品基本信息
        Product product = this.getById(id);
        if (product == null || product.getStatus() == 0) {
            return null;
        }

        // 2. 查 SKU 库存信息
        List<SkuStock> skuList = skuStockService.listByProductId(id);

        // 3. 查询分类名称
        String categoryName = null;
        if (product.getCategoryId() != null) {
            Category category = categoryService.getById(product.getCategoryId());
            if (category != null) {
                categoryName = category.getName();
            }
        }

        // 4. 组装 VO
        ProductDetailVO vo = new ProductDetailVO();
        BeanUtil.copyProperties(product, vo); // Hutool 属性拷贝
        vo.setCategory(categoryName); // 设置分类名称
        vo.setSkuList(skuList);
        return vo;
    }


    /**
     * 更新状态（清除菜单、列表、详情缓存）
     * 修复：增加清除 APP_DETAIL 缓存
     */
    @CacheEvict(value = {
            CacheKeyConstants.Product.MENU,
            CacheKeyConstants.Product.DETAIL,
            CacheKeyConstants.Product.APP_DETAIL, // 记得清除 C 端详情缓存
            CacheKeyConstants.Product.LIST
    }, allEntries = true)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        // 调用 MP 的 updateById 更新数据库
        return this.updateById(product);
    }

    /**
     * 删除商品（清除相关缓存）
     * 修复：增加清除 APP_DETAIL 缓存
     */
    @CacheEvict(value = {
            CacheKeyConstants.Product.MENU,
            CacheKeyConstants.Product.DETAIL,
            CacheKeyConstants.Product.APP_DETAIL, // 记得清除 C 端详情缓存
            CacheKeyConstants.Product.LIST
    }, allEntries = true)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        // 1. 删除关联的 SKU 库存信息 (防止产生垃圾数据)
        skuStockService.remove(new LambdaQueryWrapper<SkuStock>()
                .eq(SkuStock::getProductId, id));

        // 2. 调用父类方法删除商品本身
        return super.removeById(id);
    }
}
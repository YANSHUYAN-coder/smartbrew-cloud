package com.coffee.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.dto.PageParam;
import com.coffee.common.vo.ProductDetailVO;
import com.coffee.system.domain.entity.Category;
import com.coffee.system.domain.entity.Product;
import com.coffee.system.domain.entity.SkuStock;
import com.coffee.system.domain.dto.ProductDTO;
import com.coffee.system.mapper.ProductMapper;
import com.coffee.system.service.CategoryService;
import com.coffee.system.service.ProductService;
import com.coffee.system.service.SkuStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private SkuStockService skuStockService;

    @Autowired
    private CategoryService categoryService;

    @Override
    public Page<Product> getList(PageParam pageParam) {
        Page<Product> productPage = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Product::getCreateTime);
        return this.page(productPage, wrapper);
    }

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

    @Override
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
}
package com.coffee.common.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * C端商品详情展示对象
 * 包含：商品基本信息 + SKU列表
 */
@Data
public class ProductDetailVO {
    // --- 商品基本信息 ---
    private Long id;
    private String name;
    private String picUrl;
    private String description;
    private BigDecimal price; // 基础价格
    private String category;  // 分类
    private Integer sales;    // 销量

    // --- SKU 规格列表 ---
    // 前端根据这个列表渲染选规格的弹窗
    private List<?> skuList; 
}
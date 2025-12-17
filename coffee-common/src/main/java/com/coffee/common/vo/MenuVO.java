package com.coffee.common.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 菜单数据 VO
 * 包含分类列表和商品列表，前端可直接使用
 */
@Data
public class MenuVO {
    /**
     * 分类列表
     */
    private List<CategoryVO> categories;
    
    /**
     * 商品列表（每个商品包含 categoryId）
     */
    private List<ProductVO> products;
    
    /**
     * 分类信息
     */
    @Data
    public static class CategoryVO {
        private Long id;
        private String name;
    }
    
    /**
     * 商品信息
     */
    @Data
    public static class ProductVO {
        private Long id;
        private String name;
        private String picUrl;
        private String description;
        private BigDecimal price;
        private Long categoryId;  // 关联分类ID
        private Integer sales;
        private Double rating;     // 评分（如果有）
    }
}


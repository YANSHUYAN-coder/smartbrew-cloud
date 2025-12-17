package com.coffee.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 菜单数据 VO
 * 包含分类列表和商品列表，前端可直接使用
 */
@Data
@Schema(description = "菜单数据（包含分类与商品列表）")
public class MenuVO {

    /**
     * 分类列表
     */
    @Schema(description = "分类列表")
    private List<CategoryVO> categories;
    
    /**
     * 商品列表（每个商品包含 categoryId）
     */
    @Schema(description = "商品列表")
    private List<ProductVO> products;
    
    /**
     * 分类信息
     */
    @Data
    @Schema(description = "商品分类信息")
    public static class CategoryVO {
        @Schema(description = "分类ID")
        private Long id;

        @Schema(description = "分类名称")
        private String name;
    }
    
    /**
     * 商品信息
     */
    @Data
    @Schema(description = "菜单商品信息")
    public static class ProductVO {
        @Schema(description = "商品ID")
        private Long id;

        @Schema(description = "商品名称")
        private String name;

        @Schema(description = "商品主图地址")
        private String picUrl;

        @Schema(description = "商品描述")
        private String description;

        @Schema(description = "商品价格")
        private BigDecimal price;

        @Schema(description = "所属分类ID")
        private Long categoryId;  // 关联分类ID

        @Schema(description = "销量")
        private Integer sales;

        @Schema(description = "评分（如 4.5）")
        private Double rating;     // 评分（如果有）
    }
}


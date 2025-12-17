package com.coffee.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * C端商品详情展示对象
 * 包含：商品基本信息 + SKU列表
 */
@Data
@Schema(description = "商品详情（含基础信息与SKU列表）")
public class ProductDetailVO {

    // --- 商品基本信息 ---
    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品主图地址")
    private String picUrl;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "基础价格")
    private BigDecimal price; // 基础价格

    @Schema(description = "所属分类名称")
    private String category;  // 分类

    @Schema(description = "销量")
    private Integer sales;    // 销量

    // --- SKU 规格列表 ---
    // 前端根据这个列表渲染选规格的弹窗
    @Schema(description = "SKU 规格列表（结构与前端约定）")
    private List<?> skuList;
}
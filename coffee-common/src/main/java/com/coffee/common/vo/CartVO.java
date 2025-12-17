package com.coffee.common.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车项VO
 */
@Data
public class CartVO {
    /**
     * 购物车项ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品SKU ID
     */
    private Long productSkuId;

    /**
     * 商品主图
     */
    private String productPic;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品副标题（SKU规格描述）
     * 例如：大杯,热,全糖
     */
    private String productSubTitle;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private Integer quantity;
}


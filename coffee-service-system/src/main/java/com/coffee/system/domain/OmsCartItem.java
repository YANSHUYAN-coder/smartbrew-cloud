package com.coffee.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 购物车商品明细
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oms_cart_item")
public class OmsCartItem extends BaseEntity {

    /**
     * 会员ID
     */
    private Long memberId;

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
     * SKU条码
     */
    private String productSkuCode;

    /**
     * 加入购物车时的价格
     */
    private BigDecimal price;

    /**
     * 购买数量
     */
    private Integer quantity;
}
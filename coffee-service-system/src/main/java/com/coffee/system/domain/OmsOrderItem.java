package com.coffee.system.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单包含的商品明细
 * 注意：不继承 BaseEntity，因为订单快照通常没有 update_time
 */
@Data
@TableName("oms_order_item")
public class OmsOrderItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品图片
     */
    private String productPic;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品货号
     */
    private String productSn;

    /**
     * 销售价格
     */
    private BigDecimal productPrice;

    /**
     * 购买数量
     */
    private Integer productQuantity;

    /**
     * 商品SKU ID
     */
    private Long productSkuId;

    /**
     * 商品SKU条码
     */
    private String productSkuCode;

    /**
     * 商品销售属性:[{"key":"规格","value":"大杯"}]
     */
    private String productAttr;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
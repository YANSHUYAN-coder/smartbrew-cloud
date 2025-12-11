package com.coffee.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("oms_order")
public class OmsOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_sn")
    private String orderSn;

    @TableField("member_id")
    private Long memberId;

    @TableField("coupon_id")
    private Long couponId;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("promotion_amount")
    private BigDecimal promotionAmount;

    @TableField("coupon_amount")
    private BigDecimal couponAmount;

    @TableField("pay_amount")
    private BigDecimal payAmount;

    /**
     * 支付方式：0->未支付；1->支付宝；2->微信
     */
    @TableField("pay_type")
    private Integer payType;

    /**
     * 订单状态：0->待付款；1->待制作；2->制作中；3->待取餐；4->已完成；5->已取消
     */
    @TableField("status")
    private Integer status;

    @TableField("delivery_company")
    private String deliveryCompany;

    @TableField("delivery_sn")
    private String deliverySn;

    @TableField("receiver_name")
    private String receiverName;

    @TableField("receiver_phone")
    private String receiverPhone;

    @TableField("receiver_post_code")
    private String receiverPostCode;

    @TableField("receiver_province")
    private String receiverProvince;

    @TableField("receiver_city")
    private String receiverCity;

    @TableField("receiver_region")
    private String receiverRegion;

    @TableField("receiver_detail_address")
    private String receiverDetailAddress;

    @TableField("note")
    private String note;

    @TableField("confirm_status")
    private Integer confirmStatus;

    @TableField("payment_time")
    private LocalDateTime paymentTime;

    @TableField("delivery_time")
    private LocalDateTime deliveryTime;

    @TableField("receive_time")
    private LocalDateTime receiveTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

}

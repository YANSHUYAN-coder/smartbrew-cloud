package com.coffee.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * C 端创建订单请求对象
 * 对应小程序/APP 前端提交的下单数据
 * 
 * 优化：只传ID，后端根据ID查询购物车和地址信息
 */
@Data
public class CreateOrderRequest {

    /**
     * 选中的购物车项ID列表
     */
    private List<Long> cartItemIds;

    /**
     * 收货地址ID
     */
    private Long addressId;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 订单备注（可选）
     */
    private String remark;

    /**
     * 配送方式（例如：门店自提 / 第三方配送）
     */
    private String deliveryCompany;

    /**
     * 支付方式：0->未支付；1->支付宝；2->微信；3->咖啡卡
     */
    private Integer payType;

    /**
     * 使用的咖啡卡ID（如果使用咖啡卡支付）
     */
    private Long coffeeCardId;

    /**
     * 订单金额相关（可选，用于后端校验，如果前端不传则后端计算）
     */
    private BigDecimal totalAmount;
    private BigDecimal promotionAmount;
    private BigDecimal couponAmount;
    private BigDecimal payAmount;
    
    /**
     * 使用的优惠券历史记录ID（如果使用优惠券）
     * 注意：这是 sms_coupon_history 表的ID，不是 sms_coupon 表的ID
     */
    private Long couponHistoryId;
}



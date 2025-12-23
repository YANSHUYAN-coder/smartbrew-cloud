package com.coffee.common.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    /**
     * 0 -> 待付款
     * 用户刚下单，尚未支付
     */
    PENDING_PAYMENT(0, "待付款"),

    /**
     * 1 -> 待制作
     * 用户已付款，等待商家/咖啡师制作
     */
    PENDING_MAKING(1, "待制作"),

    /**
     * 2 -> 制作中
     * 咖啡师正在制作中
     */
    MAKING(2, "制作中"),

    /**
     * 3 -> 待取餐
     * 制作完成，等待用户取餐
     */
    PENDING_PICKUP(3, "待取餐"),

    /**
     * 4 -> 已完成
     * 用户已取餐，订单完成
     */
    COMPLETED(4, "已完成"),

    /**
     * 5 -> 已取消
     * 订单被取消（用户取消或超时未支付）
     */
    CANCELLED(5, "已取消");

    private final Integer code;
    private final String desc;

    /**
     * 根据 code 获取描述
     * @param code 状态码
     * @return 描述信息
     */
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "";
        }
        for (OrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return "";
    }
}

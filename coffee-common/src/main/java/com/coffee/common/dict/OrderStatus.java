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
     * 1 -> 待发货
     * 用户已付款，等待商家/咖啡师制作
     */
    PENDING_DELIVERY(1, "待发货"),

    /**
     * 2 -> 已发货 (制作完成/配送中)
     * 咖啡师点击“制作完成”或外卖小哥已接单
     */
    SHIPPED(2, "已发货"),

    /**
     * 3 -> 已完成
     * 用户确认收货或系统自动确认
     */
    COMPLETED(3, "已完成"),

    /**
     * 4 -> 已关闭
     * 超时未支付被关闭，或由管理员手动关闭
     */
    CLOSED(4, "已关闭"),

    /**
     * 5 -> 无效订单
     * 恶意刷单或异常订单
     */
    INVALID(5, "无效订单");

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

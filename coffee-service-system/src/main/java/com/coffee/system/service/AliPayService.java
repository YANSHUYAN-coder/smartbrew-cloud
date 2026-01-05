package com.coffee.system.service;

import com.coffee.system.domain.entity.OmsOrder;
import java.util.Map;

public interface AliPayService {
    /**
     * 支付宝沙箱支付
     *
     * @param orderId
     * @return
     */
    String payByAlipay(Long orderId);

    /**
     * 处理支付宝回调
     * @param params 支付宝回调参数
     * @return "success" or "failure"
     */
    String handleAlipayNotify(Map<String, String> params);

    /**
     * 查询支付宝订单支付状态
     * @param outTradeNo 商户订单号
     * @return true: 已支付, false: 未支付
     */
    boolean checkPaymentStatus(String outTradeNo);

    /**
     * 同步并更新订单支付状态（手动补偿）
     * @param orderId 订单ID
     * @return 更新结果
     */
    boolean syncPaymentStatus(Long orderId);

    /**
     * 执行支付成功后的订单处理逻辑
     * @param order 订单对象
     * @return 是否处理成功
     */
    /**
     * 支付宝退款
     * @param orderId 订单ID
     * @param reason 退款原因
     * @return 是否退款成功
     */
    boolean refund(Long orderId, String reason);
}

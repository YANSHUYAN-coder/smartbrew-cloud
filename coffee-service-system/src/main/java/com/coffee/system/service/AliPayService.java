package com.coffee.system.service;

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
}

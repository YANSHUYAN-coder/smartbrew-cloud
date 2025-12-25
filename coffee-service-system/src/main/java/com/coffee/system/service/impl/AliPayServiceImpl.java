package com.coffee.system.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffee.common.dict.OrderStatus;
import com.coffee.system.config.AliPayConfiguration;
import com.coffee.system.domain.entity.OmsOrder;
import com.coffee.system.service.AliPayService;
import com.coffee.system.service.GiftCardService;
import com.coffee.system.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
public class AliPayServiceImpl implements AliPayService {
    @Autowired
    private OrderService orderService;
    @Autowired
    private GiftCardService giftCardService;
    @Autowired
    private AlipayClient alipayClient;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    // 注入支付宝配置类，用于获取公钥、编码格式等
    @Autowired
    private AliPayConfiguration alipayConfig;


    /**
     * 发起支付宝支付
     *
     * @param orderId 订单ID
     * @return 支付宝签名后的 orderStr
     */
    public String payByAlipay(Long orderId) {
        // 1. 查询订单
        OmsOrder order = orderService.getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        // 只有待支付状态的订单才能发起支付
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getStatus())) {
            throw new RuntimeException("订单状态异常，当前状态：" + OrderStatus.getDescByCode(order.getStatus()) + "，只有待付款订单才能支付");
        }

        // 2. 构造 App 支付请求参数
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();

        model.setBody("智咖云咖啡订单");
        model.setSubject("智咖云订单-" + order.getOrderSn());
        model.setOutTradeNo(order.getOrderSn()); // 商户订单号，不能重复
        model.setTimeoutExpress("30m"); // 30分钟超时
        model.setTotalAmount(order.getPayAmount().toString()); // 支付金额
        model.setProductCode("QUICK_MSECURITY_PAY"); // 固定值：App支付

        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl); // 异步通知地址

        try {
            // 3. 调用 SDK 生成 signed order string
            // 注意这里使用的是 sdkExecute，不是 pageExecute
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
            // 4. 直接返回 Body，这就是前端需要的 orderStr
            return response.getBody();
        } catch (Exception e) {
            log.error("支付宝签名失败", e);
            throw new RuntimeException("支付发起失败");
        }
    }

    /**
     * 处理支付宝回调
     *
     * @param params 支付宝回调参数 (从 Controller 接收的 Map)
     * @return "success" or "failure"
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleAlipayNotify(Map<String, String> params) {
        try {
            log.info("开始处理支付宝回调，参数: {}", params);

            // 1. 验签 (Critical Step!)
            // 必须使用支付宝公钥验签，确保消息确实来自支付宝，防止黑客伪造请求
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(), // 支付宝公钥
                    alipayConfig.getCharset(),         // 字符集 (UTF-8)
                    alipayConfig.getSignType()         // 签名类型 (RSA2)
            );

            if (!signVerified) {
                log.error("支付宝回调验签失败！请检查公钥配置或是否存在攻击行为。Params: {}", params);
                return "failure"; // 验签失败，直接返回 failure
            }

            // 2. 获取核心业务参数
            // out_trade_no: 商户订单号 (对应我们系统的 OrderSn)
            String outTradeNo = params.get("out_trade_no");
            // trade_no: 支付宝交易号 (流水号)
            String tradeNo = params.get("trade_no");
            // trade_status: 交易状态
            String tradeStatus = params.get("trade_status");
            // total_amount: 支付金额
            String totalAmountStr = params.get("total_amount");

            // 3. 校验交易状态
            // 只有 TRADE_SUCCESS (支付成功) 或 TRADE_FINISHED (交易结束) 才认为是支付成功
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                log.info("支付宝回调非支付成功状态: {}", tradeStatus);
                return "success"; // 这里的 success 是告诉支付宝收到了，不需要重试，但业务上不处理
            }

            // 4. 查询本地订单
            OmsOrder order = orderService.getOne(new LambdaQueryWrapper<OmsOrder>()
                    .eq(OmsOrder::getOrderSn, outTradeNo));

            if (order == null) {
                log.error("支付宝回调订单不存在: {}", outTradeNo);
                return "failure"; // 订单不存在，可能是异常数据
            }

            // 5. 幂等性检查 (防止重复回调导致数据错误)
            // 假设 status > 0 表示已经支付过 (0:待支付, 1:待发货, 2:已发货...)
            if (order.getStatus() > 0) {
                log.info("订单 {} 已支付，忽略重复回调", outTradeNo);
                return "success";
            }

            // 6. 金额一致性校验 (防止金额篡改攻击)
            // 必须比较回调金额与数据库订单金额是否一致
            BigDecimal notifyPayAmount = new BigDecimal(totalAmountStr);
            // 注意：BigDecimal 比较必须用 compareTo，不能用 equals
            if (order.getPayAmount().compareTo(notifyPayAmount) != 0) {
                log.error("支付宝回调金额不一致! 订单金额:{}, 回调金额:{}", order.getPayAmount(), notifyPayAmount);
                return "failure";
            }

            // 7. 更新订单状态
            // 这是一个事务操作
            order.setStatus(1); // 设置为已支付/待制作状态
            order.setPayType(1); // 1: 支付宝
            order.setPaymentTime(LocalDateTime.now());
            // 如果 OmsOrder 表有 transaction_id 字段，建议保存支付宝流水号
            // order.setTransactionId(tradeNo);

            boolean updateResult = orderService.updateById(order);

            if (updateResult) {
                log.info("订单支付成功，状态更新完成: {}", outTradeNo);

                // 8. 检查是否是咖啡卡订单（虚拟商品），如果是则创建并激活咖啡卡，并将订单状态设为已完成
                if (order.getDeliveryCompany() != null && "虚拟商品".equals(order.getDeliveryCompany())) {
                    try {
                        giftCardService.activateGiftCardByOrder(order.getId());
                        log.info("咖啡卡创建并激活成功，订单ID: {}", order.getId());
                        
                        // 咖啡卡是虚拟商品，支付成功后直接设为已完成
                        order.setStatus(OrderStatus.COMPLETED.getCode());
                        orderService.updateById(order);
                        log.info("咖啡卡订单状态已更新为已完成，订单ID: {}", order.getId());
                    } catch (Exception e) {
                        log.error("咖啡卡创建失败，订单ID: {}", order.getId(), e);
                        // 咖啡卡创建失败不影响支付成功，但需要记录日志以便后续处理
                    }
                }
                
                // 9. 如果使用咖啡卡支付，从咖啡卡余额中扣减
                if (order.getCoffeeCardId() != null && order.getPayType() == 3) {
                    try {
                        giftCardService.deductBalance(order.getCoffeeCardId(), order.getPayAmount(), order.getId());
                        log.info("咖啡卡扣减成功，订单ID: {}, 扣减金额: {}", order.getId(), order.getPayAmount());
                    } catch (Exception e) {
                        log.error("咖啡卡扣减失败，订单ID: {}", order.getId(), e);
                        // 咖啡卡扣减失败不影响支付成功，但需要记录日志以便后续处理
                    }
                }

                return "success"; // 告诉支付宝处理成功，不要再发通知了
            } else {
                log.error("订单状态更新失败: {}", outTradeNo);
                return "failure"; // 数据库更新失败，让支付宝重试
            }

        } catch (AlipayApiException e) {
            log.error("支付宝SDK验签异常", e);
            return "failure";
        } catch (Exception e) {
            log.error("处理支付宝回调系统异常", e);
            return "failure";
        }
    }
}

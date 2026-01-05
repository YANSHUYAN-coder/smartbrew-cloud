package com.coffee.system.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffee.common.config.RabbitMqConfig;
import com.coffee.common.dict.OrderStatus;
import com.coffee.system.config.AliPayConfiguration;
import com.coffee.system.domain.entity.OmsOrder;
import com.coffee.system.service.AliPayService;
import com.coffee.system.service.GiftCardService;
import com.coffee.system.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    @Autowired
    private RabbitTemplate rabbitTemplate;


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
     * 支付宝退款
     */
    @Override
    public boolean refund(Long orderId, String reason) {
        OmsOrder order = orderService.getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        
        // 只能退款已支付且未完成/未取消的订单（或者根据业务需求调整）
        // 这里假设状态为1（待制作）时允许退款
        if (!OrderStatus.PENDING_MAKING.getCode().equals(order.getStatus())) {
            throw new RuntimeException("当前订单状态不支持退款");
        }

        // 调用支付宝退款接口
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(order.getOrderSn());
        model.setRefundAmount(order.getPayAmount().toString());
        model.setRefundReason(reason);
        // 标识一次退款请求，同一笔交易多次退款需要保证唯一，如需支持部分退款，这里需要生成新的请求号
        model.setOutRequestNo(order.getOrderSn() + "_refund"); 

        request.setBizModel(model);

        try {
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                log.info("支付宝退款成功，订单号: {}, 退款金额: {}", order.getOrderSn(), order.getPayAmount());
                return true;
            } else {
                log.error("支付宝退款失败，订单号: {}, 错误码: {}, 错误信息: {}", 
                        order.getOrderSn(), response.getCode(), response.getSubMsg());
                throw new RuntimeException("退款失败: " + response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            log.error("调用支付宝退款接口异常", e);
            throw new RuntimeException("退款系统异常");
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
            BigDecimal notifyPayAmount;
            try {
                notifyPayAmount = new BigDecimal(totalAmountStr);
            } catch (NumberFormatException e) {
                log.error("支付宝回调金额格式错误: {}", totalAmountStr, e);
                return "failure";
            }
            // 注意：BigDecimal 比较必须用 compareTo，不能用 equals
            if (order.getPayAmount().compareTo(notifyPayAmount) != 0) {
                log.error("支付宝回调金额不一致! 订单金额:{}, 回调金额:{}", order.getPayAmount(), notifyPayAmount);
                return "failure";
            }

            // 7. 更新订单状态
            boolean updateResult = completeOrderPayment(order);

            if (updateResult) {
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

    /**
     * 主动查询支付宝订单支付状态
     */
    @Override
    public boolean checkPaymentStatus(String outTradeNo) {
        log.info("开始主动查询支付宝订单状态，商户单号: {}", outTradeNo);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(outTradeNo);
        request.setBizModel(model);

        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                log.info("支付宝查询返回状态: {}", tradeStatus);
                // TRADE_SUCCESS: 支付成功, TRADE_FINISHED: 交易结束（不可退款）
                return "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
            }
        } catch (AlipayApiException e) {
            log.error("调用支付宝查询接口异常, outTradeNo: {}", outTradeNo, e);
        }
        return false;
    }

    /**
     * 手动同步订单支付状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean syncPaymentStatus(Long orderId) {
        OmsOrder order = orderService.getById(orderId);
        if (order == null) return false;

        // 如果订单已经不是待支付状态，直接返回 true
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getStatus())) {
            return true;
        }

        // 主动查支付宝
        boolean isPaid = checkPaymentStatus(order.getOrderSn());
        if (isPaid) {
            log.info("主动查询确认订单 {} 已支付，执行状态同步逻辑", order.getOrderSn());
            return completeOrderPayment(order);
        }
        
        return false;
    }

    /**
     * 抽取公共的支付成功处理逻辑
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean completeOrderPayment(OmsOrder order) {
        // 幂等性检查
        if (order.getStatus() > 0) {
            return true;
        }

        order.setStatus(OrderStatus.PENDING_MAKING.getCode()); // 设置为待制作状态
        order.setPayType(1); // 1: 支付宝
        order.setPaymentTime(LocalDateTime.now());

        boolean updateResult = orderService.updateById(order);

        if (updateResult) {
            log.info("订单支付成功处理完成，订单号: {}", order.getOrderSn());
            
            // 处理咖啡卡逻辑
            if (order.getOrderType() != null && order.getOrderType() == 1) {
                try {
                    giftCardService.activateGiftCardByOrder(order.getId());
                    order.setStatus(OrderStatus.COMPLETED.getCode());
                    orderService.updateById(order);
                } catch (Exception e) {
                    log.error("咖啡卡激活失败", e);
                }
            }

            // 发送 MQ 消息 (增加 CorrelationData 用于消息确认)
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.ORDER_EVENT_EXCHANGE,
                    RabbitMqConfig.ORDER_PAY_KEY,
                    order.getId(),
                    new CorrelationData("pay-" + order.getOrderSn())
            );
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.ORDER_EXCHANGE,
                    RabbitMqConfig.NEW_ORDER_KEY,
                    order.getId(),
                    new CorrelationData("new-" + order.getOrderSn())
            );
        }
        return updateResult;
    }
}

package com.coffee.system.listener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.coffee.common.config.RabbitMqConfig;
import com.coffee.system.domain.entity.OmsOrder;
import com.coffee.system.domain.entity.UmsMember;
import com.coffee.system.service.OrderService;
import com.coffee.system.service.UmsMemberIntegrationHistoryService;
import com.coffee.system.service.UmsMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;

import java.io.IOException;
import java.math.BigDecimal;

@Component
@RabbitListener(queues = RabbitMqConfig.ORDER_PAY_QUEUE)
@Slf4j
public class MemberIntegrationListener {

    @Autowired
    private UmsMemberService memberService;
    @Autowired
    private OrderService orderService;

    @Autowired
    private UmsMemberIntegrationHistoryService integrationHistoryService;

    @RabbitHandler
    public void handlePaySuccess(Long orderId, Channel channel, Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("收到支付成功消息，准备发放积分。OrderId: {}", orderId);
            // 1. 幂等性检查 (我们刚才加的)
            if (integrationHistoryService.hasRecord(orderId)) {
                log.info("该订单已发放过积分，幂等拦截。OrderId: {}", orderId);
                channel.basicAck(deliveryTag, false);
                return;
            }
            // 2. 业务逻辑
            OmsOrder order = orderService.getById(orderId);
            if (order == null) {
                // 订单不存在，可能是时序问题，建议抛异常重试或者记录错误
                log.error("订单不存在，无法发放积分。OrderId: {}", orderId);
                channel.basicAck(deliveryTag, false); // 或者 basicReject
                return;
            }

            // 计算成长值：每消费1元增加1成长值（取整数部分）
            if (order.getPayAmount() != null && order.getPayAmount().compareTo(BigDecimal.ZERO) > 0) {
                int growth = order.getPayAmount().intValue(); // 取整数部分
                if (growth > 0) {
                    memberService.addGrowth(order.getMemberId(), growth);
                    log.info("订单完成，增加成长值: 订单ID={}, 用户ID={}, 成长值={}", orderId, order.getMemberId(), growth);
                }
            }

            // 计算积分：每消费1元增加1积分（取整数部分）
            if (order.getPayAmount() != null && order.getPayAmount().compareTo(BigDecimal.ZERO) > 0) {
                int integration = order.getPayAmount().intValue(); // 取整数部分
                if (integration > 0) {
                    boolean success = memberService.addIntegration(order.getMemberId(), integration);
                    if (success){
                        log.info("订单完成，增加积分: 订单ID={}, 用户ID={}, 积分={}", orderId, order.getMemberId(), integration);
                        // 记录历史明细
                        integrationHistoryService.recordIntegrationChange(
                                order.getMemberId(),
                                3, // 3->订单完成获得
                                integration,
                                "order_pay",
                                orderId,
                                String.format("订单完成获得，订单号：%s", order.getOrderSn())
                        );
                    }
                }
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理积分发放消息异常，准备抛出以触发重试。OrderId: {}, 错误: {}", orderId, e.getMessage());

            // 【核心修改】
            // 1. 如果是明显的逻辑错误（比如数据校验不通过），这种重试也没用，可以直接 discard
            // channel.basicReject(deliveryTag, false);

            // 2. 如果是系统异常（数据库超时、网络抖动），抛出异常让 Spring 自动重试
            // 注意：这里抛出后，Spring 会根据 YAML 配置重试 3 次
            // 3 次都失败后，Spring 默认会打印 Error 日志并丢弃消息（除非配置了 MessageRecoverer）
            throw new RuntimeException("处理积分消息失败，触发重试", e);
        }
    }
}
package com.coffee.system.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffee.common.config.RabbitMqConfig;
import com.coffee.common.dict.OrderStatus;
import com.coffee.system.domain.entity.OmsOrder;
import com.coffee.system.service.OrderService;
import com.coffee.system.service.SkuStockService;
import com.coffee.system.service.SmsCouponService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Component
public class OrderTimeOutListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    private SkuStockService skuStockService;
    
    @Autowired
    private SmsCouponService smsCouponService;

    /**
     * 监听订单超时队列
     */
    @RabbitListener(queues = RabbitMqConfig.ORDER_TIMEOUT_QUEUE_NAME)
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderTimeout(Long orderId, Message message, Channel channel) throws IOException {
        log.info("收到订单超时检查消息，OrderId: {}", orderId);

        try {
            // 1. 查询订单当前状态
            OmsOrder order = orderService.getById(orderId);
            
            // 2. 只有“待支付”状态的订单才需要取消
            if (order != null && order.getStatus() == OrderStatus.PENDING_PAYMENT.getValue()) {
                
                // 2.1 修改订单状态为“已取消”
                order.setStatus(OrderStatus.CANCELLED.getValue());
                order.setCancelReason("订单超时未支付，自动取消");
                orderService.updateById(order);

                // 2.2 释放库存 (调用你之前写好的释放逻辑)
                skuStockService.releaseStock(orderId);
                
                // 2.3 退回优惠券 (如果有)
                if (order.getCouponId() != null) {
                    smsCouponService.releaseCoupon(order.getCouponId());
                }

                log.info("订单超时未支付，已自动取消并释放资源。OrderId: {}", orderId);
            } else {
                log.info("订单状态正常或已处理，无需取消。OrderId: {}, Status: {}", orderId, order == null ? "null" : order.getStatus());
            }

            // 3. 手动确认消息 (ACK)
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            log.error("处理订单超时消息异常: {}", e.getMessage(), e);
            // 发生异常，拒绝消息并重新入队 (requeue=true)，或者进入死信队列人工处理
            // 这里简单演示重新入队，生产环境建议有最大重试次数限制
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
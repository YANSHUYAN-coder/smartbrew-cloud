package com.coffee.admin.listener;

import com.coffee.common.config.RabbitMqConfig;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class NewOrderListener {

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // WebSocket 推送工具

    // 监听新订单队列
    @RabbitListener(queues = RabbitMqConfig.NEW_ORDER_QUEUE)
    public void handleNewOrder(Long orderId, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("管理端收到新订单通知，订单ID: {}", orderId);

            // 推送给所有订阅了 /topic/newOrder 的前端客户端
            // 这里可以封装更详细的对象，比如 {type: 'new_order', id: 123, msg: '您有新的订单'}
            messagingTemplate.convertAndSend("/topic/newOrder", "您有新的订单，ID: " + orderId);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理新订单通知失败，订单ID: {}", orderId, e);
            try {
                // ❌ 手动拒绝：告诉 MQ 处理失败了
                // 第一个 true: 拒绝所有未确认消息 (一般 false)
                // 第二个 false: 是否重新入队 (requeue)。
                //    设为 false -> 丢弃消息（或者进死信队列）
                //    设为 true -> 消息回到队列头部，重新投递（可能导致死循环）
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
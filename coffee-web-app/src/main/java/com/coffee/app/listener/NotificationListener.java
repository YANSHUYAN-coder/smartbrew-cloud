package com.coffee.app.listener;

import com.coffee.app.websocket.AppWebSocketServer;
import com.coffee.common.config.RabbitMqConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationListener {

    private final ObjectMapper objectMapper;

    /**
     * 监听取餐通知队列
     * 模式：Manual ACK (手动确认)
     */
    @RabbitListener(queues = RabbitMqConfig.PICKUP_NOTIFICATION_QUEUE)
    public void handlePickupNotification(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            // 1. 解析消息
            String body = new String(message.getBody(), "UTF-8");
            log.info("App端收到MQ消息: {}", body);

            @SuppressWarnings("unchecked")
            Map<String, Object> msgMap = objectMapper.readValue(body, Map.class);
            String userId = (String) msgMap.get("userId"); // 发送端已经转为 String

            if (userId == null || userId.isEmpty()) {
                log.warn("消息无效，缺少userId，直接丢弃。消息内容: {}", body);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 2. 发送 WebSocket
            // 构造发给前端的 JSON
            String wsMessage = objectMapper.writeValueAsString(msgMap);

            // 发送 (sendInfo 内部吞掉了异常，所以这里很安全)
            AppWebSocketServer.sendInfo(userId, wsMessage);

            log.info("WebSocket 推送完成，用户: {}", userId);

            // 3. 【成功】手动确认消息
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("消费消息发生异常: {}", e.getMessage(), e);

            // 4. 【失败】拒绝消息，且不重回队列 (requeue = false)
            // 这样消息会直接被丢弃（或进入死信队列），不会无限循环，也不会触发 Spring Retry 的报错
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("ACK/NACK 确认失败", ex);
            }
        }
    }
}
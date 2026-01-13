package com.coffee.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier; // 【新增】引入 Qualifier
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.Queue;

/**
 * RabbitMQ 延迟队列配置
 * 用于处理订单超时自动取消
 */
@Slf4j
@Configuration
public class RabbitMqConfig {

    /**
     * 【核心修复】使用 Customizer 替代 @PostConstruct 注入
     * 这样可以打破 RabbitTemplate 和 RabbitMqConfig 之间的循环依赖
     */
    @Bean
    public RabbitTemplateCustomizer rabbitTemplateCustomizer() {
        return rabbitTemplate -> {
            // 设置确认回调
            rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
                if (ack) {
                    log.debug("MQ 消息发送成功: correlationData={}", correlationData);
                } else {
                    log.error("MQ 消息发送失败: correlationData={}, 原因={}", correlationData, cause);
                }
            });

            // 设置退回回调
            rabbitTemplate.setReturnsCallback(returned -> {
                log.error("MQ 消息被退回: message={}, replyCode={}, replyText={}, exchange={}, routingKey={}",
                        returned.getMessage(), returned.getReplyCode(), returned.getReplyText(),
                        returned.getExchange(), returned.getRoutingKey());
            });
        };
    }

    // 延迟交换机名称
    public static final String DELAY_EXCHANGE_NAME = "order.delay.exchange";
    // 订单超时队列名称
    public static final String ORDER_TIMEOUT_QUEUE_NAME = "order.timeout.queue";
    // 路由键
    public static final String ORDER_TIMEOUT_ROUTING_KEY = "order.timeout";

    // 普通交换机
    public static final String ORDER_EVENT_EXCHANGE = "order.event.exchange";
    // 支付成功队列
    public static final String ORDER_PAY_QUEUE = "order.pay.queue";
    // 路由键
    public static final String ORDER_PAY_KEY = "order.pay.success";

    // 定义一个“异常交换机”（用于接收处理失败的消息）
    public static final String ERROR_EXCHANGE = "error.exchange";
    public static final String ERROR_QUEUE = "error.queue";
    public static final String ERROR_KEY = "error.routing.key";

    // 新订单队列
    public static final String ORDER_EXCHANGE = "order.event.exchange";
    public static final String NEW_ORDER_KEY = "order.new";
    public static final String NEW_ORDER_QUEUE = "order.new.queue";

    // 通知交换机
    public static final String NOTIFICATION_EXCHANGE = "coffee.notification.exchange";
    // 取餐通知路由键
    public static final String PICKUP_ROUTING_KEY = "notification.pickup";
    // 取餐通知队列
    public static final String PICKUP_NOTIFICATION_QUEUE = "coffee.notification.pickup.queue";

    /**
     * 定义延迟交换机
     * 注意：需要安装 rabbitmq_delayed_message_exchange 插件
     * type: "x-delayed-message"
     */
    @Bean
    public CustomExchange orderDelayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct"); // 内部路由方式
        return new CustomExchange(DELAY_EXCHANGE_NAME, "x-delayed-message", true, false, args);
    }

    /**
     * 定义订单超时队列
     */
    @Bean
    public Queue orderTimeoutQueue() {
        return new Queue(ORDER_TIMEOUT_QUEUE_NAME, true);
    }

    /**
     * 绑定队列到交换机
     * 【修复】添加 @Qualifier 指定具体 Bean 名称，解决多 Bean 歧义问题
     */
    @Bean
    public Binding bindingOrderTimeout(@Qualifier("orderTimeoutQueue") Queue orderTimeoutQueue,
                                       @Qualifier("orderDelayExchange") CustomExchange orderDelayExchange) {
        return BindingBuilder.bind(orderTimeoutQueue)
                .to(orderDelayExchange)
                .with(ORDER_TIMEOUT_ROUTING_KEY)
                .noargs();
    }

    /**
     * 定义普通交换机
     * @return
     */
    @Bean
    public DirectExchange orderEventExchange() {
        return new DirectExchange(ORDER_EVENT_EXCHANGE);
    }

    /**
     * 订单支付成功队列
     * @return
     */
    @Bean
    public Queue orderPayQueue() {
        return new Queue(ORDER_PAY_QUEUE);
    }

    /**
     * 绑定队列到交换机
     * @return
     */
    @Bean
    public Binding orderPayBinding() {
        // 这里直接调用方法获取 Bean，不会有歧义问题
        return BindingBuilder.bind(orderPayQueue()).to(orderEventExchange()).with(ORDER_PAY_KEY);
    }


    @Bean
    public DirectExchange errorExchange() {
        return new DirectExchange(ERROR_EXCHANGE);
    }

    @Bean
    public Queue errorQueue() {
        return new Queue(ERROR_QUEUE);
    }

    @Bean
    public Binding errorBinding() {
        return BindingBuilder.bind(errorQueue()).to(errorExchange()).with(ERROR_KEY);
    }

    /**
     * 【核心配置】消息回收器
     * 当 Spring Retry 达到最大次数（3次）仍然失败时，会调用这个 Recoverer
     * 这里我们将消息重新发布到 "error.exchange"，由 error.queue 接收
     */
    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, ERROR_EXCHANGE, ERROR_KEY);
    }

    /**
     * 新订单通知队列
     */
    @Bean
    public Queue newOrderQueue() {
        return new Queue(NEW_ORDER_QUEUE);
    }

    /**
     * 将新订单队列绑定到订单交换机
     */
    @Bean
    public Binding newOrderBinding() {
        return BindingBuilder.bind(newOrderQueue())
                .to(orderEventExchange()) // 复用上面的 orderEventExchange
                .with(NEW_ORDER_KEY);
    }

    /**
     * 通知主题交换机
     */
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    /**
     * 取餐通知队列
     */
    @Bean
    public Queue pickupNotificationQueue() {
        return new Queue(PICKUP_NOTIFICATION_QUEUE, true); // 持久化队列
    }

    /**
     * 绑定取餐通知队列到通知交换机
     */
    @Bean
    public Binding bindingPickupNotification() {
        return BindingBuilder.bind(pickupNotificationQueue())
                .to(notificationExchange())
                .with(PICKUP_ROUTING_KEY);
    }

    /**
     * 配置 JSON 消息转换器
     * 确保消息以 JSON 格式发送和接收，而不是 Java 序列化
     * Spring Boot 会自动将这个转换器应用到 RabbitTemplate
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        // 设置内容类型为 application/json，确保消息格式正确
        converter.setCreateMessageIds(true);
        return converter;
    }
}
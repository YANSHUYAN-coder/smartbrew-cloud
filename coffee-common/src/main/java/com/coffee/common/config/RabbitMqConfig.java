package com.coffee.common.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.Queue;

/**
 * RabbitMQ 延迟队列配置
 * 用于处理订单超时自动取消
 */
@Configuration
public class RabbitMqConfig {

    // 延迟交换机名称
    public static final String DELAY_EXCHANGE_NAME = "order.delay.exchange";
    // 订单超时队列名称
    public static final String ORDER_TIMEOUT_QUEUE_NAME = "order.timeout.queue";
    // 路由键
    public static final String ORDER_TIMEOUT_ROUTING_KEY = "order.timeout";

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
     */
    @Bean
    public Binding bindingOrderTimeout(Queue orderTimeoutQueue, CustomExchange orderDelayExchange) {
        return BindingBuilder.bind(orderTimeoutQueue)
                .to(orderDelayExchange)
                .with(ORDER_TIMEOUT_ROUTING_KEY)
                .noargs();
    }
}
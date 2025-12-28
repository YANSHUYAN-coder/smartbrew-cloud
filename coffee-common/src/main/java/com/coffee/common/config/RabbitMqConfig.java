package com.coffee.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
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
}
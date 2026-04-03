package com.cloud.hospital.system.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 延迟队列配置
 * 用于订单超时自动取消：订单创建后若未在 TTL 内完成支付，消息过期路由到死信队列触发取消逻辑
 */
@Configuration
public class RabbitMQConfig {

    /** 延迟交换机名称 */
    public static final String ORDER_TTL_EXCHANGE = "order.ttl.exchange";

    /** 延迟消息路由键 */
    public static final String ORDER_TTL_ROUTING_KEY = "order.ttl.routing.key";

    /** 死信（实际消费）队列名称 */
    public static final String ORDER_DEAD_LETTER_QUEUE = "order.dead.letter.queue";

    /** 订单支付超时时间（毫秒），默认 15 分钟 */
    public static final int ORDER_TTL_MS = 15 * 60 * 1000;

    /**
     * 声明延迟交换机（依赖 RabbitMQ 延迟消息插件 rabbitmq_delayed_message_exchange）
     */
    @Bean
    public CustomExchange orderTtlExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(ORDER_TTL_EXCHANGE, "x-delayed-message", true, false, args);
    }

    /**
     * 声明死信消费队列
     */
    @Bean
    public Queue orderDeadLetterQueue() {
        return new Queue(ORDER_DEAD_LETTER_QUEUE, true);
    }

    /**
     * 将死信队列绑定到延迟交换机
     */
    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderDeadLetterQueue())
                .to(orderTtlExchange())
                .with(ORDER_TTL_ROUTING_KEY)
                .noargs();
    }
}

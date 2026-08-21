package com.example.hrms.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 配置交换机、队列、绑定关系和消息转换器
 *
 * 交换机：hrms.exchange（主题交换机）
 * 队列：
 *   - hrms.queue.payroll：工资单生成队列
 *   - hrms.queue.notification：消息通知队列
 *   - hrms.queue.dead：死信队列
 */
@Configuration
public class RabbitMQConfig {

    // 交换机
    public static final String EXCHANGE_NAME = "hrms.exchange";
    // 工资单队列
    public static final String PAYROLL_QUEUE = "hrms.queue.payroll";
    public static final String PAYROLL_ROUTING_KEY = "hrms.payroll.generate";
    // 消息通知队列
    public static final String NOTIFICATION_QUEUE = "hrms.queue.notification";
    public static final String NOTIFICATION_ROUTING_KEY = "hrms.notification.send";
    // 死信队列
    public static final String DEAD_QUEUE = "hrms.queue.dead";
    public static final String DEAD_ROUTING_KEY = "hrms.dead";

    /**
     * 主题交换机
     */
    @Bean
    public TopicExchange hrmsExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    /**
     * 工资单队列（绑定死信队列）
     */
    @Bean
    public Queue payrollQueue() {
        return QueueBuilder.durable(PAYROLL_QUEUE)
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", DEAD_ROUTING_KEY)
                .build();
    }

    /**
     * 消息通知队列
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", DEAD_ROUTING_KEY)
                .build();
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue deadQueue() {
        return QueueBuilder.durable(DEAD_QUEUE).build();
    }

    /**
     * 绑定工资单队列到交换机
     */
    @Bean
    public Binding payrollBinding() {
        return BindingBuilder.bind(payrollQueue()).to(hrmsExchange()).with(PAYROLL_ROUTING_KEY);
    }

    /**
     * 绑定消息通知队列到交换机
     */
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue()).to(hrmsExchange()).with(NOTIFICATION_ROUTING_KEY);
    }

    /**
     * 绑定死信队列到交换机
     */
    @Bean
    public Binding deadBinding() {
        return BindingBuilder.bind(deadQueue()).to(hrmsExchange()).with(DEAD_ROUTING_KEY);
    }

    /**
     * JSON 消息转换器
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate（配置JSON转换器和生产者确认）
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        // 生产者确认回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                // 消息发送失败，可以做重试或告警
                System.err.println("RabbitMQ消息发送失败: " + cause);
            }
        });
        // 消息返回回调（路由失败时触发）
        template.setReturnsCallback(returned -> {
            System.err.println("RabbitMQ消息路由失败: " + returned.getMessage());
        });
        return template;
    }
}

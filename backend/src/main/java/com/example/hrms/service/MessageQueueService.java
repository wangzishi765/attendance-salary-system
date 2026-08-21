package com.example.hrms.service;

import com.example.hrms.config.RabbitMQConfig;
import com.example.hrms.mq.NotificationMessage;
import com.example.hrms.mq.PayrollMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 消息队列服务
 * 封装 RabbitMQ 消息发送，支持优雅降级：
 * - 当 hrms.mq.enabled=true 且 RabbitMQ 可用时，异步发送消息
 * - 当 RabbitMQ 不可用时，记录日志并返回 false，由调用方决定是否同步执行
 */
@Service
public class MessageQueueService {

    private static final Logger log = LoggerFactory.getLogger(MessageQueueService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${hrms.mq.enabled:false}")
    private boolean mqEnabled;

    /**
     * 发送工资单生成消息
     * @param message 工资单消息
     * @return true=发送成功，false=发送失败（调用方应降级为同步执行）
     */
    public boolean sendPayrollGenerate(PayrollMessage message) {
        if (!mqEnabled) {
            return false;
        }
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.PAYROLL_ROUTING_KEY,
                    message
            );
            log.info("工资单生成消息已发送，月份={}", message.getMonth());
            return true;
        } catch (Exception e) {
            log.warn("工资单生成消息发送失败，将降级为同步执行：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 发送消息通知
     * @param message 通知消息
     * @return true=发送成功，false=发送失败
     */
    public boolean sendNotification(NotificationMessage message) {
        if (!mqEnabled) {
            return false;
        }
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    message
            );
            log.info("消息通知已发送，用户ID={}, 标题={}", message.getUserId(), message.getTitle());
            return true;
        } catch (Exception e) {
            log.warn("消息通知发送失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查MQ是否可用
     */
    public boolean isMqEnabled() {
        return mqEnabled;
    }
}

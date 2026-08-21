package com.example.hrms.mq;

import com.example.hrms.config.RabbitMQConfig;
import com.example.hrms.config.TenantContext;
import com.example.hrms.service.PayrollService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 工资单生成消息消费者
 * 监听工资单生成队列，异步执行工资单生成
 * 仅在 hrms.mq.enabled=true 时生效
 */
@Component
@ConditionalOnProperty(name = "hrms.mq.enabled", havingValue = "true")
public class PayrollMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(PayrollMessageConsumer.class);

    @Autowired
    private PayrollService payrollService;

    @RabbitListener(queues = RabbitMQConfig.PAYROLL_QUEUE)
    public void handlePayrollGenerate(PayrollMessage message, Channel channel,
                                      @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            log.info("开始异步生成工资单，月份={}, 操作人={}", message.getMonth(), message.getOperatorName());

            // 设置租户上下文
            if (message.getTenantId() != null) {
                TenantContext.setTenantId(message.getTenantId());
            }

            // 执行工资单生成
            payrollService.generateMonth(message.getMonth());

            // 手动确认消息
            channel.basicAck(tag, false);
            log.info("工资单生成完成，月份={}", message.getMonth());

        } catch (Exception e) {
            log.error("工资单生成失败，月份={}", message.getMonth(), e);
            try {
                // 拒绝消息，消息会进入死信队列
                channel.basicNack(tag, false, false);
            } catch (IOException ioException) {
                log.error("消息拒绝失败", ioException);
            }
        } finally {
            TenantContext.clear();
        }
    }
}

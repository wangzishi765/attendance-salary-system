package com.example.hrms.mq;

import com.example.hrms.config.RabbitMQConfig;
import com.example.hrms.config.TenantContext;
import com.example.hrms.service.NotificationService;
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
 * 消息通知消费者
 * 监听消息通知队列，异步发送站内消息
 * 仅在 hrms.mq.enabled=true 时生效
 */
@Component
@ConditionalOnProperty(name = "hrms.mq.enabled", havingValue = "true")
public class NotificationMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationMessageConsumer.class);

    @Autowired
    private NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotification(NotificationMessage message, Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            // 设置租户上下文
            if (message.getTenantId() != null) {
                TenantContext.setTenantId(message.getTenantId());
            }

            // 发送站内消息
            notificationService.sendNotification(
                    message.getUserId(),
                    message.getTitle(),
                    message.getContent(),
                    message.getType()
            );

            channel.basicAck(tag, false);
            log.info("消息通知已处理，用户ID={}", message.getUserId());

        } catch (Exception e) {
            log.error("消息通知处理失败，用户ID={}", message.getUserId(), e);
            try {
                channel.basicNack(tag, false, false);
            } catch (IOException ioException) {
                log.error("消息拒绝失败", ioException);
            }
        } finally {
            TenantContext.clear();
        }
    }
}

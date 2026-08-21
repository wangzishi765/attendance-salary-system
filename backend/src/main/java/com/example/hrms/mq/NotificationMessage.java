package com.example.hrms.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 消息通知消息
 * 用于异步发送站内消息，避免业务接口被通知逻辑阻塞
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：SYSTEM / APPROVAL / ATTENDANCE / PAYROLL
     */
    private String type;

    /**
     * 租户ID
     */
    private Long tenantId;
}

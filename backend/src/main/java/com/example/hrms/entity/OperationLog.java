package com.example.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志
 */
@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 操作人ID */
    private Long userId;
    /** 操作人用户名 */
    private String username;
    /** 操作人姓名 */
    private String realName;
    /** 操作模块 */
    private String module;
    /** 操作类型 */
    private String operation;
    /** 操作描述 */
    private String description;
    /** 请求方法 */
    private String method;
    /** 请求参数 */
    private String params;
    /** IP地址 */
    private String ip;
    /** 操作状态：SUCCESS / FAIL */
    private String status;
    /** 错误信息 */
    private String errorMsg;
    /** 耗时（毫秒） */
    private Long costTime;
    /** 操作时间 */
    private Long tenantId;

    private LocalDateTime operationTime;
}

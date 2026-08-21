package com.example.hrms.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 工资单生成消息
 * 用于异步生成工资单，避免大量员工时接口超时
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 薪资月份，格式：yyyy-MM
     */
    private String month;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 消息创建时间
     */
    private Long createTime;
}

package com.example.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作流实例
 */
@Data
@TableName("workflow_instance")
public class WorkflowInstance {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 流程定义ID */
    private Long processId;
    /** 流程编码 */
    private String processCode;
    /** 流程名称 */
    private String processName;
    /** 发起人ID */
    private Long initiatorId;
    /** 发起人姓名 */
    private String initiatorName;
    /** 业务类型（LEAVE/OVERTIME等） */
    private String businessType;
    /** 业务ID */
    private Long businessId;
    /** 标题 */
    private String title;
    /** 当前节点索引 */
    private Integer currentNode;
    /** 状态：PENDING / APPROVED / REJECTED / CANCELLED */
    private String status;
    /** 发起时间 */
    private LocalDateTime startTime;
    /** 结束时间 */
    private LocalDateTime endTime;
    private Long tenantId;

    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

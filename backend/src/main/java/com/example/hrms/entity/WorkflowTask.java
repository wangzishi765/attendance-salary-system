package com.example.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作流审批任务
 */
@Data
@TableName("workflow_task")
public class WorkflowTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 流程实例ID */
    private Long instanceId;
    /** 节点名称 */
    private String nodeName;
    /** 节点索引 */
    private Integer nodeIndex;
    /** 审批人角色 */
    private String approverRole;
    /** 审批人ID */
    private Long approverId;
    /** 审批人姓名 */
    private String approverName;
    /** 状态：PENDING / APPROVED / REJECTED */
    private String status;
    /** 审批意见 */
    private String comment;
    /** 审批时间 */
    private LocalDateTime approveTime;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

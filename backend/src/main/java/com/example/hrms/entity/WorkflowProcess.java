package com.example.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作流流程定义
 */
@Data
@TableName("workflow_process")
public class WorkflowProcess {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 流程编码 */
    private String processCode;
    /** 流程名称 */
    private String processName;
    /** 流程描述 */
    private String description;
    /** 审批节点配置（JSON格式：[{"nodeName":"部门主管审批","role":"HR"},{"nodeName":"总经理审批","role":"ADMIN"}]） */
    private String nodesConfig;
    /** 状态：ACTIVE / DISABLED */
    private String status;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

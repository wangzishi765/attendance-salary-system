package com.example.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假记录
 */
@Data
@TableName("leave_record")
public class LeaveRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long employeeId;
    /** SICK / PERSONAL / ANNUAL / OTHER */
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal days;
    private String reason;
    /** PENDING / APPROVED / REJECTED */
    private String status;
    private String approver;
    private Long tenantId;

    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String employeeName;
}

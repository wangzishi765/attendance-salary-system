package com.example.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 加班记录
 */
@Data
@TableName("overtime_record")
public class OvertimeRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long employeeId;
    private LocalDate overtimeDate;
    private BigDecimal hours;
    private String reason;
    /** PENDING / APPROVED / REJECTED */
    private String status;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String employeeName;
}

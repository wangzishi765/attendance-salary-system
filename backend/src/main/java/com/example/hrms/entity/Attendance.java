package com.example.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤记录
 */
@Data
@TableName("attendance")
public class Attendance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long employeeId;
    private LocalDate attendDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    /** NORMAL / LATE / EARLY / ABSENT */
    private String status;
    private BigDecimal workHours;
    private String remark;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String employeeName;
}

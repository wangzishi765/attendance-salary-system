package com.example.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工资单
 */
@Data
@TableName("payroll")
public class Payroll {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long employeeId;
    /** 例如 2026-08 */
    private String salaryMonth;
    private BigDecimal baseSalary;
    private BigDecimal attendanceBonus;
    private BigDecimal overtimePay;
    private BigDecimal lateDeduct;
    private BigDecimal absentDeduct;
    private BigDecimal leaveDeduct;
    /** 社保扣除（个人部分） */
    private BigDecimal socialSecurityDeduct;
    /** 公积金扣除（个人部分） */
    private BigDecimal housingFundDeduct;
    /** 专项附加扣除 */
    private BigDecimal specialDeduction;
    private BigDecimal otherDeduct;
    private BigDecimal grossSalary;
    private BigDecimal tax;
    private BigDecimal netSalary;
    /** GENERATED / PAID */
    private String status;
    private String remark;
    private Long tenantId;

    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String employeeName;
    @TableField(exist = false)
    private String departmentName;
}

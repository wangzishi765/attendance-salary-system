package com.example.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 薪资规则（全局参数）
 */
@Data
@TableName("salary_rule")
public class SalaryRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    /** 每次迟到扣款 */
    private BigDecimal lateDeduct;
    /** 每天缺勤扣款 */
    private BigDecimal absentDeduct;
    /** 每天事假扣款 */
    private BigDecimal leaveDeduct;
    /** 每小时加班费 */
    private BigDecimal overtimeRate;
    /** 全勤奖 */
    private BigDecimal fullAttendanceBonus;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

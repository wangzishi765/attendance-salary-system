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
    /** 社保个人缴纳比例（如0.105表示10.5%） */
    private BigDecimal socialSecurityRate;
    /** 公积金个人缴纳比例（如0.07表示7%） */
    private BigDecimal housingFundRate;
    /** 个税起征点 */
    private BigDecimal taxThreshold;
    /** 专项附加扣除默认值（子女教育/赡养老人等） */
    private BigDecimal specialDeduction;
    private Long tenantId;

    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

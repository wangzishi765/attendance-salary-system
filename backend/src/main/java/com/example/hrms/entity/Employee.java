package com.example.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工
 */
@Data
@TableName("employee")
public class Employee {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String empNo;
    private String name;
    private String gender;
    private String phone;
    private String email;
    private Long departmentId;
    private String position;
    private BigDecimal baseSalary;
    private LocalDate hireDate;
    /** 在职 / 离职 */
    private String status;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 部门名称（关联查询用，不入库） */
    @TableField(exist = false)
    private String departmentName;
}

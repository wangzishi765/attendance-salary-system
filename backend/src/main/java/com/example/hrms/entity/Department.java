package com.example.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门
 */
@Data
@TableName("department")
public class Department {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 父部门ID，顶级为0 */
    private Long parentId;
    private String name;
    private Integer sort;
    /** 状态：启用/禁用 */
    private String status;
    private String remark;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 子部门（不入库） */
    @TableField(exist = false)
    private List<Department> children;
    /** 员工数量（不入库） */
    @TableField(exist = false)
    private Integer employeeCount;
}

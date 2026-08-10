package com.example.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门
 */
@Data
@TableName("department")
public class Department {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String remark;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

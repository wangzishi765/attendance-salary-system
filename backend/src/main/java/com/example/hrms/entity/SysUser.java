package com.example.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户（登录账号）
 */
@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    /** ADMIN / EMPLOYEE */
    private String role;
    /** 关联员工 id（员工账号） */
    private Long employeeId;
    /** 租户ID */
    private Long tenantId;
    private Integer enabled;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

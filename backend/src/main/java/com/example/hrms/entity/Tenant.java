package com.example.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户（企业）
 */
@Data
@TableName("tenant")
public class Tenant {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编码 */
    private String tenantCode;
    /** 租户名称（企业名称） */
    private String tenantName;
    /** 联系人 */
    private String contactPerson;
    /** 联系电话 */
    private String contactPhone;
    /** 地址 */
    private String address;
    /** 状态：ACTIVE / DISABLED */
    private String status;
    /** 过期时间 */
    private LocalDateTime expireTime;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

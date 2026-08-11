-- =============================================================
-- 中小企业员工考勤与薪资核算管理系统 - 建表脚本
-- 同时兼容 H2 (MODE=MySQL) 与 MySQL 8
-- =============================================================

-- 系统用户表（登录账号）
CREATE TABLE IF NOT EXISTS sys_user (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(64)  NOT NULL,
    password     VARCHAR(128) NOT NULL,
    real_name    VARCHAR(64),
    role         VARCHAR(20)  NOT NULL DEFAULT 'EMPLOYEE',   -- ADMIN / EMPLOYEE
    employee_id  BIGINT,                                     -- 关联员工（员工账号）
    enabled      INT          NOT NULL DEFAULT 1,
    deleted      INT          NOT NULL DEFAULT 0,
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- 部门表
CREATE TABLE IF NOT EXISTS department (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    parent_id    BIGINT       NOT NULL DEFAULT 0,       -- 父部门ID，顶级为0
    name         VARCHAR(64)  NOT NULL,
    sort         INT          NOT NULL DEFAULT 0,       -- 排序
    status       VARCHAR(16)  NOT NULL DEFAULT '启用',   -- 启用 / 禁用
    remark       VARCHAR(255),
    deleted      INT          NOT NULL DEFAULT 0,
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- 员工表
CREATE TABLE IF NOT EXISTS employee (
    id             BIGINT        AUTO_INCREMENT PRIMARY KEY,
    emp_no         VARCHAR(32)   NOT NULL,
    name           VARCHAR(64)   NOT NULL,
    gender         VARCHAR(8)    DEFAULT '男',
    phone          VARCHAR(20),
    email          VARCHAR(64),
    department_id  BIGINT,
    position       VARCHAR(64),
    base_salary    DECIMAL(12,2) NOT NULL DEFAULT 0,
    hire_date      DATE,
    status         VARCHAR(16)   NOT NULL DEFAULT '在职',    -- 在职 / 离职
    deleted        INT           NOT NULL DEFAULT 0,
    create_time    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- 考勤记录表
CREATE TABLE IF NOT EXISTS attendance (
    id             BIGINT        AUTO_INCREMENT PRIMARY KEY,
    employee_id    BIGINT        NOT NULL,
    attend_date    DATE          NOT NULL,
    check_in_time  TIMESTAMP,
    check_out_time TIMESTAMP,
    status         VARCHAR(16)   NOT NULL DEFAULT 'NORMAL',  -- NORMAL 正常 / LATE 迟到 / EARLY 早退 / ABSENT 缺勤
    work_hours     DECIMAL(6,2)  DEFAULT 0,
    remark         VARCHAR(255),
    deleted        INT           NOT NULL DEFAULT 0,
    create_time    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- 请假记录表
CREATE TABLE IF NOT EXISTS leave_record (
    id           BIGINT        AUTO_INCREMENT PRIMARY KEY,
    employee_id  BIGINT        NOT NULL,
    type         VARCHAR(16)   NOT NULL DEFAULT 'PERSONAL',  -- SICK 病假 / PERSONAL 事假 / ANNUAL 年假 / OTHER 其他
    start_date   DATE          NOT NULL,
    end_date     DATE          NOT NULL,
    days         DECIMAL(6,1)  NOT NULL DEFAULT 1,
    reason       VARCHAR(255),
    status       VARCHAR(16)   NOT NULL DEFAULT 'PENDING',   -- PENDING / APPROVED / REJECTED
    approver     VARCHAR(64),
    deleted      INT           NOT NULL DEFAULT 0,
    create_time  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- 加班记录表
CREATE TABLE IF NOT EXISTS overtime_record (
    id            BIGINT        AUTO_INCREMENT PRIMARY KEY,
    employee_id   BIGINT        NOT NULL,
    overtime_date DATE          NOT NULL,
    hours         DECIMAL(6,1)  NOT NULL DEFAULT 0,
    reason        VARCHAR(255),
    status        VARCHAR(16)   NOT NULL DEFAULT 'PENDING',  -- PENDING / APPROVED / REJECTED
    deleted       INT           NOT NULL DEFAULT 0,
    create_time   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- 薪资规则表（全局参数，一般只有一条）
CREATE TABLE IF NOT EXISTS salary_rule (
    id                   BIGINT        AUTO_INCREMENT PRIMARY KEY,
    name                 VARCHAR(64)   NOT NULL DEFAULT '默认薪资规则',
    late_deduct          DECIMAL(12,2) NOT NULL DEFAULT 50,   -- 每次迟到扣款
    absent_deduct        DECIMAL(12,2) NOT NULL DEFAULT 200,  -- 每天缺勤扣款
    leave_deduct         DECIMAL(12,2) NOT NULL DEFAULT 100,  -- 每天事假扣款
    overtime_rate        DECIMAL(12,2) NOT NULL DEFAULT 30,   -- 每小时加班费
    full_attendance_bonus DECIMAL(12,2) NOT NULL DEFAULT 300, -- 全勤奖
    social_security_rate DECIMAL(6,4)  NOT NULL DEFAULT 0.105, -- 社保个人缴纳比例（10.5%）
    housing_fund_rate    DECIMAL(6,4)  NOT NULL DEFAULT 0.07,  -- 公积金个人缴纳比例（7%）
    tax_threshold        DECIMAL(12,2) NOT NULL DEFAULT 5000,  -- 个税起征点
    special_deduction    DECIMAL(12,2) NOT NULL DEFAULT 0,     -- 专项附加扣除默认值
    deleted              INT           NOT NULL DEFAULT 0,
    create_time          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- 工资单表
CREATE TABLE IF NOT EXISTS payroll (
    id                   BIGINT        AUTO_INCREMENT PRIMARY KEY,
    employee_id          BIGINT        NOT NULL,
    salary_month         VARCHAR(7)    NOT NULL,               -- 例如 2026-08
    base_salary          DECIMAL(12,2) NOT NULL DEFAULT 0,
    attendance_bonus     DECIMAL(12,2) NOT NULL DEFAULT 0,     -- 全勤奖
    overtime_pay         DECIMAL(12,2) NOT NULL DEFAULT 0,     -- 加班费
    late_deduct          DECIMAL(12,2) NOT NULL DEFAULT 0,
    absent_deduct        DECIMAL(12,2) NOT NULL DEFAULT 0,
    leave_deduct         DECIMAL(12,2) NOT NULL DEFAULT 0,
    social_security_deduct DECIMAL(12,2) NOT NULL DEFAULT 0,   -- 社保扣除（个人部分）
    housing_fund_deduct  DECIMAL(12,2) NOT NULL DEFAULT 0,     -- 公积金扣除（个人部分）
    special_deduction    DECIMAL(12,2) NOT NULL DEFAULT 0,     -- 专项附加扣除
    other_deduct         DECIMAL(12,2) NOT NULL DEFAULT 0,
    gross_salary         DECIMAL(12,2) NOT NULL DEFAULT 0,     -- 应发
    tax                  DECIMAL(12,2) NOT NULL DEFAULT 0,     -- 个税
    net_salary           DECIMAL(12,2) NOT NULL DEFAULT 0,     -- 实发
    status               VARCHAR(16)   NOT NULL DEFAULT 'GENERATED', -- GENERATED / PAID
    remark               VARCHAR(255),
    deleted              INT           NOT NULL DEFAULT 0,
    create_time          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

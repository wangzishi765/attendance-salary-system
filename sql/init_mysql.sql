-- =============================================================
-- 中小企业员工考勤与薪资核算管理系统 - MySQL 初始化脚本（可选，手动导入用）
--
-- 说明：
--   1) 本项目默认使用内嵌 H2，无需 MySQL 即可运行；
--   2) 应用以 mysql profile 启动时会自动执行 backend/src/main/resources/db/schema.sql 建表，
--      并在数据库为空时由程序自动灌入演示数据，通常无需手动执行本脚本；
--   3) 本脚本仅供需要手动查看/初始化 MySQL 库结构时参考。
-- =============================================================

CREATE DATABASE IF NOT EXISTS hrms DEFAULT CHARSET utf8mb4;
USE hrms;

-- 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(64)  NOT NULL,
    password     VARCHAR(128) NOT NULL,
    real_name    VARCHAR(64),
    role         VARCHAR(20)  NOT NULL DEFAULT 'EMPLOYEE',
    employee_id  BIGINT,
    enabled      INT          NOT NULL DEFAULT 1,
    deleted      INT          NOT NULL DEFAULT 0,
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS department (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(64)  NOT NULL,
    remark       VARCHAR(255),
    deleted      INT          NOT NULL DEFAULT 0,
    create_time  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

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
    status         VARCHAR(16)   NOT NULL DEFAULT '在职',
    deleted        INT           NOT NULL DEFAULT 0,
    create_time    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS attendance (
    id             BIGINT        AUTO_INCREMENT PRIMARY KEY,
    employee_id    BIGINT        NOT NULL,
    attend_date    DATE          NOT NULL,
    check_in_time  TIMESTAMP     NULL,
    check_out_time TIMESTAMP     NULL,
    status         VARCHAR(16)   NOT NULL DEFAULT 'NORMAL',
    work_hours     DECIMAL(6,2)  DEFAULT 0,
    remark         VARCHAR(255),
    deleted        INT           NOT NULL DEFAULT 0,
    create_time    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS leave_record (
    id           BIGINT        AUTO_INCREMENT PRIMARY KEY,
    employee_id  BIGINT        NOT NULL,
    type         VARCHAR(16)   NOT NULL DEFAULT 'PERSONAL',
    start_date   DATE          NOT NULL,
    end_date     DATE          NOT NULL,
    days         DECIMAL(6,1)  NOT NULL DEFAULT 1,
    reason       VARCHAR(255),
    status       VARCHAR(16)   NOT NULL DEFAULT 'PENDING',
    approver     VARCHAR(64),
    deleted      INT           NOT NULL DEFAULT 0,
    create_time  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS overtime_record (
    id            BIGINT        AUTO_INCREMENT PRIMARY KEY,
    employee_id   BIGINT        NOT NULL,
    overtime_date DATE          NOT NULL,
    hours         DECIMAL(6,1)  NOT NULL DEFAULT 0,
    reason        VARCHAR(255),
    status        VARCHAR(16)   NOT NULL DEFAULT 'PENDING',
    deleted       INT           NOT NULL DEFAULT 0,
    create_time   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS salary_rule (
    id                    BIGINT        AUTO_INCREMENT PRIMARY KEY,
    name                  VARCHAR(64)   NOT NULL DEFAULT '默认薪资规则',
    late_deduct           DECIMAL(12,2) NOT NULL DEFAULT 50,
    absent_deduct         DECIMAL(12,2) NOT NULL DEFAULT 200,
    leave_deduct          DECIMAL(12,2) NOT NULL DEFAULT 100,
    overtime_rate         DECIMAL(12,2) NOT NULL DEFAULT 30,
    full_attendance_bonus DECIMAL(12,2) NOT NULL DEFAULT 300,
    deleted               INT           NOT NULL DEFAULT 0,
    create_time           TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payroll (
    id                BIGINT        AUTO_INCREMENT PRIMARY KEY,
    employee_id       BIGINT        NOT NULL,
    salary_month      VARCHAR(7)    NOT NULL,
    base_salary       DECIMAL(12,2) NOT NULL DEFAULT 0,
    attendance_bonus  DECIMAL(12,2) NOT NULL DEFAULT 0,
    overtime_pay      DECIMAL(12,2) NOT NULL DEFAULT 0,
    late_deduct       DECIMAL(12,2) NOT NULL DEFAULT 0,
    absent_deduct     DECIMAL(12,2) NOT NULL DEFAULT 0,
    leave_deduct      DECIMAL(12,2) NOT NULL DEFAULT 0,
    other_deduct      DECIMAL(12,2) NOT NULL DEFAULT 0,
    gross_salary      DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax               DECIMAL(12,2) NOT NULL DEFAULT 0,
    net_salary        DECIMAL(12,2) NOT NULL DEFAULT 0,
    status            VARCHAR(16)   NOT NULL DEFAULT 'GENERATED',
    remark            VARCHAR(255),
    deleted           INT           NOT NULL DEFAULT 0,
    create_time       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

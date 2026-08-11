<div align="center">

# 中小企业员工考勤与薪资核算管理系统
# Employee Attendance and Payroll Management System for SMEs

![Java](https://img.shields.io/badge/Java-8-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-6DB33F?logo=springboot&logoColor=white)
![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs&logoColor=white)
![Element Plus](https://img.shields.io/badge/Element%20Plus-2.8-409EFF?logo=element&logoColor=white)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5-red)
![Database](https://img.shields.io/badge/DB-H2%20%7C%20MySQL8-blue)
![License](https://img.shields.io/badge/License-学习用途-lightgrey)

**[🇨🇳 中文版](#-中文版) | [🇬🇧 English](#-english)**

</div>

---

<a id="-中文版"></a>

## 🇨🇳 中文版

> 基于 **Spring Boot + Vue 3** 的前后端分离项目，实现员工管理、考勤打卡、请假/加班审批、薪资规则配置与工资自动核算。
>
> **开箱即用**：默认使用内嵌 H2 数据库，无需安装 MySQL，启动即可登录使用；也支持一键切换 MySQL。

**[⬆️ 返回顶部](#中小企业员工考勤与薪资核算管理系统) | [🇬🇧 Switch to English](#-english)**

---

### 🖼️ 界面预览

| 登录 | 首页仪表盘（管理员） |
| :---: | :---: |
| ![登录](docs/screenshots/01-login.png) | ![仪表盘](docs/screenshots/02-dashboard.png) |

| 员工管理 | 工资单 |
| :---: | :---: |
| ![员工管理](docs/screenshots/03-employee.png) | ![工资单](docs/screenshots/08-payroll.png) |

| 考勤管理（列表） | 考勤管理（月历） |
| :---: | :---: |
| ![考勤列表](docs/screenshots/05-attendance.png) | ![考勤月历](docs/screenshots/10-attendance-calendar.png) |

| 部门管理 | 薪资规则 |
| :---: | :---: |
| ![部门管理](docs/screenshots/04-department.png) | ![薪资规则](docs/screenshots/09-salary-rule.png) |

| 请假管理 | 加班管理 |
| :---: | :---: |
| ![请假管理](docs/screenshots/06-leave.png) | ![加班管理](docs/screenshots/07-overtime.png) |

| 系统监控 | 考勤管理（月历） |
| :---: | :---: |
| ![系统监控](docs/screenshots/11-system-monitor.png) | ![考勤月历](docs/screenshots/10-attendance-calendar.png) |

---

### 📝 更新日志

#### v1.12.0 - 2026-08-11
**租户数据隔离**
- 🔒 基于 MyBatis-Plus TenantLineInnerInterceptor 实现自动租户隔离
- 📊 所有业务表（员工/部门/考勤/请假/加班/薪资/工作流/日志/消息）增加 tenant_id 字段
- 🎯 JWT 过滤器自动设置租户上下文，请求结束自动清除
- 🚫 sys_user 和 tenant 表忽略租户隔离（登录和租户管理需要跨租户）
- ✅ 不同租户的数据完全隔离，管理员也只能看到本租户数据

#### v1.11.0 - 2026-08-11
**数据备份功能**
- 💾 新增数据备份页面（仅管理员可见）
- 📦 使用 H2 BACKUP 命令安全备份数据库（运行时也可备份）
- 📋 备份文件列表展示（文件名/大小/备份时间）
- ⬇️ 支持下载备份文件（ZIP 格式）
- 🗑️ 支持删除备份文件
- 📁 备份文件保存在 backup 目录

#### v1.10.0 - 2026-08-11
**消息通知功能**
- 🔔 新增消息通知中心页面
- 📬 支持系统通知、审批通知、考勤通知、薪资通知四种类型
- 🔴 顶部栏消息图标 + 未读数量徽章（每30秒自动刷新）
- ✅ 点击消息标记已读，支持全部标记已读
- 🔍 支持按未读/已读状态筛选
- 🎨 渐变色消息图标，不同类型不同颜色

#### v1.9.0 - 2026-08-11
**操作日志功能**
- 📋 新增操作日志页面（仅管理员可见）
- 🔍 AOP 切面自动记录所有写操作（新增/修改/删除）
- 👤 记录操作人、操作模块、操作类型、请求参数、IP 地址
- ⏱️ 记录操作耗时、操作状态（成功/失败）、错误信息
- 🔎 支持按模块、操作人、状态多条件筛选查询

#### v1.8.0 - 2026-08-11
**多租户支持**
- 🏢 新增租户（企业）管理功能（仅管理员可见）
- 👥 用户表增加 tenant_id 字段，支持用户与租户关联
- 🔐 登录接口返回租户 ID 和租户名称
- 📊 前端用户状态存储租户信息
- 🔧 支持多企业共用一套系统，为后续数据隔离奠定基础

#### v1.7.0 - 2026-08-11
**工作流引擎**
- 🔄 新增工作流审批中心页面
- 📝 支持多级审批流程定义（JSON 配置节点）
- ✅ 发起流程、待我审批、我已审批、我发起的 四大视图
- 📜 审批历史时间线展示，支持查看每级审批意见
- 👥 预置请假审批流程（HR→管理员两级）、加班审批流程（HR一级）
- 🔐 按角色匹配审批人，自动流转到下一级

#### v1.6.0 - 2026-08-11
**统计报表功能**
- 📊 新增统计报表页面（仅管理员/人事可见）
- 👥 人员统计：总员工数、部门人数分布、性别比例、薪资区间分布、员工状态
- 📅 考勤统计：总打卡次数、考勤状态分布、各部门考勤对比、出勤率
- 💰 薪资成本：总成本统计、各部门薪资对比、薪资构成分析、人均实发工资
- 📈 ECharts 可视化：饼图 + 柱状图 + 明细表格

#### v1.5.0 - 2026-08-11
**系统监控功能**
- 🖥️ 新增系统监控页面（仅管理员可见）
- 📊 实时展示操作系统、JVM、磁盘、应用运行信息
- ⏱️ 每 10 秒自动刷新，支持手动刷新
- 📈 内存/磁盘使用率进度条，颜色随使用率变化（绿/橙/红）
- 🔐 严格权限控制，HR/员工账号不可访问

#### v1.4.0 - 2026-08-11
**薪资核算完善**
- 💰 新增社保扣除计算（基本工资 × 社保比例，默认 10.5%）
- 🏠 新增公积金扣除计算（基本工资 × 公积金比例，默认 7%）
- 🧾 新增个税专项附加扣除配置（子女教育/赡养老人等）
- 📊 个税起征点可配置（默认 5000 元）
- 🧮 完善个税计算：应纳税所得额 = 应发 − 社保 − 公积金 − 专项附加扣除 − 起征点
- 📋 工资单页面新增社保、公积金列
- 📤 Excel 导出增加 3 列（社保/公积金/专项附加扣除）
- ⚙️ 薪资规则页面分三组配置，底部显示完整计算规则说明

#### v1.3.0 - 2026-08-11
**界面美化与全局样式统一**
- 🎨 全新紫色主题设计，登录页渐变背景 + 装饰元素
- 📊 仪表盘优化：渐变色统计卡片 + ECharts 紫色主题图表
- 🔧 全局样式统一：卡片、表格、按钮、对话框、分页风格一致
- 👤 主布局优化：顶部栏页面标题 + 渐变色用户头像 + 角色标签
- 📱 左侧菜单美化：选中项渐变背景 + 阴影效果
- 💾 所有按钮渐变色 + hover 上浮动效

#### v1.2.0 - 2026-08-10
**功能增强**
- 📅 新增考勤月历视图，直观查看每月考勤状态
- 📊 首页新增 ECharts 统计图表（部门人数分布 / 工资柱状图）
- 🔐 新增修改密码功能
- 📤 工资单支持 Excel 导出
- 👥 新增 HR 人事专员角色，细化权限控制
- 🌳 部门管理支持树状结构 + 人数统计
- 📥 员工/考勤支持 Excel 批量导入

#### v1.1.0 - 2026-08-09
**基础功能完善**
- ✅ 员工管理 CRUD
- ✅ 部门管理
- ✅ 考勤打卡与统计
- ✅ 请假/加班申请与审批
- ✅ 薪资规则配置
- ✅ 工资自动核算
- ✅ JWT 登录鉴权 + 三级角色

---

### ✨ 功能特性

| 模块 | 功能 |
| --- | --- |
| 登录鉴权 | JWT 登录、管理员 / 人事 / 员工三级角色、接口权限控制、修改密码、**租户信息返回** |
| 首页仪表盘 | 管理员看全局统计 + ECharts 图表；员工看本月考勤统计 |
| 员工管理 | 员工增删改查、按部门/关键字检索、新增员工自动生成登录账号、**Excel 批量导入** |
| 部门管理 | **树状部门结构**、部门人数统计、多级部门、排序/状态管理 |
| 考勤管理 | 员工上/下班打卡、迟到/早退自动判定、管理员补录与查询、**月历视图**、**Excel 批量导入** |
| 请假管理 | 员工提交请假申请、管理员/人事审批（批准/驳回） |
| 加班管理 | 员工提交加班申请、管理员/人事审批 |
| 薪资规则 | 配置迟到/缺勤/事假扣款、加班费单价、全勤奖、**社保比例、公积金比例、个税起征点、专项附加扣除** |
| 工资核算 | 按月自动生成工资单：基本工资 + 全勤奖 + 加班费 − 各项扣款 − **社保 − 公积金 − 个税**，支持重算与发放、**Excel 导出** |
| **统计报表** | **人员统计报表（部门/性别/薪资区间）、考勤统计报表、薪资成本报表，ECharts 可视化（仅管理员/人事）** |
| **工作流引擎** | **多级审批流程定义、发起流程、待办/已办/我发起的、审批历史时间线、支持 HR→管理员两级审批** |
| **多租户支持** | **租户（企业）管理、用户租户关联、登录返回租户信息、支持多企业共用一套系统（仅管理员）** |
| **操作日志** | **AOP 自动记录所有写操作、操作人/模块/IP/耗时/状态、支持按模块/操作人/状态筛选（仅管理员）** |
| 系统监控 | **实时监控系统运行状态**：操作系统/JVM/磁盘/应用信息、内存使用率进度条、自动刷新（仅管理员） |
| 数据导入导出 | 员工 Excel 导入、考勤 Excel 导入、工资单 Excel 导出 |

---

### 🧰 技术栈

**后端**：Spring Boot 2.7 · Spring Security + JWT · MyBatis-Plus · H2 / MySQL 8 · Apache POI · Java 8
**前端**：Vue 3 · Vite 5 · Element Plus · Vue Router · Pinia · Axios · ECharts 5

---

### 🚀 快速开始（三选一）

> 系统要求：任选其一即可运行。为最大兼容性，后端目标为 **Java 8**，各主流操作系统均可运行。

#### 方式一：直接运行已打包的 Jar（最简单，只需 JDK 8+）

```bash
# 在 backend 目录下打包（首次需要 Maven，见方式二）后得到 target/hrms.jar
java -jar backend/target/hrms.jar
```

启动后浏览器访问 **http://localhost:8080** ，即为完整系统（前端已内置在 Jar 中）。

#### 方式二：本地开发运行（前后端分离）

**1. 启动后端**（需要 JDK 8 + Maven）
```bash
cd backend
mvn spring-boot:run
# 后端运行在 http://localhost:8080
```

**2. 启动前端**（需要 Node 18+）
```bash
cd frontend
npm install
npm run dev
# 前端运行在 http://localhost:5173 ，已配置代理到后端
```

#### 方式三：Docker 一键启动（只需 Docker）

```bash
# 默认使用内嵌 H2
docker compose up -d --build
# 访问 http://localhost:8080

# 如需使用 MySQL：
SPRING_PROFILES_ACTIVE=mysql docker compose --profile mysql up -d --build
```

---

### 📦 一键打包完整可运行 Jar

打包会先构建前端并内置到后端，最终生成单一可运行 Jar：

```bash
# 1) 构建前端（产物自动输出到后端静态目录）
cd frontend && npm install && npm run build

# 2) 打包后端（含前端）
cd ../backend && mvn clean package -DskipTests

# 3) 运行
java -jar target/hrms.jar
```

Windows 用户可直接双击运行根目录下的 `build.bat`（构建）与 `run.bat`（运行）。

---

### 🔑 默认账号

| 角色 | 用户名 | 密码 | 说明 |
| --- | --- | --- | --- |
| 管理员 | `admin` | `admin123` | 系统管理员，拥有所有权限 |
| 人事专员 | `hr` | `hr123456` | 人事专员，可管理员工/考勤/工资等 |
| 员工（张三） | `E001` | `123456` | 技术部，Java开发工程师 |
| 员工（李四…孙七） | `E002` ~ `E005` | `123456` | 各部门普通员工 |

> 首次启动会自动初始化 3 个部门、5 名员工及演示考勤/请假/加班数据。
> 新增员工时会自动创建登录账号：**用户名 = 工号，初始密码 = 123456**。

---

### 🗄️ 数据库说明

- **默认（H2）**：零配置，数据保存在 `backend/data/hrms.mv.db`。可访问 `http://localhost:8080/h2-console` 查看（JDBC URL：`jdbc:h2:file:./data/hrms`，用户名 `sa`，密码空）。
- **切换 MySQL**：以 `mysql` profile 启动即可，程序会自动建库建表并灌入演示数据。

```bash
# 通过环境变量覆盖连接信息（含默认值）
#   MYSQL_HOST=localhost MYSQL_PORT=3306 MYSQL_DB=hrms MYSQL_USER=root MYSQL_PASSWORD=root
java -jar target/hrms.jar --spring.profiles.active=mysql \
     --MYSQL_HOST=localhost --MYSQL_USER=root --MYSQL_PASSWORD=你的密码
```

建表脚本见 `backend/src/main/resources/db/schema.sql`（H2 / MySQL 通用），
另附独立参考脚本 `sql/init_mysql.sql`。

---

### 🧮 薪资核算规则

```
应发工资 = 基本工资 + 全勤奖 + 加班费 − 迟到扣款 − 缺勤扣款 − 事假扣款
社保扣除 = 基本工资 × 社保比例（默认 10.5%）
公积金扣除 = 基本工资 × 公积金比例（默认 7%）
应纳税所得额 = 应发工资 − 社保扣除 − 公积金扣除 − 专项附加扣除 − 个税起征点（默认 5000）
个人所得税 = 应纳税所得额 × 3%（应纳税所得额 ≤ 0 时为 0）
实发工资 = 应发工资 − 社保扣除 − 公积金扣除 − 个人所得税
```
- 全勤奖：当月无迟到 / 早退 / 缺勤 / 请假才发放。
- 加班费：仅统计**已批准**的加班；事假扣款仅统计**已批准**的事假。
- 社保、公积金按基本工资为基数计算，比例可在「薪资规则」页面配置。
- 专项附加扣除：子女教育、赡养老人等，可在「薪资规则」页面统一配置默认值。
- 各项参数可在「薪资规则」页面自定义。

---

### 📁 项目结构

```
attendance-salary-system/
├── backend/                      # Spring Boot 后端
│   ├── src/main/java/com/example/hrms/
│   │   ├── config/               # 安全、MyBatis-Plus、数据初始化配置
│   │   ├── security/             # JWT 与 Spring Security
│   │   ├── entity/ mapper/ service/ controller/   # 分层代码
│   │   └── common/               # 统一返回、异常处理
│   └── src/main/resources/
│       ├── application.yml        # H2 / MySQL 双 profile 配置
│       ├── db/schema.sql          # 建表脚本
│       └── static/                # 前端打包产物（构建后生成）
├── frontend/                     # Vue 3 前端
│   └── src/{api,router,store,layout,views}
├── sql/init_mysql.sql            # MySQL 参考脚本
├── Dockerfile / docker-compose.yml
├── build.bat / run.bat           # Windows 便捷脚本
└── README.md                     # 双语文档（中文+英文）
```

---

### 📡 主要接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录获取 token |
| GET | `/api/dashboard/stat` | 首页统计 |
| GET/POST/PUT/DELETE | `/api/employees` | 员工管理 |
| GET | `/api/departments` | 部门 |
| POST | `/api/attendance/check-in` `/check-out` | 上/下班打卡 |
| GET/POST/PUT | `/api/leaves` | 请假与审批 |
| GET/POST/PUT | `/api/overtimes` | 加班与审批 |
| GET/PUT | `/api/salary-rule` | 薪资规则（含社保/公积金/个税配置） |
| POST | `/api/payrolls/generate?month=YYYY-MM` | 生成/重算工资单 |
| GET | `/api/payrolls` | 工资单查询 |
| GET | `/api/payrolls/export` | 工资单 Excel 导出 |
| GET | `/api/system/monitor` | 系统监控信息（仅管理员） |
| GET | `/api/reports/employee` | 人员统计报表（仅管理员/人事） |
| GET | `/api/reports/attendance?month=YYYY-MM` | 考勤统计报表（仅管理员/人事） |
| GET | `/api/reports/salary?month=YYYY-MM` | 薪资成本报表（仅管理员/人事） |
| GET | `/api/workflow/processes` | 工作流流程定义 |
| POST | `/api/workflow/start` | 发起工作流 |
| PUT | `/api/workflow/tasks/{id}/approve` | 审批工作流任务 |
| GET | `/api/workflow/tasks/pending` | 待我审批的任务 |
| GET | `/api/workflow/tasks/approved` | 我已审批的任务 |
| GET | `/api/workflow/instances/my` | 我发起的流程 |
| GET | `/api/workflow/instances/{id}/history` | 审批历史 |
| GET/POST/PUT/DELETE | `/api/tenants` | 租户管理（仅管理员） |
| GET | `/api/operation-logs` | 操作日志（仅管理员） |

> 除登录接口外，所有 `/api/**` 需在请求头携带 `Authorization: Bearer <token>`。

---

### 📄 License

本项目仅用于学习与毕业设计演示，可自由使用与修改。

**[⬆️ 返回顶部](#中小企业员工考勤与薪资核算管理系统) | [🇬🇧 Switch to English](#-english)**

---
---

<a id="-english"></a>

## 🇬🇧 English

> A frontend-backend separated project based on **Spring Boot + Vue 3**, implementing employee management, attendance clock-in, leave/overtime approval, salary rule configuration, and automatic payroll calculation.
>
> **Out-of-the-box**: Uses embedded H2 database by default, no MySQL installation required, ready to use upon startup; also supports one-click switching to MySQL.

**[⬆️ Back to Top](#中小企业员工考勤与薪资核算管理系统) | [🇨🇳 切换到中文](#-中文版)**

---

### 🖼️ Screenshots

| Login | Dashboard (Admin) |
| :---: | :---: |
| ![Login](docs/screenshots/01-login.png) | ![Dashboard](docs/screenshots/02-dashboard.png) |

| Employee Management | Payroll |
| :---: | :---: |
| ![Employee](docs/screenshots/03-employee.png) | ![Payroll](docs/screenshots/08-payroll.png) |

| Attendance (List) | Attendance (Calendar) |
| :---: | :---: |
| ![Attendance List](docs/screenshots/05-attendance.png) | ![Attendance Calendar](docs/screenshots/10-attendance-calendar.png) |

| Department Management | Salary Rules |
| :---: | :---: |
| ![Department](docs/screenshots/04-department.png) | ![Salary Rules](docs/screenshots/09-salary-rule.png) |

| Leave Management | Overtime Management |
| :---: | :---: |
| ![Leave](docs/screenshots/06-leave.png) | ![Overtime](docs/screenshots/07-overtime.png) |

| System Monitor | Attendance (Calendar) |
| :---: | :---: |
| ![System Monitor](docs/screenshots/11-system-monitor.png) | ![Attendance Calendar](docs/screenshots/10-attendance-calendar.png) |

---

### 📝 Changelog

#### v1.12.0 - 2026-08-11
**Tenant Data Isolation**
- 🔒 Automatic tenant isolation based on MyBatis-Plus TenantLineInnerInterceptor
- 📊 All business tables (employee/department/attendance/leave/overtime/payroll/workflow/log/notification) added tenant_id field
- 🎯 JWT filter automatically sets tenant context, clears after request ends
- 🚫 sys_user and tenant tables ignore tenant isolation (login and tenant management need cross-tenant access)
- ✅ Data completely isolated between tenants, admins can only see their own tenant data

#### v1.11.0 - 2026-08-11
**Data Backup Feature**
- 💾 New data backup page (admin only)
- 📦 Secure database backup using H2 BACKUP command (works while running)
- 📋 Backup file list display (filename/size/backup time)
- ⬇️ Support downloading backup files (ZIP format)
- 🗑️ Support deleting backup files
- 📁 Backup files saved in backup directory

#### v1.10.0 - 2026-08-11
**Notification Feature**
- 🔔 New notification center page
- 📬 Supports 4 types: system, approval, attendance, payroll notifications
- 🔴 Top bar notification icon + unread count badge (auto refresh every 30s)
- ✅ Click message to mark as read, support mark all as read
- 🔍 Support filtering by unread/read status
- 🎨 Gradient notification icons, different colors for different types

#### v1.9.0 - 2026-08-11
**Operation Log Feature**
- 📋 New operation log page (admin only)
- 🔍 AOP aspect automatically records all write operations (create/update/delete)
- 👤 Records operator, module, operation type, request params, IP address
- ⏱️ Records operation duration, status (success/failure), error message
- 🔎 Supports multi-condition filtering by module, operator, status

#### v1.8.0 - 2026-08-11
**Multi-tenant Support**
- 🏢 New tenant (enterprise) management feature (admin only)
- 👥 Added tenant_id field to user table, supports user-tenant association
- 🔐 Login API returns tenant ID and tenant name
- 📊 Frontend user state stores tenant information
- 🔧 Supports multiple enterprises sharing one system, foundation for future data isolation

#### v1.7.0 - 2026-08-11
**Workflow Engine**
- 🔄 New workflow approval center page
- 📝 Supports multi-level approval process definition (JSON node configuration)
- ✅ Four views: Start Process, Pending My Approval, My Approved, I Started
- 📜 Approval history timeline display, supports viewing each level's approval comments
- 👥 Pre-configured leave approval process (HR → Admin two-level), overtime approval process (HR one-level)
- 🔐 Matches approvers by role, automatically flows to next level

#### v1.6.0 - 2026-08-11
**Statistics Report Feature**
- 📊 New statistics report page (admin/HR only)
- 👥 Employee stats: total employees, department distribution, gender ratio, salary range distribution, employee status
- 📅 Attendance stats: total check-ins, attendance status distribution, department comparison, attendance rate
- 💰 Salary cost: total cost stats, department salary comparison, salary composition analysis, average net salary
- 📈 ECharts visualization: pie charts + bar charts + detail tables

#### v1.5.0 - 2026-08-11
**System Monitor Feature**
- 🖥️ New system monitor page (admin only)
- 📊 Real-time display of OS, JVM, disk, application runtime info
- ⏱️ Auto-refresh every 10 seconds, supports manual refresh
- 📈 Memory/disk usage progress bars, color changes with usage (green/orange/red)
- 🔐 Strict permission control, HR/employee accounts cannot access

#### v1.4.0 - 2026-08-11
**Payroll Calculation Enhancement**
- 💰 Added social security deduction (base salary × rate, default 10.5%)
- 🏠 Added housing fund deduction (base salary × rate, default 7%)
- 🧾 Added special additional deduction config (children education, elderly support, etc.)
- 📊 Configurable tax threshold (default 5000 CNY)
- 🧮 Improved tax calculation: taxable income = gross − social security − housing fund − special deduction − threshold
- 📋 Added social security, housing fund columns to payroll page
- 📤 Excel export added 3 columns (social security/housing fund/special deduction)
- ⚙️ Salary rules page grouped into three config sections, full calculation rules shown at bottom

#### v1.3.0 - 2026-08-11
**UI Beautification & Global Style Unification**
- 🎨 Brand new purple theme design, login page gradient background + decorative elements
- 📊 Dashboard optimization: gradient stat cards + ECharts purple theme charts
- 🔧 Global style unification: cards, tables, buttons, dialogs, pagination consistent
- 👤 Main layout optimization: top bar page title + gradient user avatar + role tag
- 📱 Left menu beautification: selected item gradient background + shadow effect
- 💾 All buttons gradient color + hover lift animation

#### v1.2.0 - 2026-08-10
**Feature Enhancement**
- 📅 New attendance calendar view, intuitive monthly attendance status
- 📊 Dashboard added ECharts charts (department headcount distribution / salary bar chart)
- 🔐 Added change password feature
- 📤 Payroll supports Excel export
- 👥 Added HR role, refined permission control
- 🌳 Department management supports tree structure + headcount stats
- 📥 Employee/attendance supports Excel batch import

#### v1.1.0 - 2026-08-09
**Basic Feature Completion**
- ✅ Employee management CRUD
- ✅ Department management
- ✅ Attendance clock-in and statistics
- ✅ Leave/overtime application and approval
- ✅ Salary rule configuration
- ✅ Automatic payroll calculation
- ✅ JWT login authentication + three-level roles

---

### ✨ Features

| Module | Features |
| --- | --- |
| Login & Auth | JWT login, admin/HR/employee three-level roles, API permission control, change password, **tenant info return** |
| Dashboard | Admin sees global stats + ECharts charts; employee sees monthly attendance stats |
| Employee Mgmt | Employee CRUD, search by department/keyword, auto-generate login account for new employees, **Excel batch import** |
| Department Mgmt | **Tree department structure**, department headcount stats, multi-level departments, sort/status management |
| Attendance Mgmt | Employee clock-in/out, auto-detect late/early leave, admin manual entry & query, **calendar view**, **Excel batch import** |
| Leave Mgmt | Employee submit leave application, admin/HR approval (approve/reject) |
| Overtime Mgmt | Employee submit overtime application, admin/HR approval |
| Salary Rules | Configure late/absence/personal leave deduction, overtime rate, full attendance bonus, **social security rate, housing fund rate, tax threshold, special additional deduction** |
| Payroll | Auto-generate monthly payroll: base + full attendance bonus + overtime − deductions − **social security − housing fund − tax**, supports recalculate & pay, **Excel export** |
| **Statistics Reports** | **Employee report (department/gender/salary range), attendance report, salary cost report, ECharts visualization (admin/HR only)** |
| **Workflow Engine** | **Multi-level approval process definition, start process, pending/approved/started, approval history timeline, supports HR→Admin two-level approval** |
| **Multi-tenant Support** | **Tenant (enterprise) management, user-tenant association, login returns tenant info, supports multi-enterprise sharing (admin only)** |
| **Operation Log** | **AOP auto-record all write operations, operator/module/IP/duration/status, supports filtering by module/operator/status (admin only)** |
| System Monitor | **Real-time system monitoring**: OS/JVM/disk/app info, memory usage progress bar, auto-refresh (admin only) |
| Data Import/Export | Employee Excel import, attendance Excel import, payroll Excel export |

---

### 🧰 Tech Stack

**Backend**: Spring Boot 2.7 · Spring Security + JWT · MyBatis-Plus · H2 / MySQL 8 · Apache POI · Java 8
**Frontend**: Vue 3 · Vite 5 · Element Plus · Vue Router · Pinia · Axios · ECharts 5

---

### 🚀 Quick Start (Choose One)

> System requirements: any one of the following will work. For maximum compatibility, backend targets **Java 8**, runs on all major operating systems.

#### Method 1: Run Packaged Jar Directly (Simplest, only needs JDK 8+)

```bash
# After building in backend directory (needs Maven first, see Method 2), get target/hrms.jar
java -jar backend/target/hrms.jar
```

After startup, visit **http://localhost:8080** in browser for the complete system (frontend is bundled in the Jar).

#### Method 2: Local Development (Frontend-Backend Separated)

**1. Start Backend** (needs JDK 8 + Maven)
```bash
cd backend
mvn spring-boot:run
# Backend runs at http://localhost:8080
```

**2. Start Frontend** (needs Node 18+)
```bash
cd frontend
npm install
npm run dev
# Frontend runs at http://localhost:5173, proxy configured to backend
```

#### Method 3: Docker One-Click Start (only needs Docker)

```bash
# Default uses embedded H2
docker compose up -d --build
# Visit http://localhost:8080

# To use MySQL:
SPRING_PROFILES_ACTIVE=mysql docker compose --profile mysql up -d --build
```

---

### 📦 One-Click Build Complete Runnable Jar

Build will first compile frontend and bundle into backend, finally generate a single runnable Jar:

```bash
# 1) Build frontend (output auto-sent to backend static directory)
cd frontend && npm install && npm run build

# 2) Package backend (includes frontend)
cd ../backend && mvn clean package -DskipTests

# 3) Run
java -jar target/hrms.jar
```

Windows users can double-click `build.bat` (build) and `run.bat` (run) in root directory.

---

### 🔑 Default Accounts

| Role | Username | Password | Description |
| --- | --- | --- | --- |
| Admin | `admin` | `admin123` | System administrator, full permissions |
| HR | `hr` | `hr123456` | HR specialist, manages employees/attendance/payroll |
| Employee (Zhang San) | `E001` | `123456` | Tech Dept, Java Developer |
| Employee (Li Si...Sun Qi) | `E002` ~ `E005` | `123456` | Regular employees in various departments |

> First startup auto-initializes 3 departments, 5 employees, and demo attendance/leave/overtime data.
> New employees auto-create login accounts: **username = employee ID, initial password = 123456**.

---

### 🗄️ Database Info

- **Default (H2)**: Zero config, data saved at `backend/data/hrms.mv.db`. Can view at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/hrms`, username `sa`, empty password).
- **Switch to MySQL**: Start with `mysql` profile, program will auto-create database/tables and insert demo data.

```bash
# Override connection info via environment variables (with defaults)
#   MYSQL_HOST=localhost MYSQL_PORT=3306 MYSQL_DB=hrms MYSQL_USER=root MYSQL_PASSWORD=root
java -jar target/hrms.jar --spring.profiles.active=mysql \
     --MYSQL_HOST=localhost --MYSQL_USER=root --MYSQL_PASSWORD=your_password
```

Schema script at `backend/src/main/resources/db/schema.sql` (H2/MySQL compatible),
also includes standalone reference script `sql/init_mysql.sql`.

---

### 🧮 Payroll Calculation Rules

```
Gross Salary = Base Salary + Full Attendance Bonus + Overtime Pay − Late Deduction − Absence Deduction − Personal Leave Deduction
Social Security Deduction = Base Salary × Social Security Rate (default 10.5%)
Housing Fund Deduction = Base Salary × Housing Fund Rate (default 7%)
Taxable Income = Gross Salary − Social Security Deduction − Housing Fund Deduction − Special Additional Deduction − Tax Threshold (default 5000)
Personal Income Tax = Taxable Income × 3% (0 when taxable income ≤ 0)
Net Salary = Gross Salary − Social Security Deduction − Housing Fund Deduction − Personal Income Tax
```
- Full attendance bonus: only paid when no late/early leave/absence/leave in the month.
- Overtime pay: only counts **approved** overtime; personal leave deduction only counts **approved** personal leave.
- Social security and housing fund calculated based on base salary, rates configurable in "Salary Rules" page.
- Special additional deduction: children education, elderly support, etc., configurable default value in "Salary Rules" page.
- All parameters customizable in "Salary Rules" page.

---

### 📁 Project Structure

```
attendance-salary-system/
├── backend/                      # Spring Boot backend
│   ├── src/main/java/com/example/hrms/
│   │   ├── config/               # Security, MyBatis-Plus, data init config
│   │   ├── security/             # JWT and Spring Security
│   │   ├── entity/ mapper/ service/ controller/   # Layered code
│   │   └── common/               # Unified response, exception handling
│   └── src/main/resources/
│       ├── application.yml        # H2 / MySQL dual profile config
│       ├── db/schema.sql          # Schema script
│       └── static/                # Frontend build output (generated after build)
├── frontend/                     # Vue 3 frontend
│   └── src/{api,router,store,layout,views}
├── sql/init_mysql.sql            # MySQL reference script
├── Dockerfile / docker-compose.yml
├── build.bat / run.bat           # Windows convenience scripts
└── README.md                     # Bilingual documentation (Chinese + English)
```

---

### 📡 Main APIs

| Method | Path | Description |
| --- | --- | --- |
| POST | `/api/auth/login` | Login to get token |
| GET | `/api/dashboard/stat` | Dashboard statistics |
| GET/POST/PUT/DELETE | `/api/employees` | Employee management |
| GET | `/api/departments` | Departments |
| POST | `/api/attendance/check-in` `/check-out` | Clock in/out |
| GET/POST/PUT | `/api/leaves` | Leave and approval |
| GET/POST/PUT | `/api/overtimes` | Overtime and approval |
| GET/PUT | `/api/salary-rule` | Salary rules (incl. social security/housing fund/tax config) |
| POST | `/api/payrolls/generate?month=YYYY-MM` | Generate/recalculate payroll |
| GET | `/api/payrolls` | Payroll query |
| GET | `/api/payrolls/export` | Payroll Excel export |
| GET | `/api/system/monitor` | System monitor info (admin only) |
| GET | `/api/reports/employee` | Employee statistics report (admin/HR only) |
| GET | `/api/reports/attendance?month=YYYY-MM` | Attendance statistics report (admin/HR only) |
| GET | `/api/reports/salary?month=YYYY-MM` | Salary cost report (admin/HR only) |
| GET | `/api/workflow/processes` | Workflow process definitions |
| POST | `/api/workflow/start` | Start workflow process |
| PUT | `/api/workflow/tasks/{id}/approve` | Approve workflow task |
| GET | `/api/workflow/tasks/pending` | My pending tasks |
| GET | `/api/workflow/tasks/approved` | My approved tasks |
| GET | `/api/workflow/instances/my` | Processes I started |
| GET | `/api/workflow/instances/{id}/history` | Approval history |
| GET/POST/PUT/DELETE | `/api/tenants` | Tenant management (admin only) |
| GET | `/api/operation-logs` | Operation logs (admin only) |

> Except login API, all `/api/**` require `Authorization: Bearer <token>` in request header.

---

### 📄 License

This project is for educational and graduation project demonstration purposes only, free to use and modify.

**[⬆️ Back to Top](#中小企业员工考勤与薪资核算管理系统) | [🇨🇳 切换到中文](#-中文版)**

# Employee Attendance and Payroll Management System for SMEs

![Java](https://img.shields.io/badge/Java-8-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-6DB33F?logo=springboot&logoColor=white)
![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs&logoColor=white)
![Element Plus](https://img.shields.io/badge/Element%20Plus-2.8-409EFF?logo=element&logoColor=white)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5-red)
![Database](https://img.shields.io/badge/DB-H2%20%7C%20MySQL8-blue)
![License](https://img.shields.io/badge/License-Educational%20Use-lightgrey)

> A frontend-backend separated project based on **Spring Boot + Vue 3**, implementing employee management, attendance clock-in, leave/overtime approval, salary rule configuration, and automatic payroll calculation.
>
> **Out-of-the-box**: Uses embedded H2 database by default, no MySQL installation required, ready to use upon startup; also supports one-click switching to MySQL.

[中文版 README](README.md)

---

## 🖼️ Screenshots

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

## 📝 Changelog

### v1.9.0 - 2026-08-11
**Operation Log Feature**
- 📋 New operation log page (admin only)
- 🔍 AOP aspect automatically records all write operations (create/update/delete)
- 👤 Records operator, module, operation type, request params, IP address
- ⏱️ Records operation duration, status (success/failure), error message
- 🔎 Supports multi-condition filtering by module, operator, status

### v1.8.0 - 2026-08-11
**Multi-tenant Support**
- 🏢 New tenant (enterprise) management feature (admin only)
- 👥 Added tenant_id field to user table, supports user-tenant association
- 🔐 Login API returns tenant ID and tenant name
- 📊 Frontend user state stores tenant information
- 🔧 Supports multiple enterprises sharing one system, foundation for future data isolation

### v1.7.0 - 2026-08-11
**Workflow Engine**
- 🔄 New workflow approval center page
- 📝 Supports multi-level approval process definition (JSON node configuration)
- ✅ Four views: Start Process, Pending My Approval, My Approved, I Started
- 📜 Approval history timeline display, supports viewing each level's approval comments
- 👥 Pre-configured leave approval process (HR → Admin two-level), overtime approval process (HR one-level)
- 🔐 Matches approvers by role, automatically flows to next level

### v1.6.0 - 2026-08-11
**Statistics Report Feature**
- 📊 New statistics report page (admin/HR only)
- 👥 Employee stats: total employees, department distribution, gender ratio, salary range distribution, employee status
- 📅 Attendance stats: total check-ins, attendance status distribution, department comparison, attendance rate
- 💰 Salary cost: total cost stats, department salary comparison, salary composition analysis, average net salary
- 📈 ECharts visualization: pie charts + bar charts + detail tables

### v1.5.0 - 2026-08-11
**System Monitor Feature**
- 🖥️ New system monitor page (admin only)
- 📊 Real-time display of OS, JVM, disk, application runtime info
- ⏱️ Auto-refresh every 10 seconds, supports manual refresh
- 📈 Memory/disk usage progress bars, color changes with usage (green/orange/red)
- 🔐 Strict permission control, HR/employee accounts cannot access

### v1.4.0 - 2026-08-11
**Payroll Calculation Enhancement**
- 💰 Added social security deduction (base salary × rate, default 10.5%)
- 🏠 Added housing fund deduction (base salary × rate, default 7%)
- 🧾 Added special additional deduction config (children education, elderly support, etc.)
- 📊 Configurable tax threshold (default 5000 CNY)
- 🧮 Improved tax calculation: taxable income = gross − social security − housing fund − special deduction − threshold
- 📋 Added social security, housing fund columns to payroll page
- 📤 Excel export added 3 columns (social security/housing fund/special deduction)
- ⚙️ Salary rules page grouped into three config sections, full calculation rules shown at bottom

### v1.3.0 - 2026-08-11
**UI Beautification & Global Style Unification**
- 🎨 Brand new purple theme design, login page gradient background + decorative elements
- 📊 Dashboard optimization: gradient stat cards + ECharts purple theme charts
- 🔧 Global style unification: cards, tables, buttons, dialogs, pagination consistent
- 👤 Main layout optimization: top bar page title + gradient user avatar + role tag
- 📱 Left menu beautification: selected item gradient background + shadow effect
- 💾 All buttons gradient color + hover lift animation

### v1.2.0 - 2026-08-10
**Feature Enhancement**
- 📅 New attendance calendar view, intuitive monthly attendance status
- 📊 Dashboard added ECharts charts (department headcount distribution / salary bar chart)
- 🔐 Added change password feature
- 📤 Payroll supports Excel export
- 👥 Added HR role, refined permission control
- 🌳 Department management supports tree structure + headcount stats
- 📥 Employee/attendance supports Excel batch import

### v1.1.0 - 2026-08-09
**Basic Feature Completion**
- ✅ Employee management CRUD
- ✅ Department management
- ✅ Attendance clock-in and statistics
- ✅ Leave/overtime application and approval
- ✅ Salary rule configuration
- ✅ Automatic payroll calculation
- ✅ JWT login authentication + three-level roles

---

## ✨ Features

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

## 🧰 Tech Stack

**Backend**: Spring Boot 2.7 · Spring Security + JWT · MyBatis-Plus · H2 / MySQL 8 · Apache POI · Java 8
**Frontend**: Vue 3 · Vite 5 · Element Plus · Vue Router · Pinia · Axios · ECharts 5

---

## 🚀 Quick Start (Choose One)

> System requirements: any one of the following will work. For maximum compatibility, backend targets **Java 8**, runs on all major operating systems.

### Method 1: Run Packaged Jar Directly (Simplest, only needs JDK 8+)

```bash
# After building in backend directory (needs Maven first, see Method 2), get target/hrms.jar
java -jar backend/target/hrms.jar
```

After startup, visit **http://localhost:8080** in browser for the complete system (frontend is bundled in the Jar).

### Method 2: Local Development (Frontend-Backend Separated)

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

### Method 3: Docker One-Click Start (only needs Docker)

```bash
# Default uses embedded H2
docker compose up -d --build
# Visit http://localhost:8080

# To use MySQL:
SPRING_PROFILES_ACTIVE=mysql docker compose --profile mysql up -d --build
```

---

## 📦 One-Click Build Complete Runnable Jar

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

## 🔑 Default Accounts

| Role | Username | Password | Description |
| --- | --- | --- | --- |
| Admin | `admin` | `admin123` | System administrator, full permissions |
| HR | `hr` | `hr123456` | HR specialist, manages employees/attendance/payroll |
| Employee (Zhang San) | `E001` | `123456` | Tech Dept, Java Developer |
| Employee (Li Si...Sun Qi) | `E002` ~ `E005` | `123456` | Regular employees in various departments |

> First startup auto-initializes 3 departments, 5 employees, and demo attendance/leave/overtime data.
> New employees auto-create login accounts: **username = employee ID, initial password = 123456**.

---

## 🗄️ Database Info

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

## 🧮 Payroll Calculation Rules

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

## 📁 Project Structure

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
├── README.md                     # Chinese documentation
└── README-en.md                  # English documentation (this file)
```

---

## 📡 Main APIs

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

## 📄 License

This project is for educational and graduation project demonstration purposes only, free to use and modify.

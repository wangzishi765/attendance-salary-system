package com.example.hrms.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hrms.entity.*;
import com.example.hrms.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * 演示数据初始化：仅在数据库为空时执行一次，保证开箱即用。
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final DepartmentMapper departmentMapper;
    private final EmployeeMapper employeeMapper;
    private final AttendanceMapper attendanceMapper;
    private final LeaveRecordMapper leaveRecordMapper;
    private final OvertimeRecordMapper overtimeRecordMapper;
    private final SalaryRuleMapper salaryRuleMapper;
    private final WorkflowProcessMapper workflowProcessMapper;
    private final TenantMapper tenantMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Long userCount = sysUserMapper.selectCount(new LambdaQueryWrapper<>());
        if (userCount != null && userCount > 0) {
            return; // 已初始化，跳过
        }
        System.out.println(">>> 首次启动，初始化演示数据...");

        // 0. 默认租户
        Tenant defaultTenant = new Tenant();
        defaultTenant.setTenantCode("DEFAULT");
        defaultTenant.setTenantName("默认企业");
        defaultTenant.setContactPerson("系统管理员");
        defaultTenant.setContactPhone("400-000-0000");
        defaultTenant.setAddress("四川省成都市");
        defaultTenant.setStatus("ACTIVE");
        tenantMapper.insert(defaultTenant);

        // 1. 部门
        Department d1 = dept("技术部", "负责产品研发与技术支持");
        Department d2 = dept("市场部", "负责市场推广与销售");
        Department d3 = dept("行政部", "负责人事与行政管理");

        // 2. 员工
        Employee e1 = emp("E001", "张三", "男", "13800000001", "zhangsan@demo.com", d1.getId(), "Java开发工程师", "12000", LocalDate.of(2023, 3, 1));
        Employee e2 = emp("E002", "李四", "男", "13800000002", "lisi@demo.com", d1.getId(), "前端开发工程师", "11000", LocalDate.of(2023, 6, 15));
        Employee e3 = emp("E003", "王五", "女", "13800000003", "wangwu@demo.com", d2.getId(), "市场专员", "8000", LocalDate.of(2024, 1, 10));
        Employee e4 = emp("E004", "赵六", "女", "13800000004", "zhaoliu@demo.com", d2.getId(), "销售经理", "15000", LocalDate.of(2022, 9, 1));
        Employee e5 = emp("E005", "孙七", "男", "13800000005", "sunqi@demo.com", d3.getId(), "行政专员", "7000", LocalDate.of(2024, 5, 20));

        // 3. 系统账号
        // 管理员
        user("admin", "admin123", "系统管理员", "ADMIN", null);
        // 人事专员
        user("hr", "hr123456", "人事专员", "HR", null);
        // 员工账号（用户名=工号，密码 123456）
        user(e1.getEmpNo(), "123456", e1.getName(), "EMPLOYEE", e1.getId());
        user(e2.getEmpNo(), "123456", e2.getName(), "EMPLOYEE", e2.getId());
        user(e3.getEmpNo(), "123456", e3.getName(), "EMPLOYEE", e3.getId());
        user(e4.getEmpNo(), "123456", e4.getName(), "EMPLOYEE", e4.getId());
        user(e5.getEmpNo(), "123456", e5.getName(), "EMPLOYEE", e5.getId());

        // 4. 薪资规则
        SalaryRule rule = new SalaryRule();
        rule.setName("默认薪资规则");
        rule.setLateDeduct(new BigDecimal("50"));
        rule.setAbsentDeduct(new BigDecimal("200"));
        rule.setLeaveDeduct(new BigDecimal("100"));
        rule.setOvertimeRate(new BigDecimal("30"));
        rule.setFullAttendanceBonus(new BigDecimal("300"));
        rule.setSocialSecurityRate(new BigDecimal("0.105"));  // 社保个人缴纳比例 10.5%
        rule.setHousingFundRate(new BigDecimal("0.07"));        // 公积金个人缴纳比例 7%
        rule.setTaxThreshold(new BigDecimal("5000"));           // 个税起征点
        rule.setSpecialDeduction(new BigDecimal("0"));          // 专项附加扣除默认值
        salaryRuleMapper.insert(rule);

        // 5. 演示考勤（本月前 5 个工作日，为张三、李四生成）
        LocalDate today = LocalDate.now();
        LocalDate cursor = today.withDayOfMonth(1);
        int workdays = 0;
        List<Employee> attEmps = Arrays.asList(e1, e2, e3);
        while (workdays < 6 && !cursor.isAfter(today)) {
            if (cursor.getDayOfWeek().getValue() <= 5) {
                for (Employee e : attEmps) {
                    boolean late = (workdays == 1 && e == e2); // 制造一次迟到
                    Attendance a = new Attendance();
                    a.setEmployeeId(e.getId());
                    a.setAttendDate(cursor);
                    a.setCheckInTime(LocalDateTime.of(cursor, late ? LocalTime.of(9, 20) : LocalTime.of(8, 55)));
                    a.setCheckOutTime(LocalDateTime.of(cursor, LocalTime.of(18, 10)));
                    a.setStatus(late ? "LATE" : "NORMAL");
                    a.setWorkHours(new BigDecimal("9.0"));
                    attendanceMapper.insert(a);
                }
                workdays++;
            }
            cursor = cursor.plusDays(1);
        }

        // 6. 演示请假（王五 事假1天，已批准）
        LeaveRecord lr = new LeaveRecord();
        lr.setEmployeeId(e3.getId());
        lr.setType("PERSONAL");
        lr.setStartDate(today.withDayOfMonth(Math.min(10, today.getDayOfMonth())));
        lr.setEndDate(today.withDayOfMonth(Math.min(10, today.getDayOfMonth())));
        lr.setDays(new BigDecimal("1"));
        lr.setReason("家中有事");
        lr.setStatus("APPROVED");
        lr.setApprover("系统管理员");
        leaveRecordMapper.insert(lr);

        // 7. 演示加班（张三 加班3小时，已批准）
        OvertimeRecord ot = new OvertimeRecord();
        ot.setEmployeeId(e1.getId());
        ot.setOvertimeDate(today.withDayOfMonth(Math.min(5, today.getDayOfMonth())));
        ot.setHours(new BigDecimal("3"));
        ot.setReason("项目上线加班");
        ot.setStatus("APPROVED");
        overtimeRecordMapper.insert(ot);

        // 8. 工作流流程定义
        // 请假审批流程：HR审批 → 管理员审批
        WorkflowProcess leaveProcess = new WorkflowProcess();
        leaveProcess.setProcessCode("LEAVE_APPROVAL");
        leaveProcess.setProcessName("请假审批流程");
        leaveProcess.setDescription("员工请假需经人事专员和管理员两级审批");
        leaveProcess.setNodesConfig("[{\"nodeName\":\"人事专员审批\",\"role\":\"HR\"},{\"nodeName\":\"管理员审批\",\"role\":\"ADMIN\"}]");
        leaveProcess.setStatus("ACTIVE");
        workflowProcessMapper.insert(leaveProcess);

        // 加班审批流程：HR审批
        WorkflowProcess overtimeProcess = new WorkflowProcess();
        overtimeProcess.setProcessCode("OVERTIME_APPROVAL");
        overtimeProcess.setProcessName("加班审批流程");
        overtimeProcess.setDescription("员工加班需经人事专员审批");
        overtimeProcess.setNodesConfig("[{\"nodeName\":\"人事专员审批\",\"role\":\"HR\"}]");
        overtimeProcess.setStatus("ACTIVE");
        workflowProcessMapper.insert(overtimeProcess);

        System.out.println(">>> 演示数据初始化完成。");
    }

    private Department dept(String name, String remark) {
        Department d = new Department();
        d.setName(name);
        d.setRemark(remark);
        departmentMapper.insert(d);
        return d;
    }

    private Employee emp(String no, String name, String gender, String phone, String email,
                         Long deptId, String position, String salary, LocalDate hire) {
        Employee e = new Employee();
        e.setEmpNo(no);
        e.setName(name);
        e.setGender(gender);
        e.setPhone(phone);
        e.setEmail(email);
        e.setDepartmentId(deptId);
        e.setPosition(position);
        e.setBaseSalary(new BigDecimal(salary));
        e.setHireDate(hire);
        e.setStatus("在职");
        employeeMapper.insert(e);
        return e;
    }

    private void user(String username, String rawPwd, String realName, String role, Long employeeId) {
        SysUser u = new SysUser();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPwd));
        u.setRealName(realName);
        u.setRole(role);
        u.setEmployeeId(employeeId);
        u.setTenantId(1L); // 默认租户
        u.setEnabled(1);
        sysUserMapper.insert(u);
    }
}

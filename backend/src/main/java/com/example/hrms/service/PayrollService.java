package com.example.hrms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.entity.*;
import com.example.hrms.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 薪资核算服务
 */
@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollMapper payrollMapper;
    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final AttendanceMapper attendanceMapper;
    private final LeaveRecordMapper leaveRecordMapper;
    private final OvertimeRecordMapper overtimeRecordMapper;
    private final SalaryRuleService salaryRuleService;

    /** 个税起征点 */
    private static final BigDecimal TAX_THRESHOLD = new BigDecimal("5000");
    /** 简化个税税率 3% */
    private static final BigDecimal TAX_RATE = new BigDecimal("0.03");

    /**
     * 生成/重算某月全部在职员工的工资单
     */
    @Transactional
    public int generateMonth(String month) {
        SalaryRule rule = salaryRuleService.current();
        List<Employee> employees = employeeMapper.selectList(
                new LambdaQueryWrapper<Employee>().eq(Employee::getStatus, "在职"));
        LocalDate start = LocalDate.parse(month + "-01");
        LocalDate end = start.plusMonths(1);

        int count = 0;
        for (Employee emp : employees) {
            // 删除该员工该月已有工资单（支持重算）
            payrollMapper.delete(new LambdaQueryWrapper<Payroll>()
                    .eq(Payroll::getEmployeeId, emp.getId())
                    .eq(Payroll::getSalaryMonth, month));

            Payroll payroll = calculate(emp, month, start, end, rule);
            payrollMapper.insert(payroll);
            count++;
        }
        return count;
    }

    private Payroll calculate(Employee emp, String month, LocalDate start, LocalDate end, SalaryRule rule) {
        // 考勤
        List<Attendance> attList = attendanceMapper.selectList(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getEmployeeId, emp.getId())
                .ge(Attendance::getAttendDate, start)
                .lt(Attendance::getAttendDate, end));
        long lateCount = attList.stream().filter(a -> "LATE".equals(a.getStatus())).count();
        long earlyCount = attList.stream().filter(a -> "EARLY".equals(a.getStatus())).count();
        long absentCount = attList.stream().filter(a -> "ABSENT".equals(a.getStatus())).count();

        // 已批准的事假天数
        List<LeaveRecord> leaves = leaveRecordMapper.selectList(new LambdaQueryWrapper<LeaveRecord>()
                .eq(LeaveRecord::getEmployeeId, emp.getId())
                .eq(LeaveRecord::getStatus, "APPROVED")
                .ge(LeaveRecord::getStartDate, start)
                .lt(LeaveRecord::getStartDate, end));
        BigDecimal personalLeaveDays = leaves.stream()
                .filter(l -> "PERSONAL".equals(l.getType()))
                .map(LeaveRecord::getDays)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long leaveTotal = leaves.size();

        // 已批准的加班小时
        List<OvertimeRecord> overtimes = overtimeRecordMapper.selectList(new LambdaQueryWrapper<OvertimeRecord>()
                .eq(OvertimeRecord::getEmployeeId, emp.getId())
                .eq(OvertimeRecord::getStatus, "APPROVED")
                .ge(OvertimeRecord::getOvertimeDate, start)
                .lt(OvertimeRecord::getOvertimeDate, end));
        BigDecimal overtimeHours = overtimes.stream()
                .map(OvertimeRecord::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal base = emp.getBaseSalary() == null ? BigDecimal.ZERO : emp.getBaseSalary();
        BigDecimal lateDeduct = rule.getLateDeduct().multiply(BigDecimal.valueOf(lateCount));
        BigDecimal absentDeduct = rule.getAbsentDeduct().multiply(BigDecimal.valueOf(absentCount));
        BigDecimal leaveDeduct = rule.getLeaveDeduct().multiply(personalLeaveDays);
        BigDecimal overtimePay = rule.getOvertimeRate().multiply(overtimeHours);

        // 全勤奖：无迟到/早退/缺勤/请假
        BigDecimal bonus = (lateCount == 0 && earlyCount == 0 && absentCount == 0 && leaveTotal == 0)
                ? rule.getFullAttendanceBonus() : BigDecimal.ZERO;

        BigDecimal gross = base.add(bonus).add(overtimePay)
                .subtract(lateDeduct).subtract(absentDeduct).subtract(leaveDeduct);
        if (gross.compareTo(BigDecimal.ZERO) < 0) {
            gross = BigDecimal.ZERO;
        }

        // 简化个税
        BigDecimal tax = BigDecimal.ZERO;
        if (gross.compareTo(TAX_THRESHOLD) > 0) {
            tax = gross.subtract(TAX_THRESHOLD).multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal net = gross.subtract(tax);

        Payroll p = new Payroll();
        p.setEmployeeId(emp.getId());
        p.setSalaryMonth(month);
        p.setBaseSalary(base);
        p.setAttendanceBonus(bonus);
        p.setOvertimePay(overtimePay);
        p.setLateDeduct(lateDeduct);
        p.setAbsentDeduct(absentDeduct);
        p.setLeaveDeduct(leaveDeduct);
        p.setOtherDeduct(BigDecimal.ZERO);
        p.setGrossSalary(gross);
        p.setTax(tax);
        p.setNetSalary(net);
        p.setStatus("GENERATED");
        p.setRemark(String.format("迟到%d次,早退%d次,缺勤%d天,事假%s天,加班%s小时",
                lateCount, earlyCount, absentCount,
                personalLeaveDays.stripTrailingZeros().toPlainString(),
                overtimeHours.stripTrailingZeros().toPlainString()));
        return p;
    }

    private void fillNames(List<Payroll> list) {
        Map<Long, Employee> empMap = employeeMapper.selectList(null).stream()
                .collect(Collectors.toMap(Employee::getId, e -> e, (a, b) -> a));
        Map<Long, String> deptMap = departmentMapper.selectList(null).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName, (a, b) -> a));
        for (Payroll p : list) {
            Employee e = empMap.get(p.getEmployeeId());
            if (e != null) {
                p.setEmployeeName(e.getName());
                p.setDepartmentName(deptMap.get(e.getDepartmentId()));
            }
        }
    }

    public Page<Payroll> page(long current, long size, Long employeeId, String month) {
        LambdaQueryWrapper<Payroll> wrapper = new LambdaQueryWrapper<>();
        if (employeeId != null) {
            wrapper.eq(Payroll::getEmployeeId, employeeId);
        }
        if (month != null && !month.isEmpty()) {
            wrapper.eq(Payroll::getSalaryMonth, month);
        }
        wrapper.orderByDesc(Payroll::getSalaryMonth).orderByAsc(Payroll::getEmployeeId);
        Page<Payroll> page = payrollMapper.selectPage(new Page<>(current, size), wrapper);
        fillNames(page.getRecords());
        return page;
    }

    public void markPaid(Long id) {
        Payroll p = payrollMapper.selectById(id);
        p.setStatus("PAID");
        payrollMapper.updateById(p);
    }

    public void delete(Long id) {
        payrollMapper.deleteById(id);
    }
}

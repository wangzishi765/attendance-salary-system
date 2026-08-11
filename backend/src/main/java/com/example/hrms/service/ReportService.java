package com.example.hrms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hrms.entity.*;
import com.example.hrms.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计报表服务
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final AttendanceMapper attendanceMapper;
    private final PayrollMapper payrollMapper;

    /**
     * 人员统计报表
     */
    public Map<String, Object> employeeReport() {
        Map<String, Object> result = new HashMap<>();

        // 总员工数
        Long total = employeeMapper.selectCount(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getStatus, "在职"));
        result.put("totalEmployees", total);

        // 部门人数分布
        List<Department> departments = departmentMapper.selectList(null);
        List<Map<String, Object>> deptDistribution = new ArrayList<>();
        for (Department dept : departments) {
            Long count = employeeMapper.selectCount(new LambdaQueryWrapper<Employee>()
                    .eq(Employee::getDepartmentId, dept.getId())
                    .eq(Employee::getStatus, "在职"));
            Map<String, Object> item = new HashMap<>();
            item.put("name", dept.getName());
            item.put("value", count);
            deptDistribution.add(item);
        }
        result.put("departmentDistribution", deptDistribution);

        // 性别分布
        Long maleCount = employeeMapper.selectCount(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getGender, "男")
                .eq(Employee::getStatus, "在职"));
        Long femaleCount = employeeMapper.selectCount(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getGender, "女")
                .eq(Employee::getStatus, "在职"));
        List<Map<String, Object>> genderDistribution = new ArrayList<>();
        Map<String, Object> male = new HashMap<>();
        male.put("name", "男");
        male.put("value", maleCount);
        genderDistribution.add(male);
        Map<String, Object> female = new HashMap<>();
        female.put("name", "女");
        female.put("value", femaleCount);
        genderDistribution.add(female);
        result.put("genderDistribution", genderDistribution);

        // 员工状态统计
        Long activeCount = employeeMapper.selectCount(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getStatus, "在职"));
        Long leaveCount = employeeMapper.selectCount(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getStatus, "离职"));
        Map<String, Object> statusStat = new HashMap<>();
        statusStat.put("active", activeCount);
        statusStat.put("leave", leaveCount);
        result.put("statusStat", statusStat);

        // 薪资区间分布
        List<Employee> employees = employeeMapper.selectList(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getStatus, "在职"));
        int salaryRange1 = 0; // <5000
        int salaryRange2 = 0; // 5000-10000
        int salaryRange3 = 0; // 10000-15000
        int salaryRange4 = 0; // >15000
        for (Employee emp : employees) {
            BigDecimal salary = emp.getBaseSalary();
            if (salary != null) {
                double s = salary.doubleValue();
                if (s < 5000) salaryRange1++;
                else if (s < 10000) salaryRange2++;
                else if (s < 15000) salaryRange3++;
                else salaryRange4++;
            }
        }
        List<Map<String, Object>> salaryDistribution = new ArrayList<>();
        String[] ranges = {"5000以下", "5000-10000", "10000-15000", "15000以上"};
        int[] counts = {salaryRange1, salaryRange2, salaryRange3, salaryRange4};
        for (int i = 0; i < ranges.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", ranges[i]);
            item.put("value", counts[i]);
            salaryDistribution.add(item);
        }
        result.put("salaryDistribution", salaryDistribution);

        return result;
    }

    /**
     * 考勤统计报表
     */
    public Map<String, Object> attendanceReport(String month) {
        Map<String, Object> result = new HashMap<>();

        // 查询该月所有考勤记录
        List<Attendance> attendances = attendanceMapper.selectList(new LambdaQueryWrapper<Attendance>()
                .like(Attendance::getAttendDate, month));

        // 总打卡天数
        result.put("totalCheckIns", attendances.size());

        // 状态统计
        int normal = 0, late = 0, early = 0, absent = 0;
        for (Attendance att : attendances) {
            String status = att.getStatus();
            if ("NORMAL".equals(status)) normal++;
            else if ("LATE".equals(status)) late++;
            else if ("EARLY".equals(status)) early++;
            else if ("ABSENT".equals(status)) absent++;
        }
        Map<String, Object> statusStat = new HashMap<>();
        statusStat.put("normal", normal);
        statusStat.put("late", late);
        statusStat.put("early", early);
        statusStat.put("absent", absent);
        result.put("statusStat", statusStat);

        // 各部门考勤统计
        List<Department> departments = departmentMapper.selectList(null);
        List<Map<String, Object>> deptAttendance = new ArrayList<>();
        for (Department dept : departments) {
            List<Employee> emps = employeeMapper.selectList(new LambdaQueryWrapper<Employee>()
                    .eq(Employee::getDepartmentId, dept.getId())
                    .eq(Employee::getStatus, "在职"));
            Set<Long> empIds = emps.stream().map(Employee::getId).collect(Collectors.toSet());

            int deptNormal = 0, deptLate = 0, deptAbsent = 0;
            for (Attendance att : attendances) {
                if (empIds.contains(att.getEmployeeId())) {
                    String status = att.getStatus();
                    if ("NORMAL".equals(status)) deptNormal++;
                    else if ("LATE".equals(status)) deptLate++;
                    else if ("ABSENT".equals(status)) deptAbsent++;
                }
            }
            Map<String, Object> item = new HashMap<>();
            item.put("department", dept.getName());
            item.put("normal", deptNormal);
            item.put("late", deptLate);
            item.put("absent", deptAbsent);
            item.put("total", emps.size());
            deptAttendance.add(item);
        }
        result.put("departmentAttendance", deptAttendance);

        // 出勤率
        double attendanceRate = attendances.size() > 0 ?
                (double) normal / attendances.size() * 100 : 0;
        result.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);

        return result;
    }

    /**
     * 薪资成本报表
     */
    public Map<String, Object> salaryReport(String month) {
        Map<String, Object> result = new HashMap<>();

        // 查询该月所有工资单
        List<Payroll> payrolls = payrollMapper.selectList(new LambdaQueryWrapper<Payroll>()
                .eq(Payroll::getSalaryMonth, month));

        // 总成本统计
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        BigDecimal totalSocial = BigDecimal.ZERO;
        BigDecimal totalFund = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        BigDecimal totalOvertime = BigDecimal.ZERO;
        BigDecimal totalBonus = BigDecimal.ZERO;

        for (Payroll p : payrolls) {
            totalGross = totalGross.add(p.getGrossSalary() != null ? p.getGrossSalary() : BigDecimal.ZERO);
            totalNet = totalNet.add(p.getNetSalary() != null ? p.getNetSalary() : BigDecimal.ZERO);
            totalSocial = totalSocial.add(p.getSocialSecurityDeduct() != null ? p.getSocialSecurityDeduct() : BigDecimal.ZERO);
            totalFund = totalFund.add(p.getHousingFundDeduct() != null ? p.getHousingFundDeduct() : BigDecimal.ZERO);
            totalTax = totalTax.add(p.getTax() != null ? p.getTax() : BigDecimal.ZERO);
            totalOvertime = totalOvertime.add(p.getOvertimePay() != null ? p.getOvertimePay() : BigDecimal.ZERO);
            totalBonus = totalBonus.add(p.getAttendanceBonus() != null ? p.getAttendanceBonus() : BigDecimal.ZERO);
        }

        Map<String, Object> costStat = new HashMap<>();
        costStat.put("totalGross", totalGross);
        costStat.put("totalNet", totalNet);
        costStat.put("totalSocial", totalSocial);
        costStat.put("totalFund", totalFund);
        costStat.put("totalTax", totalTax);
        costStat.put("totalOvertime", totalOvertime);
        costStat.put("totalBonus", totalBonus);
        costStat.put("employeeCount", payrolls.size());
        costStat.put("avgNet", payrolls.size() > 0 ? totalNet.divide(new BigDecimal(payrolls.size()), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
        result.put("costStat", costStat);

        // 各部门薪资对比
        List<Department> departments = departmentMapper.selectList(null);
        List<Map<String, Object>> deptSalary = new ArrayList<>();
        for (Department dept : departments) {
            List<Employee> emps = employeeMapper.selectList(new LambdaQueryWrapper<Employee>()
                    .eq(Employee::getDepartmentId, dept.getId()));
            Set<Long> empIds = emps.stream().map(Employee::getId).collect(Collectors.toSet());

            BigDecimal deptGross = BigDecimal.ZERO;
            BigDecimal deptNet = BigDecimal.ZERO;
            int count = 0;
            for (Payroll p : payrolls) {
                if (empIds.contains(p.getEmployeeId())) {
                    deptGross = deptGross.add(p.getGrossSalary() != null ? p.getGrossSalary() : BigDecimal.ZERO);
                    deptNet = deptNet.add(p.getNetSalary() != null ? p.getNetSalary() : BigDecimal.ZERO);
                    count++;
                }
            }
            Map<String, Object> item = new HashMap<>();
            item.put("department", dept.getName());
            item.put("grossSalary", deptGross);
            item.put("netSalary", deptNet);
            item.put("employeeCount", count);
            item.put("avgNet", count > 0 ? deptNet.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
            deptSalary.add(item);
        }
        result.put("departmentSalary", deptSalary);

        // 薪资构成分析
        List<Map<String, Object>> composition = new ArrayList<>();
        String[] compNames = {"基本工资", "加班费", "全勤奖", "社保扣除", "公积金扣除", "个税"};
        BigDecimal[] compValues = {
                totalGross.subtract(totalOvertime).subtract(totalBonus),
                totalOvertime,
                totalBonus,
                totalSocial,
                totalFund,
                totalTax
        };
        for (int i = 0; i < compNames.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", compNames[i]);
            item.put("value", compValues[i]);
            composition.add(item);
        }
        result.put("salaryComposition", composition);

        return result;
    }
}

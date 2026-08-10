package com.example.hrms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.common.BizException;
import com.example.hrms.entity.Department;
import com.example.hrms.entity.Employee;
import com.example.hrms.entity.SysUser;
import com.example.hrms.mapper.DepartmentMapper;
import com.example.hrms.mapper.EmployeeMapper;
import com.example.hrms.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    private void fillDeptName(List<Employee> list) {
        Map<Long, String> deptMap = departmentMapper.selectList(null).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName, (a, b) -> a));
        list.forEach(e -> e.setDepartmentName(deptMap.get(e.getDepartmentId())));
    }

    public Page<Employee> page(long current, long size, String keyword, Long departmentId) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Employee::getName, keyword)
                    .or().like(Employee::getEmpNo, keyword)
                    .or().like(Employee::getPhone, keyword));
        }
        if (departmentId != null) {
            wrapper.eq(Employee::getDepartmentId, departmentId);
        }
        wrapper.orderByDesc(Employee::getId);
        Page<Employee> page = employeeMapper.selectPage(new Page<>(current, size), wrapper);
        fillDeptName(page.getRecords());
        return page;
    }

    public List<Employee> listAll() {
        List<Employee> list = employeeMapper.selectList(
                new LambdaQueryWrapper<Employee>().orderByAsc(Employee::getId));
        fillDeptName(list);
        return list;
    }

    public Employee getById(Long id) {
        Employee e = employeeMapper.selectById(id);
        if (e != null) {
            fillDeptName(java.util.Collections.singletonList(e));
        }
        return e;
    }

    @Transactional
    public void create(Employee employee) {
        Long count = employeeMapper.selectCount(
                new LambdaQueryWrapper<Employee>().eq(Employee::getEmpNo, employee.getEmpNo()));
        if (count != null && count > 0) {
            throw new BizException("工号已存在：" + employee.getEmpNo());
        }
        employeeMapper.insert(employee);
        // 自动为员工创建登录账号（用户名=工号，初始密码 123456）
        SysUser user = new SysUser();
        user.setUsername(employee.getEmpNo());
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRealName(employee.getName());
        user.setRole("EMPLOYEE");
        user.setEmployeeId(employee.getId());
        user.setEnabled(1);
        sysUserMapper.insert(user);
    }

    public void update(Employee employee) {
        employeeMapper.updateById(employee);
    }

    @Transactional
    public void delete(Long id) {
        employeeMapper.deleteById(id);
        // 同步删除关联登录账号
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmployeeId, id));
        if (user != null) {
            sysUserMapper.deleteById(user.getId());
        }
    }

    public long count() {
        Long c = employeeMapper.selectCount(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getStatus, "在职"));
        return c == null ? 0 : c;
    }

    /** Excel 导入员工 */
    @Transactional
    public Map<String, Object> importExcel(MultipartFile file) {
        List<Map<String, String>> errors = new ArrayList<>();
        int success = 0;
        int total = 0;

        // 部门映射（名称 -> ID）
        Map<String, Long> deptMap = departmentMapper.selectList(null).stream()
                .collect(Collectors.toMap(Department::getName, Department::getId, (a, b) -> a));

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                total++;

                try {
                    String empNo = getCellStr(row.getCell(0));
                    String name = getCellStr(row.getCell(1));
                    String gender = getCellStr(row.getCell(2));
                    String phone = getCellStr(row.getCell(3));
                    String email = getCellStr(row.getCell(4));
                    String deptName = getCellStr(row.getCell(5));
                    String position = getCellStr(row.getCell(6));
                    String baseSalaryStr = getCellStr(row.getCell(7));
                    String hireDateStr = getCellStr(row.getCell(8));
                    String status = getCellStr(row.getCell(9));

                    if (!StringUtils.hasText(empNo) || !StringUtils.hasText(name)) {
                        throw new BizException("工号和姓名不能为空");
                    }

                    // 检查工号重复
                    Long count = employeeMapper.selectCount(
                            new LambdaQueryWrapper<Employee>().eq(Employee::getEmpNo, empNo));
                    if (count != null && count > 0) {
                        throw new BizException("工号已存在");
                    }

                    Employee emp = new Employee();
                    emp.setEmpNo(empNo);
                    emp.setName(name);
                    emp.setGender(StringUtils.hasText(gender) ? gender : "男");
                    emp.setPhone(phone);
                    emp.setEmail(email);
                    emp.setPosition(position);
                    emp.setStatus(StringUtils.hasText(status) ? status : "在职");

                    // 部门
                    if (StringUtils.hasText(deptName)) {
                        Long deptId = deptMap.get(deptName);
                        if (deptId == null) {
                            throw new BizException("部门不存在：" + deptName);
                        }
                        emp.setDepartmentId(deptId);
                    }

                    // 基本工资
                    if (StringUtils.hasText(baseSalaryStr)) {
                        emp.setBaseSalary(new BigDecimal(baseSalaryStr));
                    }

                    // 入职日期
                    if (StringUtils.hasText(hireDateStr)) {
                        Cell cell = row.getCell(8);
                        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                            emp.setHireDate(cell.getDateCellValue().toInstant()
                                    .atZone(ZoneId.systemDefault()).toLocalDate());
                        } else {
                            emp.setHireDate(LocalDate.parse(hireDateStr));
                        }
                    }

                    employeeMapper.insert(emp);

                    // 自动创建登录账号
                    SysUser user = new SysUser();
                    user.setUsername(empNo);
                    user.setPassword(passwordEncoder.encode("123456"));
                    user.setRealName(name);
                    user.setRole("EMPLOYEE");
                    user.setEmployeeId(emp.getId());
                    user.setEnabled(1);
                    sysUserMapper.insert(user);

                    success++;
                } catch (Exception e) {
                    Map<String, String> err = new HashMap<>();
                    err.put("row", String.valueOf(i + 1));
                    err.put("msg", e.getMessage());
                    errors.add(err);
                }
            }
        } catch (Exception e) {
            throw new BizException("解析 Excel 失败：" + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("success", success);
        result.put("fail", total - success);
        result.put("errors", errors);
        return result;
    }

    private String getCellStr(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) return cell.getDateCellValue().toString();
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }
}

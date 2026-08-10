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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
}

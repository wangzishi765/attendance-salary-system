package com.example.hrms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hrms.entity.Department;
import com.example.hrms.entity.Employee;
import com.example.hrms.mapper.DepartmentMapper;
import com.example.hrms.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final EmployeeMapper employeeMapper;

    public List<Department> list() {
        return departmentMapper.selectList(
                new LambdaQueryWrapper<Department>().orderByAsc(Department::getSort));
    }

    /** 树状部门列表，带员工数量 */
    public List<Department> listTree() {
        List<Department> all = list();
        // 统计每个部门的员工数量
        Map<Long, Long> empCountMap = employeeMapper.selectList(
                new LambdaQueryWrapper<Employee>().eq(Employee::getStatus, "在职"))
                .stream()
                .collect(Collectors.groupingBy(Employee::getDepartmentId, Collectors.counting()));

        all.forEach(d -> {
            d.setEmployeeCount(empCountMap.getOrDefault(d.getId(), 0L).intValue());
            if (d.getParentId() == null) d.setParentId(0L);
            if (d.getSort() == null) d.setSort(0);
            if (!StringUtils.hasText(d.getStatus())) d.setStatus("启用");
        });

        // 构建树
        return buildTree(all, 0L);
    }

    private List<Department> buildTree(List<Department> all, Long parentId) {
        List<Department> children = all.stream()
                .filter(d -> parentId.equals(d.getParentId()))
                .sorted((a, b) -> a.getSort() - b.getSort())
                .collect(Collectors.toList());
        children.forEach(c -> c.setChildren(buildTree(all, c.getId())));
        return children;
    }

    public void save(Department dept) {
        if (dept.getParentId() == null) dept.setParentId(0L);
        if (dept.getSort() == null) dept.setSort(0);
        if (!StringUtils.hasText(dept.getStatus())) dept.setStatus("启用");
        if (dept.getId() == null) {
            departmentMapper.insert(dept);
        } else {
            departmentMapper.updateById(dept);
        }
    }

    public void delete(Long id) {
        // 检查是否有子部门
        Long childCount = departmentMapper.selectCount(
                new LambdaQueryWrapper<Department>().eq(Department::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new com.example.hrms.common.BizException("该部门下还有子部门，无法删除");
        }
        // 检查是否有员工
        Long empCount = employeeMapper.selectCount(
                new LambdaQueryWrapper<Employee>().eq(Employee::getDepartmentId, id));
        if (empCount != null && empCount > 0) {
            throw new com.example.hrms.common.BizException("该部门下还有员工，无法删除");
        }
        departmentMapper.deleteById(id);
    }
}

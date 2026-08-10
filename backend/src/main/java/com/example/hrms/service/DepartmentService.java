package com.example.hrms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hrms.entity.Department;
import com.example.hrms.mapper.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentMapper departmentMapper;

    public List<Department> list() {
        return departmentMapper.selectList(
                new LambdaQueryWrapper<Department>().orderByAsc(Department::getId));
    }

    public void save(Department dept) {
        if (dept.getId() == null) {
            departmentMapper.insert(dept);
        } else {
            departmentMapper.updateById(dept);
        }
    }

    public void delete(Long id) {
        departmentMapper.deleteById(id);
    }
}

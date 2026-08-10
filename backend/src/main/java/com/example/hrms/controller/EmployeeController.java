package com.example.hrms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.common.Result;
import com.example.hrms.entity.Employee;
import com.example.hrms.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public Result<Page<Employee>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId) {
        return Result.success(employeeService.page(current, size, keyword, departmentId));
    }

    @GetMapping("/all")
    public Result<List<Employee>> all() {
        return Result.success(employeeService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Employee> get(@PathVariable Long id) {
        return Result.success(employeeService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<?> create(@RequestBody Employee employee) {
        employeeService.create(employee);
        return Result.success("新增成功，已自动创建登录账号（用户名=工号，初始密码123456）", null);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<?> update(@RequestBody Employee employee) {
        employeeService.update(employee);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<?> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return Result.success();
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        return Result.success("导入完成", employeeService.importExcel(file));
    }
}

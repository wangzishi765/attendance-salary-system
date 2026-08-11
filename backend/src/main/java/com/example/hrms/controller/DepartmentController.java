package com.example.hrms.controller;

import com.example.hrms.common.Result;
import com.example.hrms.entity.Department;
import com.example.hrms.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Department>> list() {
        return Result.success(departmentService.list());
    }

    @GetMapping("/tree")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Department>> tree() {
        return Result.success(departmentService.listTree());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> save(@RequestBody Department dept) {
        departmentService.save(dept);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return Result.success();
    }
}

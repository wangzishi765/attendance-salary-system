package com.example.hrms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.common.Result;
import com.example.hrms.entity.Tenant;
import com.example.hrms.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户管理控制器（仅管理员可访问）
 */
@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    /**
     * 分页查询租户
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<Tenant>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return Result.success(tenantService.page(current, size, keyword));
    }

    /**
     * 获取所有租户（下拉选择用）
     */
    @GetMapping("/all")
    public Result<List<Tenant>> all() {
        return Result.success(tenantService.listAll());
    }

    /**
     * 根据ID获取租户
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Tenant> get(@PathVariable Long id) {
        return Result.success(tenantService.getById(id));
    }

    /**
     * 新增租户
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Tenant> create(@RequestBody Tenant tenant) {
        return Result.success(tenantService.create(tenant));
    }

    /**
     * 更新租户
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Tenant> update(@RequestBody Tenant tenant) {
        return Result.success(tenantService.update(tenant));
    }

    /**
     * 删除租户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        tenantService.delete(id);
        return Result.success();
    }
}

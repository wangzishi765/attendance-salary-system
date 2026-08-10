package com.example.hrms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.common.Result;
import com.example.hrms.entity.Payroll;
import com.example.hrms.security.SecurityUtil;
import com.example.hrms.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    /** 生成/重算某月工资单 */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> generate(@RequestParam String month) {
        int count = payrollService.generateMonth(month);
        Map<String, Object> map = new HashMap<>();
        map.put("month", month);
        map.put("count", count);
        return Result.success("已生成 " + count + " 条工资单", map);
    }

    @GetMapping
    public Result<Page<Payroll>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String month) {
        if (!SecurityUtil.isAdmin()) {
            employeeId = SecurityUtil.getCurrentUser().getEmployeeId();
        }
        return Result.success(payrollService.page(current, size, employeeId, month));
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> markPaid(@PathVariable Long id) {
        payrollService.markPaid(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        payrollService.delete(id);
        return Result.success();
    }
}

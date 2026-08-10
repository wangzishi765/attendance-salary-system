package com.example.hrms.controller;

import com.example.hrms.common.Result;
import com.example.hrms.entity.SalaryRule;
import com.example.hrms.service.SalaryRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/salary-rule")
@RequiredArgsConstructor
public class SalaryRuleController {

    private final SalaryRuleService salaryRuleService;

    @GetMapping
    public Result<SalaryRule> current() {
        return Result.success(salaryRuleService.current());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> update(@RequestBody SalaryRule rule) {
        salaryRuleService.update(rule);
        return Result.success();
    }
}

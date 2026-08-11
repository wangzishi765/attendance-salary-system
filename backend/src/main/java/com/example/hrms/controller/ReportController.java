package com.example.hrms.controller;

import com.example.hrms.common.Result;
import com.example.hrms.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 统计报表控制器
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 人员统计报表
     */
    @GetMapping("/employee")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Map<String, Object>> employeeReport() {
        return Result.success(reportService.employeeReport());
    }

    /**
     * 考勤统计报表
     */
    @GetMapping("/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Map<String, Object>> attendanceReport(
            @RequestParam(required = false, defaultValue = "2026-08") String month) {
        return Result.success(reportService.attendanceReport(month));
    }

    /**
     * 薪资成本报表
     */
    @GetMapping("/salary")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Map<String, Object>> salaryReport(
            @RequestParam(required = false, defaultValue = "2026-08") String month) {
        return Result.success(reportService.salaryReport(month));
    }
}

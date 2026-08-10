package com.example.hrms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.common.Result;
import com.example.hrms.entity.OvertimeRecord;
import com.example.hrms.entity.SysUser;
import com.example.hrms.security.SecurityUtil;
import com.example.hrms.service.OvertimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/overtimes")
@RequiredArgsConstructor
public class OvertimeController {

    private final OvertimeService overtimeService;

    @GetMapping
    public Result<Page<OvertimeRecord>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status) {
        if (!SecurityUtil.isAdminOrHr()) {
            employeeId = SecurityUtil.getCurrentUser().getEmployeeId();
        }
        return Result.success(overtimeService.page(current, size, employeeId, status));
    }

    @PostMapping
    public Result<?> apply(@RequestBody OvertimeRecord record) {
        SysUser user = SecurityUtil.getCurrentUser();
        if (!SecurityUtil.isAdminOrHr() && user.getEmployeeId() != null) {
            record.setEmployeeId(user.getEmployeeId());
        }
        overtimeService.apply(record);
        return Result.success("申请已提交，等待审批", null);
    }

    @PutMapping("/{id}/audit")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<?> audit(@PathVariable Long id, @RequestParam String status) {
        overtimeService.audit(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<?> delete(@PathVariable Long id) {
        overtimeService.delete(id);
        return Result.success();
    }
}

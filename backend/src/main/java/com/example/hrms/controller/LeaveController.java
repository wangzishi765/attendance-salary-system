package com.example.hrms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.common.Result;
import com.example.hrms.entity.LeaveRecord;
import com.example.hrms.entity.SysUser;
import com.example.hrms.security.SecurityUtil;
import com.example.hrms.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @GetMapping
    public Result<Page<LeaveRecord>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status) {
        if (!SecurityUtil.isAdmin()) {
            employeeId = SecurityUtil.getCurrentUser().getEmployeeId();
        }
        return Result.success(leaveService.page(current, size, employeeId, status));
    }

    /** 员工提交请假申请 */
    @PostMapping
    public Result<?> apply(@RequestBody LeaveRecord record) {
        SysUser user = SecurityUtil.getCurrentUser();
        if (!SecurityUtil.isAdmin() && user.getEmployeeId() != null) {
            record.setEmployeeId(user.getEmployeeId());
        }
        leaveService.apply(record);
        return Result.success("申请已提交，等待审批", null);
    }

    /** 管理员审批 */
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> audit(@PathVariable Long id, @RequestParam String status) {
        leaveService.audit(id, status, SecurityUtil.getCurrentUser().getRealName());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        leaveService.delete(id);
        return Result.success();
    }
}

package com.example.hrms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.common.BizException;
import com.example.hrms.common.Result;
import com.example.hrms.entity.Attendance;
import com.example.hrms.entity.SysUser;
import com.example.hrms.security.SecurityUtil;
import com.example.hrms.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    private Long currentEmployeeId() {
        SysUser user = SecurityUtil.getCurrentUser();
        if (user == null || user.getEmployeeId() == null) {
            throw new BizException("当前账号未关联员工，无法打卡");
        }
        return user.getEmployeeId();
    }

    /** 上班打卡（当前登录员工） */
    @PostMapping("/check-in")
    public Result<Attendance> checkIn() {
        return Result.success("上班打卡成功", attendanceService.checkIn(currentEmployeeId()));
    }

    /** 下班打卡（当前登录员工） */
    @PostMapping("/check-out")
    public Result<Attendance> checkOut() {
        return Result.success("下班打卡成功", attendanceService.checkOut(currentEmployeeId()));
    }

    /** 当前登录员工今日打卡状态 */
    @GetMapping("/today")
    public Result<Attendance> today() {
        return Result.success(attendanceService.today(currentEmployeeId()));
    }

    /** 分页查询（管理员/人事可查全部；员工只能查自己） */
    @GetMapping
    public Result<Page<Attendance>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String month) {
        if (!SecurityUtil.isAdminOrHr()) {
            employeeId = currentEmployeeId();
        }
        return Result.success(attendanceService.page(current, size, employeeId, month));
    }

    /** 月度统计 */
    @GetMapping("/stat")
    public Result<Map<String, Object>> stat(
            @RequestParam(required = false) Long employeeId,
            @RequestParam String month) {
        if (!SecurityUtil.isAdminOrHr() || employeeId == null) {
            employeeId = currentEmployeeId();
        }
        return Result.success(attendanceService.monthStat(employeeId, month));
    }

    /** 月历视图 */
    @GetMapping("/calendar")
    public Result<Map<String, Object>> calendar(
            @RequestParam(required = false) Long employeeId,
            @RequestParam String month) {
        if (!SecurityUtil.isAdminOrHr() || employeeId == null) {
            employeeId = currentEmployeeId();
        }
        return Result.success(attendanceService.calendar(employeeId, month));
    }

    @PostMapping("/manual")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<?> manualSave(@RequestBody Attendance a) {
        attendanceService.save(a);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<?> delete(@PathVariable Long id) {
        attendanceService.delete(id);
        return Result.success();
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        return Result.success("导入完成", attendanceService.importExcel(file));
    }
}

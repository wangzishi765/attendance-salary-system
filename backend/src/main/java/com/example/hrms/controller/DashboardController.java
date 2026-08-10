package com.example.hrms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hrms.common.Result;
import com.example.hrms.entity.Attendance;
import com.example.hrms.entity.LeaveRecord;
import com.example.hrms.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 首页仪表盘统计
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final AttendanceMapper attendanceMapper;
    private final LeaveRecordMapper leaveRecordMapper;

    @GetMapping("/stat")
    public Result<Map<String, Object>> stat() {
        Map<String, Object> map = new HashMap<>();
        map.put("employeeCount", employeeMapper.selectCount(new LambdaQueryWrapper<>()));
        map.put("departmentCount", departmentMapper.selectCount(new LambdaQueryWrapper<>()));
        // 今日出勤人数
        Long todayAttend = attendanceMapper.selectCount(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getAttendDate, LocalDate.now()));
        map.put("todayAttendance", todayAttend);
        // 待审批请假数
        Long pendingLeave = leaveRecordMapper.selectCount(new LambdaQueryWrapper<LeaveRecord>()
                .eq(LeaveRecord::getStatus, "PENDING"));
        map.put("pendingLeave", pendingLeave);
        return Result.success(map);
    }
}

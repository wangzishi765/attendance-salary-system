package com.example.hrms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hrms.common.CacheService;
import com.example.hrms.common.Result;
import com.example.hrms.config.TenantContext;
import com.example.hrms.entity.Attendance;
import com.example.hrms.entity.LeaveRecord;
import com.example.hrms.mapper.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 首页仪表盘统计
 * 使用 Redis 缓存优化，缓存5分钟，数据变更时自动清除
 */
@Api(tags = "仪表盘管理")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final AttendanceMapper attendanceMapper;
    private final LeaveRecordMapper leaveRecordMapper;
    private final CacheService cacheService;

    private static final String CACHE_PREFIX = "dashboard:stat:";
    private static final long CACHE_TTL = 5; // 缓存5分钟

    @ApiOperation("获取仪表盘统计数据")
    @GetMapping("/stat")
    public Result<Map<String, Object>> stat() {
        // 按租户隔离缓存
        Long tenantId = TenantContext.getTenantId();
        String cacheKey = CACHE_PREFIX + (tenantId != null ? tenantId : "default");

        // 先查缓存
        Map<String, Object> cached = cacheService.get(cacheKey);
        if (cached != null) {
            return Result.success(cached);
        }

        // 缓存未命中，查询数据库
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

        // 写入缓存
        cacheService.set(cacheKey, map, CACHE_TTL, TimeUnit.MINUTES);

        return Result.success(map);
    }

    /**
     * 清除仪表盘缓存（数据变更时调用）
     */
    public void clearCache() {
        Long tenantId = TenantContext.getTenantId();
        String cacheKey = CACHE_PREFIX + (tenantId != null ? tenantId : "default");
        cacheService.delete(cacheKey);
    }
}

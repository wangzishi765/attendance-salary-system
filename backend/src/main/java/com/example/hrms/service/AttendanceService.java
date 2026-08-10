package com.example.hrms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.common.BizException;
import com.example.hrms.entity.Attendance;
import com.example.hrms.entity.Employee;
import com.example.hrms.mapper.AttendanceMapper;
import com.example.hrms.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceMapper attendanceMapper;
    private final EmployeeMapper employeeMapper;

    /** 上班时间 09:00，下班时间 18:00 */
    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(18, 0);

    private void fillEmpName(List<Attendance> list) {
        Map<Long, String> map = employeeMapper.selectList(null).stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName, (a, b) -> a));
        list.forEach(a -> a.setEmployeeName(map.get(a.getEmployeeId())));
    }

    /** 上班打卡 */
    public Attendance checkIn(Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance record = attendanceMapper.selectOne(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getEmployeeId, employeeId)
                .eq(Attendance::getAttendDate, today));
        LocalDateTime now = LocalDateTime.now();
        if (record != null && record.getCheckInTime() != null) {
            throw new BizException("今天已经打过上班卡了");
        }
        if (record == null) {
            record = new Attendance();
            record.setEmployeeId(employeeId);
            record.setAttendDate(today);
        }
        record.setCheckInTime(now);
        record.setStatus(now.toLocalTime().isAfter(WORK_START) ? "LATE" : "NORMAL");
        if (record.getId() == null) {
            attendanceMapper.insert(record);
        } else {
            attendanceMapper.updateById(record);
        }
        return record;
    }

    /** 下班打卡 */
    public Attendance checkOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance record = attendanceMapper.selectOne(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getEmployeeId, employeeId)
                .eq(Attendance::getAttendDate, today));
        if (record == null || record.getCheckInTime() == null) {
            throw new BizException("请先打上班卡");
        }
        LocalDateTime now = LocalDateTime.now();
        record.setCheckOutTime(now);
        // 早退判定（不覆盖迟到状态，迟到优先展示）
        if (!"LATE".equals(record.getStatus()) && now.toLocalTime().isBefore(WORK_END)) {
            record.setStatus("EARLY");
        }
        // 计算工时
        long minutes = Duration.between(record.getCheckInTime(), now).toMinutes();
        BigDecimal hours = new BigDecimal(minutes).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
        record.setWorkHours(hours);
        attendanceMapper.updateById(record);
        return record;
    }

    public Attendance today(Long employeeId) {
        return attendanceMapper.selectOne(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getEmployeeId, employeeId)
                .eq(Attendance::getAttendDate, LocalDate.now()));
    }

    public Page<Attendance> page(long current, long size, Long employeeId, String month) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        if (employeeId != null) {
            wrapper.eq(Attendance::getEmployeeId, employeeId);
        }
        if (month != null && !month.isEmpty()) {
            LocalDate start = LocalDate.parse(month + "-01");
            LocalDate end = start.plusMonths(1);
            wrapper.ge(Attendance::getAttendDate, start).lt(Attendance::getAttendDate, end);
        }
        wrapper.orderByDesc(Attendance::getAttendDate);
        Page<Attendance> page = attendanceMapper.selectPage(new Page<>(current, size), wrapper);
        fillEmpName(page.getRecords());
        return page;
    }

    public List<Attendance> listByEmployeeMonth(Long employeeId, String month) {
        LocalDate start = LocalDate.parse(month + "-01");
        LocalDate end = start.plusMonths(1);
        return attendanceMapper.selectList(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getEmployeeId, employeeId)
                .ge(Attendance::getAttendDate, start)
                .lt(Attendance::getAttendDate, end));
    }

    /** 某员工某月考勤统计 */
    public Map<String, Object> monthStat(Long employeeId, String month) {
        List<Attendance> list = listByEmployeeMonth(employeeId, month);
        long normal = list.stream().filter(a -> "NORMAL".equals(a.getStatus())).count();
        long late = list.stream().filter(a -> "LATE".equals(a.getStatus())).count();
        long early = list.stream().filter(a -> "EARLY".equals(a.getStatus())).count();
        long absent = list.stream().filter(a -> "ABSENT".equals(a.getStatus())).count();
        Map<String, Object> map = new HashMap<>();
        map.put("total", list.size());
        map.put("normal", normal);
        map.put("late", late);
        map.put("early", early);
        map.put("absent", absent);
        return map;
    }

    public void save(Attendance a) {
        if (a.getId() == null) {
            attendanceMapper.insert(a);
        } else {
            attendanceMapper.updateById(a);
        }
    }

    public void delete(Long id) {
        attendanceMapper.deleteById(id);
    }
}

package com.example.hrms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.common.BizException;
import com.example.hrms.entity.Attendance;
import com.example.hrms.entity.Employee;
import com.example.hrms.mapper.AttendanceMapper;
import com.example.hrms.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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

    /** 月历视图：返回某月每天的考勤状态 */
    public Map<String, Object> calendar(Long employeeId, String month) {
        List<Attendance> list = listByEmployeeMonth(employeeId, month);
        // 按日期建立索引
        Map<LocalDate, Attendance> map = new HashMap<>();
        for (Attendance a : list) {
            map.put(a.getAttendDate(), a);
        }

        LocalDate start = LocalDate.parse(month + "-01");
        LocalDate end = start.plusMonths(1);
        List<Map<String, Object>> days = new ArrayList<>();

        for (LocalDate d = start; d.isBefore(end); d = d.plusDays(1)) {
            Map<String, Object> day = new HashMap<>();
            day.put("date", d.toString());
            day.put("day", d.getDayOfMonth());
            int weekday = d.getDayOfWeek().getValue(); // 1=周一 ... 7=周日
            day.put("weekday", weekday);
            boolean isWeekend = weekday >= 6;
            day.put("isWeekend", isWeekend);

            Attendance a = map.get(d);
            if (a != null) {
                day.put("status", a.getStatus());
                day.put("checkInTime", a.getCheckInTime() != null ? a.getCheckInTime().toLocalTime().toString() : null);
                day.put("checkOutTime", a.getCheckOutTime() != null ? a.getCheckOutTime().toLocalTime().toString() : null);
                day.put("workHours", a.getWorkHours());
            } else if (isWeekend) {
                day.put("status", "WEEKEND");
            } else {
                day.put("status", "ABSENT");
            }
            days.add(day);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("month", month);
        result.put("days", days);
        // 顺带返回统计
        result.put("stat", monthStat(employeeId, month));
        return result;
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

    /** Excel 导入考勤 */
    public Map<String, Object> importExcel(MultipartFile file) {
        List<Map<String, String>> errors = new ArrayList<>();
        int success = 0;
        int total = 0;

        // 员工映射（工号 -> ID）
        Map<String, Long> empMap = employeeMapper.selectList(null).stream()
                .collect(Collectors.toMap(Employee::getEmpNo, Employee::getId, (a, b) -> a));

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                total++;

                try {
                    String empNo = getCellStr(row.getCell(0));
                    String dateStr = getCellStr(row.getCell(1));
                    String checkInStr = getCellStr(row.getCell(2));
                    String checkOutStr = getCellStr(row.getCell(3));
                    String status = getCellStr(row.getCell(4));
                    String remark = getCellStr(row.getCell(5));

                    if (!empMap.containsKey(empNo)) {
                        throw new BizException("工号不存在：" + empNo);
                    }
                    Long employeeId = empMap.get(empNo);

                    LocalDate date;
                    Cell dateCell = row.getCell(1);
                    if (dateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dateCell)) {
                        date = dateCell.getDateCellValue().toInstant()
                                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                    } else {
                        date = LocalDate.parse(dateStr);
                    }

                    Attendance a = attendanceMapper.selectOne(
                            new LambdaQueryWrapper<Attendance>()
                                    .eq(Attendance::getEmployeeId, employeeId)
                                    .eq(Attendance::getAttendDate, date));
                    boolean isNew = false;
                    if (a == null) {
                        a = new Attendance();
                        a.setEmployeeId(employeeId);
                        a.setAttendDate(date);
                        isNew = true;
                    }

                    // 上班时间
                    if (hasText(checkInStr)) {
                        LocalTime t = LocalTime.parse(checkInStr);
                        a.setCheckInTime(LocalDateTime.of(date, t));
                    }
                    // 下班时间
                    if (hasText(checkOutStr)) {
                        LocalTime t = LocalTime.parse(checkOutStr);
                        a.setCheckOutTime(LocalDateTime.of(date, t));
                    }
                    // 状态
                    if (hasText(status)) {
                        a.setStatus(status.toUpperCase());
                    } else if (a.getCheckInTime() != null) {
                        a.setStatus(a.getCheckInTime().toLocalTime().isAfter(WORK_START) ? "LATE" : "NORMAL");
                    }
                    // 工时
                    if (a.getCheckInTime() != null && a.getCheckOutTime() != null) {
                        long minutes = Duration.between(a.getCheckInTime(), a.getCheckOutTime()).toMinutes();
                        a.setWorkHours(new BigDecimal(minutes).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP));
                    }
                    a.setRemark(remark);

                    if (isNew) {
                        attendanceMapper.insert(a);
                    } else {
                        attendanceMapper.updateById(a);
                    }
                    success++;
                } catch (Exception e) {
                    Map<String, String> err = new HashMap<>();
                    err.put("row", String.valueOf(i + 1));
                    err.put("msg", e.getMessage());
                    errors.add(err);
                }
            }
        } catch (Exception e) {
            throw new BizException("解析 Excel 失败：" + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("success", success);
        result.put("fail", total - success);
        result.put("errors", errors);
        return result;
    }

    private boolean hasText(String s) {
        return s != null && !s.isEmpty();
    }

    private String getCellStr(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) return cell.getDateCellValue().toString();
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }
}

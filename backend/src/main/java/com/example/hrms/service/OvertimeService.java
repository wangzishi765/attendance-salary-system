package com.example.hrms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.entity.Employee;
import com.example.hrms.entity.OvertimeRecord;
import com.example.hrms.mapper.EmployeeMapper;
import com.example.hrms.mapper.OvertimeRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OvertimeService {

    private final OvertimeRecordMapper overtimeRecordMapper;
    private final EmployeeMapper employeeMapper;

    private void fillEmpName(List<OvertimeRecord> list) {
        Map<Long, String> map = employeeMapper.selectList(null).stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName, (a, b) -> a));
        list.forEach(a -> a.setEmployeeName(map.get(a.getEmployeeId())));
    }

    public Page<OvertimeRecord> page(long current, long size, Long employeeId, String status) {
        LambdaQueryWrapper<OvertimeRecord> wrapper = new LambdaQueryWrapper<>();
        if (employeeId != null) {
            wrapper.eq(OvertimeRecord::getEmployeeId, employeeId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(OvertimeRecord::getStatus, status);
        }
        wrapper.orderByDesc(OvertimeRecord::getId);
        Page<OvertimeRecord> page = overtimeRecordMapper.selectPage(new Page<>(current, size), wrapper);
        fillEmpName(page.getRecords());
        return page;
    }

    public void apply(OvertimeRecord record) {
        record.setStatus("PENDING");
        overtimeRecordMapper.insert(record);
    }

    public void audit(Long id, String status) {
        OvertimeRecord record = overtimeRecordMapper.selectById(id);
        record.setStatus(status);
        overtimeRecordMapper.updateById(record);
    }

    public void delete(Long id) {
        overtimeRecordMapper.deleteById(id);
    }
}

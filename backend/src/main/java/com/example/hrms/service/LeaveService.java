package com.example.hrms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.entity.Employee;
import com.example.hrms.entity.LeaveRecord;
import com.example.hrms.mapper.EmployeeMapper;
import com.example.hrms.mapper.LeaveRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRecordMapper leaveRecordMapper;
    private final EmployeeMapper employeeMapper;

    private void fillEmpName(List<LeaveRecord> list) {
        Map<Long, String> map = employeeMapper.selectList(null).stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName, (a, b) -> a));
        list.forEach(a -> a.setEmployeeName(map.get(a.getEmployeeId())));
    }

    public Page<LeaveRecord> page(long current, long size, Long employeeId, String status) {
        LambdaQueryWrapper<LeaveRecord> wrapper = new LambdaQueryWrapper<>();
        if (employeeId != null) {
            wrapper.eq(LeaveRecord::getEmployeeId, employeeId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(LeaveRecord::getStatus, status);
        }
        wrapper.orderByDesc(LeaveRecord::getId);
        Page<LeaveRecord> page = leaveRecordMapper.selectPage(new Page<>(current, size), wrapper);
        fillEmpName(page.getRecords());
        return page;
    }

    public void apply(LeaveRecord record) {
        record.setStatus("PENDING");
        leaveRecordMapper.insert(record);
    }

    public void audit(Long id, String status, String approver) {
        LeaveRecord record = leaveRecordMapper.selectById(id);
        record.setStatus(status);
        record.setApprover(approver);
        leaveRecordMapper.updateById(record);
    }

    public void delete(Long id) {
        leaveRecordMapper.deleteById(id);
    }
}

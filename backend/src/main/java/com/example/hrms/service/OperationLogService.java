package com.example.hrms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.entity.OperationLog;
import com.example.hrms.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务
 */
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    /**
     * 分页查询操作日志
     */
    public Page<OperationLog> page(long current, long size, String module, String username, String status) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (module != null && !module.isEmpty()) {
            wrapper.like(OperationLog::getModule, module);
        }
        if (username != null && !username.isEmpty()) {
            wrapper.like(OperationLog::getUsername, username)
                    .or().like(OperationLog::getRealName, username);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(OperationLog::getStatus, status);
        }
        wrapper.orderByDesc(OperationLog::getOperationTime);
        return operationLogMapper.selectPage(new Page<>(current, size), wrapper);
    }

    /**
     * 保存操作日志
     */
    public void save(OperationLog log) {
        operationLogMapper.insert(log);
    }
}

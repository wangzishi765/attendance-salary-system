package com.example.hrms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.common.BizException;
import com.example.hrms.entity.*;
import com.example.hrms.mapper.*;
import com.example.hrms.security.SecurityUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 工作流服务
 */
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowProcessMapper processMapper;
    private final WorkflowInstanceMapper instanceMapper;
    private final WorkflowTaskMapper taskMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取所有流程定义
     */
    public List<WorkflowProcess> listProcesses() {
        return processMapper.selectList(new LambdaQueryWrapper<WorkflowProcess>()
                .eq(WorkflowProcess::getStatus, "ACTIVE")
                .orderByAsc(WorkflowProcess::getId));
    }

    /**
     * 发起流程
     */
    @Transactional
    public WorkflowInstance startProcess(String processCode, String businessType, Long businessId, String title) {
        // 查询流程定义
        WorkflowProcess process = processMapper.selectOne(new LambdaQueryWrapper<WorkflowProcess>()
                .eq(WorkflowProcess::getProcessCode, processCode)
                .eq(WorkflowProcess::getStatus, "ACTIVE"));
        if (process == null) {
            throw new BizException("流程不存在或已禁用");
        }

        SysUser user = SecurityUtil.getCurrentUser();

        // 创建流程实例
        WorkflowInstance instance = new WorkflowInstance();
        instance.setProcessId(process.getId());
        instance.setProcessCode(process.getProcessCode());
        instance.setProcessName(process.getProcessName());
        instance.setInitiatorId(user.getId());
        instance.setInitiatorName(user.getRealName());
        instance.setBusinessType(businessType);
        instance.setBusinessId(businessId);
        instance.setTitle(title);
        instance.setCurrentNode(0);
        instance.setStatus("PENDING");
        instance.setStartTime(LocalDateTime.now());
        instanceMapper.insert(instance);

        // 解析节点配置，创建第一个审批任务
        try {
            List<Map<String, Object>> nodes = objectMapper.readValue(process.getNodesConfig(),
                    new TypeReference<List<Map<String, Object>>>() {});
            if (nodes != null && !nodes.isEmpty()) {
                Map<String, Object> firstNode = nodes.get(0);
                WorkflowTask task = new WorkflowTask();
                task.setInstanceId(instance.getId());
                task.setNodeName((String) firstNode.get("nodeName"));
                task.setNodeIndex(0);
                task.setApproverRole((String) firstNode.get("role"));
                task.setStatus("PENDING");
                taskMapper.insert(task);
            } else {
                // 没有审批节点，直接通过
                instance.setStatus("APPROVED");
                instance.setEndTime(LocalDateTime.now());
                instanceMapper.updateById(instance);
            }
        } catch (Exception e) {
            throw new BizException("流程节点配置错误");
        }

        return instance;
    }

    /**
     * 审批任务
     */
    @Transactional
    public void approveTask(Long taskId, String status, String comment) {
        WorkflowTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BizException("审批任务不存在");
        }
        if (!"PENDING".equals(task.getStatus())) {
            throw new BizException("该任务已审批");
        }

        SysUser user = SecurityUtil.getCurrentUser();

        // 更新任务
        task.setStatus(status);
        task.setComment(comment);
        task.setApproverId(user.getId());
        task.setApproverName(user.getRealName());
        task.setApproveTime(LocalDateTime.now());
        taskMapper.updateById(task);

        WorkflowInstance instance = instanceMapper.selectById(task.getInstanceId());

        if ("REJECTED".equals(status)) {
            // 拒绝，流程结束
            instance.setStatus("REJECTED");
            instance.setEndTime(LocalDateTime.now());
            instanceMapper.updateById(instance);
            return;
        }

        // 查询流程定义，获取下一个节点
        WorkflowProcess process = processMapper.selectById(instance.getProcessId());
        try {
            List<Map<String, Object>> nodes = objectMapper.readValue(process.getNodesConfig(),
                    new TypeReference<List<Map<String, Object>>>() {});
            int nextIndex = task.getNodeIndex() + 1;

            if (nextIndex >= nodes.size()) {
                // 所有节点审批完成，流程通过
                instance.setStatus("APPROVED");
                instance.setEndTime(LocalDateTime.now());
                instanceMapper.updateById(instance);
            } else {
                // 创建下一个审批任务
                Map<String, Object> nextNode = nodes.get(nextIndex);
                WorkflowTask nextTask = new WorkflowTask();
                nextTask.setInstanceId(instance.getId());
                nextTask.setNodeName((String) nextNode.get("nodeName"));
                nextTask.setNodeIndex(nextIndex);
                nextTask.setApproverRole((String) nextNode.get("role"));
                nextTask.setStatus("PENDING");
                taskMapper.insert(nextTask);

                instance.setCurrentNode(nextIndex);
                instanceMapper.updateById(instance);
            }
        } catch (Exception e) {
            throw new BizException("流程节点配置错误");
        }
    }

    /**
     * 查询待我审批的任务
     */
    public Page<Map<String, Object>> myPendingTasks(long current, long size) {
        SysUser user = SecurityUtil.getCurrentUser();
        String role = user.getRole();

        LambdaQueryWrapper<WorkflowTask> wrapper = new LambdaQueryWrapper<WorkflowTask>()
                .eq(WorkflowTask::getStatus, "PENDING")
                .eq(WorkflowTask::getApproverRole, role)
                .orderByDesc(WorkflowTask::getCreateTime);

        Page<WorkflowTask> taskPage = taskMapper.selectPage(new Page<>(current, size), wrapper);

        // 关联查询流程实例信息
        Page<Map<String, Object>> result = new Page<>(current, size, taskPage.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();
        for (WorkflowTask task : taskPage.getRecords()) {
            WorkflowInstance instance = instanceMapper.selectById(task.getInstanceId());
            Map<String, Object> item = new HashMap<>();
            item.put("taskId", task.getId());
            item.put("instanceId", instance.getId());
            item.put("title", instance.getTitle());
            item.put("processName", instance.getProcessName());
            item.put("initiatorName", instance.getInitiatorName());
            item.put("nodeName", task.getNodeName());
            item.put("businessType", instance.getBusinessType());
            item.put("businessId", instance.getBusinessId());
            item.put("startTime", instance.getStartTime());
            item.put("createTime", task.getCreateTime());
            records.add(item);
        }
        result.setRecords(records);
        return result;
    }

    /**
     * 查询我已审批的任务
     */
    public Page<Map<String, Object>> myApprovedTasks(long current, long size) {
        SysUser user = SecurityUtil.getCurrentUser();

        LambdaQueryWrapper<WorkflowTask> wrapper = new LambdaQueryWrapper<WorkflowTask>()
                .eq(WorkflowTask::getApproverId, user.getId())
                .ne(WorkflowTask::getStatus, "PENDING")
                .orderByDesc(WorkflowTask::getApproveTime);

        Page<WorkflowTask> taskPage = taskMapper.selectPage(new Page<>(current, size), wrapper);

        Page<Map<String, Object>> result = new Page<>(current, size, taskPage.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();
        for (WorkflowTask task : taskPage.getRecords()) {
            WorkflowInstance instance = instanceMapper.selectById(task.getInstanceId());
            Map<String, Object> item = new HashMap<>();
            item.put("taskId", task.getId());
            item.put("instanceId", instance.getId());
            item.put("title", instance.getTitle());
            item.put("processName", instance.getProcessName());
            item.put("initiatorName", instance.getInitiatorName());
            item.put("nodeName", task.getNodeName());
            item.put("status", task.getStatus());
            item.put("comment", task.getComment());
            item.put("approveTime", task.getApproveTime());
            records.add(item);
        }
        result.setRecords(records);
        return result;
    }

    /**
     * 查询我发起的流程
     */
    public Page<WorkflowInstance> myStartedProcesses(long current, long size) {
        SysUser user = SecurityUtil.getCurrentUser();
        return instanceMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<WorkflowInstance>()
                        .eq(WorkflowInstance::getInitiatorId, user.getId())
                        .orderByDesc(WorkflowInstance::getCreateTime));
    }

    /**
     * 查询审批历史
     */
    public List<WorkflowTask> getApprovalHistory(Long instanceId) {
        return taskMapper.selectList(new LambdaQueryWrapper<WorkflowTask>()
                .eq(WorkflowTask::getInstanceId, instanceId)
                .orderByAsc(WorkflowTask::getNodeIndex));
    }

    /**
     * 获取流程详情
     */
    public WorkflowInstance getInstance(Long id) {
        return instanceMapper.selectById(id);
    }
}

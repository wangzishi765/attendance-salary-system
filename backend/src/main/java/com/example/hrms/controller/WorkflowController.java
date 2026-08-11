package com.example.hrms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.common.Result;
import com.example.hrms.entity.WorkflowInstance;
import com.example.hrms.entity.WorkflowProcess;
import com.example.hrms.entity.WorkflowTask;
import com.example.hrms.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流控制器
 */
@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    /**
     * 获取所有流程定义
     */
    @GetMapping("/processes")
    public Result<List<WorkflowProcess>> listProcesses() {
        return Result.success(workflowService.listProcesses());
    }

    /**
     * 发起流程
     */
    @PostMapping("/start")
    public Result<WorkflowInstance> startProcess(@RequestBody Map<String, Object> params) {
        String processCode = (String) params.get("processCode");
        String businessType = (String) params.get("businessType");
        Long businessId = params.get("businessId") != null ? Long.valueOf(params.get("businessId").toString()) : null;
        String title = (String) params.get("title");
        return Result.success(workflowService.startProcess(processCode, businessType, businessId, title));
    }

    /**
     * 审批任务
     */
    @PutMapping("/tasks/{id}/approve")
    public Result<Void> approveTask(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String status = (String) params.get("status");
        String comment = (String) params.get("comment");
        workflowService.approveTask(id, status, comment);
        return Result.success();
    }

    /**
     * 待我审批的任务
     */
    @GetMapping("/tasks/pending")
    public Result<Page<Map<String, Object>>> myPendingTasks(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.success(workflowService.myPendingTasks(current, size));
    }

    /**
     * 我已审批的任务
     */
    @GetMapping("/tasks/approved")
    public Result<Page<Map<String, Object>>> myApprovedTasks(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.success(workflowService.myApprovedTasks(current, size));
    }

    /**
     * 我发起的流程
     */
    @GetMapping("/instances/my")
    public Result<Page<WorkflowInstance>> myStartedProcesses(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.success(workflowService.myStartedProcesses(current, size));
    }

    /**
     * 审批历史
     */
    @GetMapping("/instances/{id}/history")
    public Result<List<WorkflowTask>> getApprovalHistory(@PathVariable Long id) {
        return Result.success(workflowService.getApprovalHistory(id));
    }

    /**
     * 流程详情
     */
    @GetMapping("/instances/{id}")
    public Result<WorkflowInstance> getInstance(@PathVariable Long id) {
        return Result.success(workflowService.getInstance(id));
    }
}

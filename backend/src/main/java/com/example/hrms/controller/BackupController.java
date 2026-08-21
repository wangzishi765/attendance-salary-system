package com.example.hrms.controller;

import com.example.hrms.common.Result;
import com.example.hrms.service.BackupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Api(tags = "数据备份管理")
@RestController
@RequestMapping("/api/backup")
@PreAuthorize("hasRole('ADMIN')")
public class BackupController {

    @Autowired
    private BackupService backupService;

    @ApiOperation("创建数据备份")
    @PostMapping("/create")
    public Result<Map<String, Object>> createBackup() {
        return Result.success(backupService.createBackup());
    }

    @ApiOperation("获取备份列表")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> listBackups() {
        return Result.success(backupService.listBackups());
    }

    @ApiOperation("下载备份文件")
    @GetMapping("/download/{name}")
    public ResponseEntity<Resource> downloadBackup(@PathVariable String name) {
        try {
            Path backupFile = backupService.getBackupFile(name);
            if (!Files.exists(backupFile)) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new FileSystemResource(backupFile);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiOperation("删除备份文件")
    @DeleteMapping("/{name}")
    public Result<Void> deleteBackup(@PathVariable String name) {
        backupService.deleteBackup(name);
        return Result.success();
    }
}

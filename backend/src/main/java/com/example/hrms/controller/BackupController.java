package com.example.hrms.controller;

import com.example.hrms.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/backup")
@PreAuthorize("hasRole('ADMIN')")
public class BackupController {

    @Autowired
    private DataSource dataSource;

    private Path backupDir;

    @PostConstruct
    public void init() {
        backupDir = Paths.get("backup").toAbsolutePath();
        if (!Files.exists(backupDir)) {
            try {
                Files.createDirectories(backupDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @PostMapping("/create")
    public Result<Map<String, Object>> createBackup() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupName = "backup_" + timestamp + ".zip";
            Path backupFile = backupDir.resolve(backupName);

            // 使用H2的BACKUP命令安全备份（数据库运行时也可执行）
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                String backupPath = backupFile.toAbsolutePath().toString().replace("\\", "/");
                stmt.execute("BACKUP TO '" + backupPath + "'");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("name", backupName);
            result.put("size", Files.size(backupFile));
            result.put("createTime", LocalDateTime.now().toString());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("备份失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> listBackups() {
        try {
            if (!Files.exists(backupDir)) {
                return Result.success(new ArrayList<>());
            }
            List<Map<String, Object>> list = Files.list(backupDir)
                .filter(f -> f.getFileName().toString().endsWith(".zip"))
                .sorted((f1, f2) -> {
                    try {
                        return Files.getLastModifiedTime(f2).compareTo(Files.getLastModifiedTime(f1));
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .map(f -> {
                    Map<String, Object> item = new HashMap<>();
                    try {
                        item.put("name", f.getFileName().toString());
                        item.put("size", Files.size(f));
                        item.put("createTime", Files.getLastModifiedTime(f).toString());
                    } catch (IOException e) {}
                    return item;
                })
                .collect(Collectors.toList());
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取备份列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/download/{name}")
    public ResponseEntity<Resource> downloadBackup(@PathVariable String name) {
        try {
            Path backupFile = backupDir.resolve(name);
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

    @DeleteMapping("/{name}")
    public Result<Void> deleteBackup(@PathVariable String name) {
        try {
            Path backupFile = backupDir.resolve(name);
            if (Files.exists(backupFile)) {
                Files.delete(backupFile);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}

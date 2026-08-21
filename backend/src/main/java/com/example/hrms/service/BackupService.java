package com.example.hrms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

/**
 * 数据备份服务
 * 使用 H2 的 BACKUP TO 命令实现运行时安全备份
 */
@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);

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
                log.error("创建备份目录失败", e);
            }
        }
    }

    /**
     * 创建备份
     * @return 备份文件信息
     */
    public Map<String, Object> createBackup() {
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
            log.info("数据备份成功：{}", backupName);
            return result;
        } catch (Exception e) {
            log.error("数据备份失败", e);
            throw new RuntimeException("备份失败: " + e.getMessage(), e);
        }
    }

    /**
     * 列出备份文件
     */
    public List<Map<String, Object>> listBackups() {
        try {
            if (!Files.exists(backupDir)) {
                return new ArrayList<>();
            }
            return Files.list(backupDir)
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
                        } catch (IOException e) {
                            // ignore
                        }
                        return item;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取备份列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 删除备份文件
     */
    public boolean deleteBackup(String name) {
        try {
            Path backupFile = backupDir.resolve(name);
            if (Files.exists(backupFile)) {
                Files.delete(backupFile);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("删除备份失败", e);
            return false;
        }
    }

    /**
     * 获取备份文件路径
     */
    public Path getBackupFile(String name) {
        return backupDir.resolve(name);
    }
}

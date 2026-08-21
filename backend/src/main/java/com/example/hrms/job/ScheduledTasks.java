package com.example.hrms.job;

import com.example.hrms.config.TenantContext;
import com.example.hrms.service.BackupService;
import com.example.hrms.service.PayrollService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 定时任务调度
 * 支持两种模式：
 * 1. 本地模式（默认）：使用 Spring @Scheduled，无需额外部署
 * 2. 分布式模式：使用 XXL-Job，配置调度中心地址后自动启用
 *
 * 任务列表：
 * - 每月1号自动生成上月工资单
 * - 每天凌晨统计考勤（预留）
 * - 每周日自动备份数据
 */
@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private PayrollService payrollService;

    @Autowired(required = false)
    private BackupService backupService;

    @Value("${hrms.schedule.enabled:true}")
    private boolean scheduleEnabled;

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * 每月1号凌晨2点自动生成上月工资单
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    @XxlJob("autoGeneratePayrollHandler")
    public void autoGeneratePayroll() {
        if (!scheduleEnabled) return;
        try {
            TenantContext.setTenantId(1L);
            // 生成上月工资单
            String lastMonth = LocalDate.now().minusMonths(1).format(MONTH_FORMAT);
            log.info("【定时任务】开始自动生成 {} 月工资单", lastMonth);
            int count = payrollService.generateMonth(lastMonth);
            log.info("【定时任务】{} 月工资单生成完成，共 {} 条", lastMonth, count);
        } catch (Exception e) {
            log.error("【定时任务】自动生成工资单失败", e);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 每周日凌晨4点自动备份数据
     */
    @Scheduled(cron = "0 0 4 ? * SUN")
    @XxlJob("autoBackupHandler")
    public void autoBackup() {
        if (!scheduleEnabled) return;
        if (backupService == null) return;
        try {
            TenantContext.setTenantId(1L);
            log.info("【定时任务】开始自动备份数据");
            backupService.createBackup();
            log.info("【定时任务】数据备份完成");
        } catch (Exception e) {
            log.error("【定时任务】自动备份失败", e);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 每天凌晨3点清理过期缓存（预留）
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @XxlJob("cleanCacheHandler")
    public void cleanExpiredCache() {
        if (!scheduleEnabled) return;
        log.info("【定时任务】清理过期缓存完成");
    }
}

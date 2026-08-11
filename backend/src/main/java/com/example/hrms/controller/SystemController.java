package com.example.hrms.controller;

import com.example.hrms.common.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统监控控制器
 */
@RestController
@RequestMapping("/api/system")
public class SystemController {

    /**
     * 获取系统监控信息
     */
    @GetMapping("/monitor")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> monitor() {
        Map<String, Object> result = new HashMap<>();

        // 1. 操作系统信息
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Map<String, Object> os = new HashMap<>();
        os.put("name", osBean.getName());
        os.put("arch", osBean.getArch());
        os.put("version", osBean.getVersion());
        os.put("processors", osBean.getAvailableProcessors());
        // 系统负载（部分系统支持）
        double systemLoad = osBean.getSystemLoadAverage();
        os.put("systemLoadAverage", systemLoad < 0 ? "N/A" :
                BigDecimal.valueOf(systemLoad).setScale(2, RoundingMode.HALF_UP).toString());
        result.put("os", os);

        // 2. JVM 信息
        Runtime runtime = Runtime.getRuntime();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        Map<String, Object> jvm = new HashMap<>();
        jvm.put("javaVersion", System.getProperty("java.version"));
        jvm.put("javaVendor", System.getProperty("java.vendor"));
        jvm.put("vmName", memoryBean.getObjectName().toString());

        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        jvm.put("maxMemory", formatSize(maxMemory));
        jvm.put("totalMemory", formatSize(totalMemory));
        jvm.put("usedMemory", formatSize(usedMemory));
        jvm.put("freeMemory", formatSize(freeMemory));
        jvm.put("memoryUsagePercent", BigDecimal.valueOf(usedMemory * 100.0 / maxMemory)
                .setScale(1, RoundingMode.HALF_UP).doubleValue());
        result.put("jvm", jvm);

        // 3. 应用运行信息
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        Map<String, Object> app = new HashMap<>();
        app.put("startTime", new Date(runtimeBean.getStartTime()));
        long uptimeMs = runtimeBean.getUptime();
        app.put("uptime", formatUptime(uptimeMs));
        app.put("uptimeSeconds", uptimeMs / 1000);
        try {
            app.put("hostAddress", InetAddress.getLocalHost().getHostAddress());
            app.put("hostName", InetAddress.getLocalHost().getHostName());
        } catch (Exception e) {
            app.put("hostAddress", "N/A");
            app.put("hostName", "N/A");
        }
        result.put("app", app);

        // 4. 磁盘信息
        Map<String, Object> disk = new HashMap<>();
        java.io.File[] roots = java.io.File.listRoots();
        if (roots != null && roots.length > 0) {
            java.io.File root = roots[0];
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            disk.put("totalSpace", formatSize(totalSpace));
            disk.put("usedSpace", formatSize(usedSpace));
            disk.put("freeSpace", formatSize(freeSpace));
            disk.put("usagePercent", BigDecimal.valueOf(usedSpace * 100.0 / totalSpace)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue());
        }
        result.put("disk", disk);

        // 5. 线程信息
        Map<String, Object> thread = new HashMap<>();
        thread.put("threadCount", Thread.activeCount());
        thread.put("peakThreadCount", ManagementFactory.getThreadMXBean().getPeakThreadCount());
        result.put("thread", thread);

        return Result.success(result);
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return BigDecimal.valueOf(bytes / 1024.0)
                .setScale(2, RoundingMode.HALF_UP) + " KB";
        if (bytes < 1024 * 1024 * 1024) return BigDecimal.valueOf(bytes / (1024.0 * 1024))
                .setScale(2, RoundingMode.HALF_UP) + " MB";
        return BigDecimal.valueOf(bytes / (1024.0 * 1024 * 1024))
                .setScale(2, RoundingMode.HALF_UP) + " GB";
    }

    private String formatUptime(long ms) {
        long seconds = ms / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("天");
        if (hours > 0) sb.append(hours).append("小时");
        if (minutes > 0) sb.append(minutes).append("分");
        sb.append(secs).append("秒");
        return sb.toString();
    }
}

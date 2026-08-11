<template>
  <div>
    <!-- 概览卡片 -->
    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
            <el-icon :size="24"><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ data?.app?.uptime || '—' }}</div>
            <div class="stat-label">运行时长</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%)">
            <el-icon :size="24"><Cpu /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ data?.os?.processors || '—' }} 核</div>
            <div class="stat-label">CPU 核心数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
            <el-icon :size="24"><Coin /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ data?.thread?.threadCount || '—' }}</div>
            <div class="stat-label">活跃线程数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
            <el-icon :size="24"><DataLine /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ data?.jvm?.memoryUsagePercent || 0 }}%</div>
            <div class="stat-label">JVM 内存使用率</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- 操作系统信息 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span><el-icon style="margin-right: 6px"><Monitor /></el-icon>操作系统信息</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="操作系统">{{ data?.os?.name || '—' }}</el-descriptions-item>
            <el-descriptions-item label="系统架构">{{ data?.os?.arch || '—' }}</el-descriptions-item>
            <el-descriptions-item label="系统版本">{{ data?.os?.version || '—' }}</el-descriptions-item>
            <el-descriptions-item label="CPU 核心数">{{ data?.os?.processors || '—' }} 核</el-descriptions-item>
            <el-descriptions-item label="系统负载">{{ data?.os?.systemLoadAverage || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- JVM 信息 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span><el-icon style="margin-right: 6px"><Cpu /></el-icon>JVM 信息</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="Java 版本">{{ data?.jvm?.javaVersion || '—' }}</el-descriptions-item>
            <el-descriptions-item label="Java 厂商">{{ data?.jvm?.javaVendor || '—' }}</el-descriptions-item>
            <el-descriptions-item label="最大内存">{{ data?.jvm?.maxMemory || '—' }}</el-descriptions-item>
            <el-descriptions-item label="已用内存">{{ data?.jvm?.usedMemory || '—' }}</el-descriptions-item>
            <el-descriptions-item label="空闲内存">{{ data?.jvm?.freeMemory || '—' }}</el-descriptions-item>
          </el-descriptions>
          <div style="margin-top: 12px">
            <div style="display: flex; justify-content: space-between; margin-bottom: 4px; font-size: 13px">
              <span>内存使用率</span>
              <span>{{ data?.jvm?.memoryUsagePercent || 0 }}%</span>
            </div>
            <el-progress :percentage="data?.jvm?.memoryUsagePercent || 0" :color="progressColor" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <!-- 磁盘信息 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span><el-icon style="margin-right: 6px"><FolderOpened /></el-icon>磁盘信息</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="总容量">{{ data?.disk?.totalSpace || '—' }}</el-descriptions-item>
            <el-descriptions-item label="已用空间">{{ data?.disk?.usedSpace || '—' }}</el-descriptions-item>
            <el-descriptions-item label="可用空间">{{ data?.disk?.freeSpace || '—' }}</el-descriptions-item>
          </el-descriptions>
          <div style="margin-top: 12px">
            <div style="display: flex; justify-content: space-between; margin-bottom: 4px; font-size: 13px">
              <span>磁盘使用率</span>
              <span>{{ data?.disk?.usagePercent || 0 }}%</span>
            </div>
            <el-progress :percentage="data?.disk?.usagePercent || 0" :color="diskColor" />
          </div>
        </el-card>
      </el-col>

      <!-- 应用信息 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span><el-icon style="margin-right: 6px"><InfoFilled /></el-icon>应用信息</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="主机名">{{ data?.app?.hostName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="主机地址">{{ data?.app?.hostAddress || '—' }}</el-descriptions-item>
            <el-descriptions-item label="启动时间">{{ formatTime(data?.app?.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="运行时长">{{ data?.app?.uptime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="峰值线程数">{{ data?.thread?.peakThreadCount || '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <div style="margin-top: 16px; text-align: center">
      <el-button type="primary" :icon="Refresh" @click="loadData" :loading="loading">刷新数据</el-button>
      <span style="margin-left: 12px; color: #999; font-size: 13px">数据每 10 秒自动刷新</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { Timer, Cpu, Coin, DataLine, Monitor, FolderOpened, InfoFilled, Refresh } from '@element-plus/icons-vue'
import { getSystemMonitor } from '@/api'

const data = ref(null)
const loading = ref(false)
let timer = null

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSystemMonitor()
    data.value = res.data
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return '—'
  const d = new Date(time)
  return d.toLocaleString('zh-CN')
}

const progressColor = computed(() => {
  const percent = data.value?.jvm?.memoryUsagePercent || 0
  if (percent < 60) return '#67c23a'
  if (percent < 80) return '#e6a23c'
  return '#f56c6c'
})

const diskColor = computed(() => {
  const percent = data.value?.disk?.usagePercent || 0
  if (percent < 60) return '#67c23a'
  if (percent < 80) return '#e6a23c'
  return '#f56c6c'
})

onMounted(() => {
  loadData()
  timer = setInterval(loadData, 10000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 16px;
  flex-shrink: 0;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
</style>

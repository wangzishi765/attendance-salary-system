<template>
  <div>
    <!-- 员工：打卡卡片 -->
    <el-card v-if="!userStore.isAdminOrHr" class="clock-card" shadow="hover">
      <div class="clock-content">
        <div class="clock-info">
          <div class="clock-time">{{ nowTime }}</div>
          <div class="clock-date">{{ nowDate }}</div>
        </div>
        <div class="clock-status">
          <p>上班打卡：<b>{{ todayRecord?.checkInTime ? formatTime(todayRecord.checkInTime) : '未打卡' }}</b>
            <el-tag v-if="todayRecord?.status === 'LATE'" type="warning" size="small">迟到</el-tag>
          </p>
          <p>下班打卡：<b>{{ todayRecord?.checkOutTime ? formatTime(todayRecord.checkOutTime) : '未打卡' }}</b></p>
        </div>
        <div class="clock-btns">
          <el-button type="primary" size="large" round @click="doCheckIn">上班打卡</el-button>
          <el-button type="success" size="large" round @click="doCheckOut">下班打卡</el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col v-for="c in cards" :key="c.label" :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" :style="{ background: c.color }">
            <el-icon :size="26"><component :is="c.icon" /></el-icon>
          </div>
          <div class="stat-text">
            <div class="stat-value">{{ c.value }}</div>
            <div class="stat-label">{{ c.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16">
      <el-col :xs="24" :sm="12">
        <el-card shadow="never">
          <template #header>{{ userStore.isAdminOrHr ? '各部门人数分布' : '本月考勤构成' }}</template>
          <div ref="pieRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-card shadow="never">
          <template #header>{{ userStore.isAdminOrHr ? '本月各员工实发工资' : '本月考勤天数' }}</template>
          <div ref="barRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { useUserStore } from '@/store/user'
import {
  getDashboardStat, getTodayAttendance, checkIn, checkOut,
  getAttendanceStat, listAllEmployees, pagePayrolls
} from '@/api'

const userStore = useUserStore()
const nowTime = ref('')
const nowDate = ref('')
const todayRecord = ref(null)
const stat = reactive({ employeeCount: 0, departmentCount: 0, todayAttendance: 0, pendingLeave: 0 })
const empStat = reactive({ total: 0, normal: 0, late: 0, early: 0, absent: 0 })

const pieRef = ref(null)
const barRef = ref(null)
let pieChart = null
let barChart = null
let timer = null

const currentMonth = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

const cards = computed(() => {
  if (userStore.isAdminOrHr) {
    return [
      { label: '员工总数', value: stat.employeeCount, icon: 'User', color: 'linear-gradient(135deg, #667eea, #764ba2)' },
      { label: '部门数', value: stat.departmentCount, icon: 'OfficeBuilding', color: 'linear-gradient(135deg, #11998e, #38ef7d)' },
      { label: '今日出勤', value: stat.todayAttendance, icon: 'Clock', color: 'linear-gradient(135deg, #f093fb, #f5576c)' },
      { label: '待审批请假', value: stat.pendingLeave, icon: 'Calendar', color: 'linear-gradient(135deg, #4facfe, #00f2fe)' }
    ]
  }
  return [
    { label: '本月出勤', value: empStat.total, icon: 'Clock', color: 'linear-gradient(135deg, #667eea, #764ba2)' },
    { label: '正常', value: empStat.normal, icon: 'CircleCheck', color: 'linear-gradient(135deg, #11998e, #38ef7d)' },
    { label: '迟到', value: empStat.late, icon: 'Warning', color: 'linear-gradient(135deg, #f093fb, #f5576c)' },
    { label: '缺勤', value: empStat.absent, icon: 'CircleClose', color: 'linear-gradient(135deg, #4facfe, #00f2fe)' }
  ]
})

const updateClock = () => {
  const d = new Date()
  nowTime.value = d.toLocaleTimeString('zh-CN')
  nowDate.value = d.toLocaleDateString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })
}

const formatTime = (t) => (t ? t.substring(11, 19) : '')

const loadToday = async () => {
  const res = await getTodayAttendance()
  todayRecord.value = res.data
}

const doCheckIn = async () => { await checkIn(); ElMessage.success('上班打卡成功'); loadToday() }
const doCheckOut = async () => { await checkOut(); ElMessage.success('下班打卡成功'); loadToday() }

const renderPie = (data) => {
  if (!pieChart) pieChart = echarts.init(pieRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    color: ['#667eea', '#11998e', '#f093fb', '#4facfe', '#fc6076', '#ffd26f'],
    series: [{
      type: 'pie', radius: ['40%', '68%'], center: ['50%', '45%'],
      label: { formatter: '{b}: {c}' },
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      data
    }]
  })
}

const renderBar = (names, values, unit) => {
  if (!barChart) barChart = echarts.init(barRef.value)
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: names, axisLine: { lineStyle: { color: '#ddd' } } },
    yAxis: { type: 'value', name: unit, axisLine: { show: false }, splitLine: { lineStyle: { color: '#f0f0f0' } } },
    series: [{
      type: 'bar', data: values, barWidth: '45%',
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#667eea' },
          { offset: 1, color: '#764ba2' }
        ])
      }
    }]
  })
}

const loadAdminCharts = async () => {
  // 部门人数分布
  const empRes = await listAllEmployees()
  const deptMap = {}
  empRes.data.forEach((e) => {
    const key = e.departmentName || '未分配'
    deptMap[key] = (deptMap[key] || 0) + 1
  })
  renderPie(Object.keys(deptMap).map((k) => ({ name: k, value: deptMap[k] })))

  // 本月各员工实发工资
  const payRes = await pagePayrolls({ current: 1, size: 100, month: currentMonth() })
  const records = payRes.data.records
  renderBar(records.map((r) => r.employeeName), records.map((r) => Number(r.netSalary)), '元')
}

const loadEmployeeCharts = async () => {
  renderPie([
    { name: '正常', value: empStat.normal },
    { name: '迟到', value: empStat.late },
    { name: '早退', value: empStat.early },
    { name: '缺勤', value: empStat.absent }
  ].filter((i) => i.value > 0))
  renderBar(['正常', '迟到', '早退', '缺勤'],
    [empStat.normal, empStat.late, empStat.early, empStat.absent], '天')
}

const resize = () => { pieChart?.resize(); barChart?.resize() }

onMounted(async () => {
  updateClock()
  timer = setInterval(updateClock, 1000)
  if (userStore.isAdminOrHr) {
    const res = await getDashboardStat()
    Object.assign(stat, res.data)
    await nextTick()
    loadAdminCharts()
  } else {
    loadToday()
    const res = await getAttendanceStat({ month: currentMonth() })
    Object.assign(empStat, res.data)
    await nextTick()
    loadEmployeeCharts()
  }
  window.addEventListener('resize', resize)
})

onUnmounted(() => {
  clearInterval(timer)
  window.removeEventListener('resize', resize)
  pieChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.clock-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
}
.clock-card :deep(.el-card__body) {
  padding: 24px;
}
.clock-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}
.clock-time {
  font-size: 42px;
  font-weight: bold;
  color: #fff;
}
.clock-date {
  color: rgba(255, 255, 255, 0.85);
  margin-top: 4px;
}
.clock-status p {
  margin: 6px 0;
  color: rgba(255, 255, 255, 0.9);
}
.clock-btns {
  display: flex;
  gap: 12px;
}
.clock-btns :deep(.el-button) {
  border-radius: 20px;
  padding: 0 24px;
}
.stat-row {
  margin-bottom: 20px;
}
.stat-card {
  margin-bottom: 20px;
  border: none;
  border-radius: 12px;
  transition: all 0.3s;
}
.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}
.stat-label {
  color: #888;
  font-size: 14px;
  margin-top: 4px;
}
.chart {
  height: 320px;
  width: 100%;
}
:deep(.el-card) {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
:deep(.el-card__header) {
  border-bottom: 1px solid #f0f0f0;
  font-weight: 600;
  color: #333;
}
</style>

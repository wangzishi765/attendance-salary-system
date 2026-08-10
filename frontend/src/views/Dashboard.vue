<template>
  <div>
    <!-- 员工：打卡卡片 -->
    <el-card v-if="!userStore.isAdmin" class="clock-card" shadow="hover">
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
          <template #header>{{ userStore.isAdmin ? '各部门人数分布' : '本月考勤构成' }}</template>
          <div ref="pieRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-card shadow="never">
          <template #header>{{ userStore.isAdmin ? '本月各员工实发工资' : '本月考勤天数' }}</template>
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
  if (userStore.isAdmin) {
    return [
      { label: '员工总数', value: stat.employeeCount, icon: 'User', color: '#1890ff' },
      { label: '部门数', value: stat.departmentCount, icon: 'OfficeBuilding', color: '#52c41a' },
      { label: '今日出勤', value: stat.todayAttendance, icon: 'Clock', color: '#faad14' },
      { label: '待审批请假', value: stat.pendingLeave, icon: 'Calendar', color: '#f5222d' }
    ]
  }
  return [
    { label: '本月出勤', value: empStat.total, icon: 'Clock', color: '#1890ff' },
    { label: '正常', value: empStat.normal, icon: 'CircleCheck', color: '#52c41a' },
    { label: '迟到', value: empStat.late, icon: 'Warning', color: '#faad14' },
    { label: '缺勤', value: empStat.absent, icon: 'CircleClose', color: '#f5222d' }
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
    color: ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1', '#13c2c2'],
    series: [{
      type: 'pie', radius: ['40%', '68%'], center: ['50%', '45%'],
      label: { formatter: '{b}: {c}' },
      data
    }]
  })
}

const renderBar = (names, values, unit) => {
  if (!barChart) barChart = echarts.init(barRef.value)
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: names },
    yAxis: { type: 'value', name: unit },
    series: [{
      type: 'bar', data: values, barWidth: '45%',
      itemStyle: { color: '#1890ff', borderRadius: [4, 4, 0, 0] }
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
  if (userStore.isAdmin) {
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
.clock-card { margin-bottom: 16px; background: linear-gradient(135deg, #e6f7ff, #ffffff); }
.clock-content { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 16px; }
.clock-time { font-size: 40px; font-weight: bold; color: #1890ff; }
.clock-date { color: #666; }
.clock-status p { margin: 6px 0; color: #555; }
.clock-btns { display: flex; gap: 12px; }
.stat-row { margin-bottom: 16px; }
.stat-card { margin-bottom: 16px; }
.stat-card :deep(.el-card__body) { display: flex; align-items: center; gap: 14px; }
.stat-icon { width: 54px; height: 54px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; }
.stat-value { font-size: 26px; font-weight: bold; }
.stat-label { color: #999; font-size: 13px; }
.chart { height: 300px; width: 100%; }
</style>

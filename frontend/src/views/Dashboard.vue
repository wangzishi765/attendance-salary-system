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

    <el-card shadow="never" class="welcome">
      <h3>欢迎使用，{{ userStore.realName || userStore.username }}！</h3>
      <p v-if="userStore.isAdmin">您可以在左侧菜单管理员工、部门、考勤、请假、加班，配置薪资规则并生成工资单。</p>
      <p v-else>您可以在此打卡，并在左侧查看个人考勤、提交请假/加班申请、查看工资单。</p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import {
  getDashboardStat, getTodayAttendance, checkIn, checkOut, getAttendanceStat
} from '@/api'

const userStore = useUserStore()
const nowTime = ref('')
const nowDate = ref('')
const todayRecord = ref(null)
const stat = reactive({ employeeCount: 0, departmentCount: 0, todayAttendance: 0, pendingLeave: 0 })
const empStat = reactive({ total: 0, normal: 0, late: 0, absent: 0 })

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

const doCheckIn = async () => {
  await checkIn()
  ElMessage.success('上班打卡成功')
  loadToday()
}
const doCheckOut = async () => {
  await checkOut()
  ElMessage.success('下班打卡成功')
  loadToday()
}

onMounted(async () => {
  updateClock()
  timer = setInterval(updateClock, 1000)
  if (userStore.isAdmin) {
    const res = await getDashboardStat()
    Object.assign(stat, res.data)
  } else {
    loadToday()
    const res = await getAttendanceStat({ month: currentMonth() })
    Object.assign(empStat, res.data)
  }
})

onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.clock-card {
  margin-bottom: 16px;
  background: linear-gradient(135deg, #e6f7ff, #ffffff);
}
.clock-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}
.clock-time {
  font-size: 40px;
  font-weight: bold;
  color: #1890ff;
}
.clock-date {
  color: #666;
}
.clock-status p {
  margin: 6px 0;
  color: #555;
}
.clock-btns {
  display: flex;
  gap: 12px;
}
.stat-row {
  margin-bottom: 16px;
}
.stat-card {
  margin-bottom: 16px;
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 14px;
}
.stat-icon {
  width: 54px;
  height: 54px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.stat-value {
  font-size: 26px;
  font-weight: bold;
}
.stat-label {
  color: #999;
  font-size: 13px;
}
.welcome h3 {
  margin-bottom: 8px;
}
.welcome p {
  color: #666;
}
</style>

<template>
  <div class="notification-page">
    <div class="page-header">
      <h2>消息通知</h2>
      <div class="header-actions">
        <el-select v-model="filterRead" placeholder="全部状态" style="width: 120px; margin-right: 10px" @change="loadList">
          <el-option label="全部" :value="null" />
          <el-option label="未读" :value="0" />
          <el-option label="已读" :value="1" />
        </el-select>
        <el-button type="primary" @click="markAllRead" :disabled="unreadCount === 0">全部标记已读</el-button>
      </div>
    </div>

    <el-card class="list-card">
      <div v-if="list.length === 0" class="empty-tip">
        <el-empty description="暂无消息" />
      </div>
      <div v-else class="notification-list">
        <div
          v-for="item in list"
          :key="item.id"
          class="notification-item"
          :class="{ unread: item.isRead === 0 }"
          @click="handleRead(item)"
        >
          <div class="msg-icon" :class="item.type">
            <el-icon><Bell /></el-icon>
          </div>
          <div class="msg-content">
            <div class="msg-title">
              {{ item.title }}
              <el-tag v-if="item.isRead === 0" size="small" type="danger" class="unread-tag">未读</el-tag>
            </div>
            <div class="msg-body">{{ item.content }}</div>
            <div class="msg-meta">
              <el-tag size="small" :type="typeTagType(item.type)">{{ typeLabel(item.type) }}</el-tag>
              <span class="msg-time">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="current"
          v-model:page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import { getNotifications, markNotificationRead, markAllNotificationsRead, getUnreadCount } from '../api'

const list = ref([])
const current = ref(1)
const size = ref(10)
const total = ref(0)
const filterRead = ref(null)
const unreadCount = ref(0)

const loadList = async () => {
  const params = { current: current.value, size: size.value }
  if (filterRead.value !== null) params.isRead = filterRead.value
  const res = await getNotifications(params)
  list.value = res.data.records
  total.value = res.data.total
}

const loadUnread = async () => {
  const res = await getUnreadCount()
  unreadCount.value = res.data
}

const handleRead = async (item) => {
  if (item.isRead === 0) {
    await markNotificationRead(item.id)
    item.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
}

const markAllRead = async () => {
  await markAllNotificationsRead()
  ElMessage.success('已全部标记为已读')
  list.value.forEach(i => i.isRead = 1)
  unreadCount.value = 0
}

const typeLabel = (type) => {
  const map = { SYSTEM: '系统通知', APPROVAL: '审批通知', ATTENDANCE: '考勤通知', PAYROLL: '薪资通知' }
  return map[type] || '通知'
}

const typeTagType = (type) => {
  const map = { SYSTEM: '', APPROVAL: 'warning', ATTENDANCE: 'success', PAYROLL: 'primary' }
  return map[type] || ''
}

const formatTime = (t) => {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  loadList()
  loadUnread()
})
</script>

<style scoped>
.notification-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; }
.header-actions { display: flex; align-items: center; }
.list-card { background: #fff; border-radius: 8px; }
.notification-list { display: flex; flex-direction: column; }
.notification-item {
  display: flex; padding: 16px; border-bottom: 1px solid #f0f0f0;
  cursor: pointer; transition: background 0.2s;
}
.notification-item:hover { background: #f9f9ff; }
.notification-item.unread { background: #fef9f9; }
.msg-icon {
  width: 40px; height: 40px; border-radius: 50%; display: flex;
  align-items: center; justify-content: center; margin-right: 14px;
  font-size: 18px; color: #fff; flex-shrink: 0;
}
.msg-icon.SYSTEM { background: linear-gradient(135deg, #667eea, #764ba2); }
.msg-icon.APPROVAL { background: linear-gradient(135deg, #f093fb, #f5576c); }
.msg-icon.ATTENDANCE { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.msg-icon.PAYROLL { background: linear-gradient(135deg, #43e97b, #38f9d7); }
.msg-content { flex: 1; }
.msg-title { font-size: 15px; font-weight: 600; margin-bottom: 6px; display: flex; align-items: center; gap: 8px; }
.unread-tag { font-size: 11px; }
.msg-body { font-size: 13px; color: #666; margin-bottom: 8px; line-height: 1.5; }
.msg-meta { display: flex; align-items: center; gap: 10px; font-size: 12px; color: #999; }
.msg-time { color: #bbb; }
.pagination-wrap { display: flex; justify-content: center; padding: 16px 0 8px; }
.empty-tip { padding: 40px 0; }
</style>

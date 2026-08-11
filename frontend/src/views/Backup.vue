<template>
  <div class="backup-page">
    <div class="page-header">
      <h2>数据备份</h2>
      <el-button type="primary" @click="createBackup" :loading="creating">
        <el-icon><Download /></el-icon>
        立即备份
      </el-button>
    </div>

    <el-card class="info-card">
      <el-alert
        title="数据备份说明"
        type="info"
        :closable="false"
        description="系统会将当前数据库文件打包为zip文件保存到服务器backup目录。建议定期备份，防止数据丢失。恢复数据需要手动替换数据库文件后重启服务。"
      />
    </el-card>

    <el-card class="list-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="name" label="备份文件名" min-width="220" />
        <el-table-column prop="size" label="大小" width="120">
          <template #default="{ row }">{{ formatSize(row.size) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="备份时间" width="200">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="download(row.name)">下载</el-button>
            <el-button type="danger" link @click="deleteBackup(row.name)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="list.length === 0 && !loading" class="empty-tip">
        <el-empty description="暂无备份文件" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { createBackupApi, listBackups, deleteBackupApi, downloadBackup } from '../api'

const list = ref([])
const loading = ref(false)
const creating = ref(false)

const loadList = async () => {
  loading.value = true
  try {
    const res = await listBackups()
    list.value = res.data
  } finally {
    loading.value = false
  }
}

const createBackup = async () => {
  creating.value = true
  try {
    const res = await createBackupApi()
    ElMessage.success('备份创建成功')
    loadList()
  } catch (e) {
    ElMessage.error('备份失败')
  } finally {
    creating.value = false
  }
}

const download = async (name) => {
  try {
    const res = await downloadBackup(name)
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', name)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error('下载失败')
  }
}

const deleteBackup = async (name) => {
  try {
    await ElMessageBox.confirm('确定删除该备份文件吗？', '提示', { type: 'warning' })
    await deleteBackupApi(name)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {}
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(2) + ' MB'
}

const formatTime = (t) => {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 19)
}

onMounted(loadList)
</script>

<style scoped>
.backup-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; }
.info-card { margin-bottom: 16px; }
.list-card { background: #fff; border-radius: 8px; }
.empty-tip { padding: 40px 0; }
</style>

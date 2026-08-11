<template>
  <div>
    <el-card shadow="never" class="workflow-header">
      <div class="header-content">
        <h2>🔄 工作流审批中心</h2>
        <el-radio-group v-model="activeTab" @change="loadData">
          <el-radio-button label="pending">⏳ 待我审批</el-radio-button>
          <el-radio-button label="approved">✅ 我已审批</el-radio-button>
          <el-radio-button label="started">📝 我发起的</el-radio-button>
        </el-radio-group>
      </div>
    </el-card>

    <!-- 待我审批 -->
    <el-card shadow="never" v-if="activeTab === 'pending'">
      <el-table :data="pendingList" stripe>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="processName" label="流程类型" width="140" />
        <el-table-column prop="initiatorName" label="发起人" width="100" />
        <el-table-column prop="nodeName" label="当前节点" width="140" />
        <el-table-column prop="startTime" label="发起时间" width="170">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showApproveDialog(row)">审批</el-button>
            <el-button type="info" size="small" @click="showDetail(row.instanceId)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pendingPage.current"
        v-model:page-size="pendingPage.size"
        :total="pendingPage.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadPending"
        @current-change="loadPending"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 我已审批 -->
    <el-card shadow="never" v-if="activeTab === 'approved'">
      <el-table :data="approvedList" stripe>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="processName" label="流程类型" width="140" />
        <el-table-column prop="initiatorName" label="发起人" width="100" />
        <el-table-column prop="nodeName" label="审批节点" width="140" />
        <el-table-column label="审批结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'APPROVED' ? 'success' : 'danger'">
              {{ row.status === 'APPROVED' ? '通过' : '拒绝' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approveTime" label="审批时间" width="170">
          <template #default="{ row }">{{ formatTime(row.approveTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="info" size="small" @click="showDetail(row.instanceId)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="approvedPage.current"
        v-model:page-size="approvedPage.size"
        :total="approvedPage.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadApproved"
        @current-change="loadApproved"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 我发起的 -->
    <el-card shadow="never" v-if="activeTab === 'started'">
      <el-table :data="startedList" stripe>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="processName" label="流程类型" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="发起时间" width="170">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="170">
          <template #default="{ row }">{{ row.endTime ? formatTime(row.endTime) : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="info" size="small" @click="showDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="startedPage.current"
        v-model:page-size="startedPage.size"
        :total="startedPage.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadStarted"
        @current-change="loadStarted"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 审批弹窗 -->
    <el-dialog v-model="approveDialogVisible" title="审批" width="500px">
      <div class="approve-info">
        <p><b>标题：</b>{{ currentTask?.title }}</p>
        <p><b>流程：</b>{{ currentTask?.processName }}</p>
        <p><b>发起人：</b>{{ currentTask?.initiatorName }}</p>
        <p><b>当前节点：</b>{{ currentTask?.nodeName }}</p>
      </div>
      <el-form label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="approveForm.comment" type="textarea" :rows="3" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="doApprove('REJECTED')">拒绝</el-button>
        <el-button type="success" @click="doApprove('APPROVED')">通过</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="流程详情" width="600px">
      <div v-if="instanceDetail" class="detail-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题">{{ instanceDetail.title }}</el-descriptions-item>
          <el-descriptions-item label="流程类型">{{ instanceDetail.processName }}</el-descriptions-item>
          <el-descriptions-item label="发起人">{{ instanceDetail.initiatorName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(instanceDetail.status)">{{ getStatusText(instanceDetail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发起时间">{{ formatTime(instanceDetail.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ instanceDetail.endTime ? formatTime(instanceDetail.endTime) : '-' }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 20px 0 10px">审批历史</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(task, index) in approvalHistory"
            :key="task.id"
            :timestamp="task.approveTime ? formatTime(task.approveTime) : '待审批'"
            :type="task.status === 'APPROVED' ? 'success' : task.status === 'REJECTED' ? 'danger' : 'primary'"
          >
            <div class="timeline-content">
              <b>{{ task.nodeName }}</b>
              <span v-if="task.approverName"> - {{ task.approverName }}</span>
              <el-tag v-if="task.status !== 'PENDING'" :type="task.status === 'APPROVED' ? 'success' : 'danger'" size="small" style="margin-left: 8px">
                {{ task.status === 'APPROVED' ? '通过' : '拒绝' }}
              </el-tag>
              <el-tag v-else type="warning" size="small" style="margin-left: 8px">审批中</el-tag>
              <p v-if="task.comment" style="margin: 8px 0 0; color: #606266">意见：{{ task.comment }}</p>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getWorkflowPending, getWorkflowApproved, getWorkflowStarted,
  approveWorkflowTask, getWorkflowInstance, getWorkflowHistory
} from '@/api'

const activeTab = ref('pending')
const pendingList = ref([])
const approvedList = ref([])
const startedList = ref([])

const pendingPage = reactive({ current: 1, size: 10, total: 0 })
const approvedPage = reactive({ current: 1, size: 10, total: 0 })
const startedPage = reactive({ current: 1, size: 10, total: 0 })

const approveDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentTask = ref(null)
const approveForm = reactive({ comment: '' })
const instanceDetail = ref(null)
const approvalHistory = ref([])

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

function getStatusType(status) {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', CANCELLED: 'info' }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = { PENDING: '审批中', APPROVED: '已通过', REJECTED: '已拒绝', CANCELLED: '已取消' }
  return map[status] || status
}

async function loadPending() {
  const res = await getWorkflowPending(pendingPage.current, pendingPage.size)
  pendingList.value = res.data.records
  pendingPage.total = res.data.total
}

async function loadApproved() {
  const res = await getWorkflowApproved(approvedPage.current, approvedPage.size)
  approvedList.value = res.data.records
  approvedPage.total = res.data.total
}

async function loadStarted() {
  const res = await getWorkflowStarted(startedPage.current, startedPage.size)
  startedList.value = res.data.records
  startedPage.total = res.data.total
}

function loadData() {
  if (activeTab.value === 'pending') loadPending()
  else if (activeTab.value === 'approved') loadApproved()
  else loadStarted()
}

function showApproveDialog(row) {
  currentTask.value = row
  approveForm.comment = ''
  approveDialogVisible.value = true
}

async function doApprove(status) {
  try {
    await approveWorkflowTask(currentTask.value.taskId, status, approveForm.comment)
    ElMessage.success(status === 'APPROVED' ? '审批通过' : '已拒绝')
    approveDialogVisible.value = false
    loadPending()
  } catch (e) {
    ElMessage.error('审批失败')
  }
}

async function showDetail(instanceId) {
  const [instanceRes, historyRes] = await Promise.all([
    getWorkflowInstance(instanceId),
    getWorkflowHistory(instanceId)
  ])
  instanceDetail.value = instanceRes.data
  approvalHistory.value = historyRes.data
  detailDialogVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.workflow-header {
  margin-bottom: 16px;
}
.header-content {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}
.header-content h2 {
  margin: 0;
  font-size: 20px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.approve-info {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}
.approve-info p {
  margin: 8px 0;
}
.timeline-content {
  padding: 4px 0;
}
</style>

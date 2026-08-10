<template>
  <div>
    <el-card shadow="never">
      <div class="page-toolbar">
        <el-select v-model="query.status" placeholder="审批状态" clearable style="width: 140px" @change="loadData">
          <el-option label="待审批" value="PENDING" />
          <el-option label="已批准" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        <el-button type="success" :icon="Plus" @click="openApply">申请请假</el-button>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column v-if="userStore.isAdminOrHr" prop="employeeName" label="员工" width="90" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">{{ typeText(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始" width="120" />
        <el-table-column prop="endDate" label="结束" width="120" />
        <el-table-column prop="days" label="天数" width="70" />
        <el-table-column prop="reason" label="事由" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="approver" label="审批人" width="90" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <template v-if="userStore.isAdminOrHr && row.status === 'PENDING'">
              <el-button link type="success" @click="audit(row.id, 'APPROVED')">批准</el-button>
              <el-button link type="danger" @click="audit(row.id, 'REJECTED')">驳回</el-button>
            </template>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pager" layout="total, prev, pager, next" :total="total"
        :page-size="query.size" :current-page="query.current" @current-change="handlePage" />
    </el-card>

    <el-dialog v-model="dialogVisible" title="申请请假" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item v-if="userStore.isAdminOrHr" label="员工">
          <el-select v-model="form.employeeId" filterable placeholder="选择员工" style="width: 100%">
            <el-option v-for="e in employees" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="病假" value="SICK" />
            <el-option label="事假" value="PERSONAL" />
            <el-option label="年假" value="ANNUAL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD"
            range-separator="至" start-placeholder="开始" end-placeholder="结束" style="width: 100%" @change="calcDays" />
        </el-form-item>
        <el-form-item label="天数">
          <el-input-number v-model="form.days" :min="0.5" :step="0.5" />
        </el-form-item>
        <el-form-item label="事由">
          <el-input v-model="form.reason" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { pageLeaves, applyLeave, auditLeave, deleteLeave, listAllEmployees } from '@/api'

const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const employees = ref([])
const dialogVisible = ref(false)
const dateRange = ref([])

const query = reactive({ current: 1, size: 10, status: '' })
const form = reactive({ employeeId: null, type: 'PERSONAL', startDate: '', endDate: '', days: 1, reason: '' })

const typeText = (t) => ({ SICK: '病假', PERSONAL: '事假', ANNUAL: '年假', OTHER: '其他' }[t] || t)
const statusText = (s) => ({ PENDING: '待审批', APPROVED: '已批准', REJECTED: '已驳回' }[s] || s)
const statusType = (s) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s] || 'info')

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageLeaves(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handlePage = (p) => { query.current = p; loadData() }

const calcDays = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    form.startDate = dateRange.value[0]
    form.endDate = dateRange.value[1]
    const d1 = new Date(dateRange.value[0])
    const d2 = new Date(dateRange.value[1])
    form.days = Math.round((d2 - d1) / 86400000) + 1
  }
}

const openApply = () => {
  Object.assign(form, { employeeId: null, type: 'PERSONAL', startDate: '', endDate: '', days: 1, reason: '' })
  dateRange.value = []
  dialogVisible.value = true
}

const submitApply = async () => {
  if (!form.startDate || !form.endDate) { ElMessage.warning('请选择请假日期'); return }
  await applyLeave(form)
  ElMessage.success('申请已提交')
  dialogVisible.value = false
  loadData()
}

const audit = async (id, status) => {
  await auditLeave(id, status)
  ElMessage.success('操作成功')
  loadData()
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteLeave(id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

onMounted(async () => {
  if (userStore.isAdminOrHr) {
    const res = await listAllEmployees()
    employees.value = res.data
  }
  loadData()
})
</script>

<style scoped>
.pager { margin-top: 16px; justify-content: flex-end; }
</style>

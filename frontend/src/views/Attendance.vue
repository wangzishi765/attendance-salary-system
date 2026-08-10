<template>
  <div>
    <el-card shadow="never">
      <div class="page-toolbar">
        <el-select
          v-if="userStore.isAdmin"
          v-model="query.employeeId"
          placeholder="选择员工"
          clearable
          filterable
          style="width: 180px"
          @change="loadData"
        >
          <el-option v-for="e in employees" :key="e.id" :label="e.name" :value="e.id" />
        </el-select>
        <el-date-picker
          v-model="query.month"
          type="month"
          value-format="YYYY-MM"
          placeholder="选择月份"
          @change="loadData"
        />
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        <el-button v-if="userStore.isAdmin" type="success" :icon="Plus" @click="openManual">补录考勤</el-button>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column v-if="userStore.isAdmin" prop="employeeName" label="员工" width="100" />
        <el-table-column prop="attendDate" label="日期" width="120" />
        <el-table-column label="上班打卡" width="160">
          <template #default="{ row }">{{ formatTime(row.checkInTime) }}</template>
        </el-table-column>
        <el-table-column label="下班打卡" width="160">
          <template #default="{ row }">{{ formatTime(row.checkOutTime) }}</template>
        </el-table-column>
        <el-table-column prop="workHours" label="工时" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column v-if="userStore.isAdmin" label="操作" width="90">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.size"
        :current-page="query.current"
        @current-change="handlePage"
      />
    </el-card>

    <!-- 补录考勤弹窗 -->
    <el-dialog v-model="dialogVisible" title="补录考勤" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="员工">
          <el-select v-model="form.employeeId" filterable placeholder="选择员工" style="width: 100%">
            <el-option v-for="e in employees" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="form.attendDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="正常" value="NORMAL" />
            <el-option label="迟到" value="LATE" />
            <el-option label="早退" value="EARLY" />
            <el-option label="缺勤" value="ABSENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveManual">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { pageAttendance, listAllEmployees, saveManualAttendance, deleteAttendance } from '@/api'

const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const employees = ref([])
const dialogVisible = ref(false)

const query = reactive({ current: 1, size: 10, employeeId: null, month: '' })
const form = reactive({ employeeId: null, attendDate: '', status: 'NORMAL', remark: '' })

const formatTime = (t) => (t ? t.substring(11, 19) : '—')
const statusText = (s) => ({ NORMAL: '正常', LATE: '迟到', EARLY: '早退', ABSENT: '缺勤' }[s] || s)
const statusType = (s) => ({ NORMAL: 'success', LATE: 'warning', EARLY: 'warning', ABSENT: 'danger' }[s] || 'info')

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageAttendance(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handlePage = (p) => {
  query.current = p
  loadData()
}

const openManual = () => {
  Object.assign(form, { employeeId: null, attendDate: '', status: 'NORMAL', remark: '' })
  dialogVisible.value = true
}

const saveManual = async () => {
  if (!form.employeeId || !form.attendDate) {
    ElMessage.warning('请选择员工和日期')
    return
  }
  await saveManualAttendance(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除该考勤记录吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteAttendance(id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

onMounted(async () => {
  if (userStore.isAdmin) {
    const res = await listAllEmployees()
    employees.value = res.data
  }
  loadData()
})
</script>

<style scoped>
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>

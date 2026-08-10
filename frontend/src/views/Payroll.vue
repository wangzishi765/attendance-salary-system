<template>
  <div>
    <el-card shadow="never">
      <div class="page-toolbar">
        <el-date-picker v-model="query.month" type="month" value-format="YYYY-MM" placeholder="选择月份" @change="loadData" />
        <el-select v-if="userStore.isAdmin" v-model="query.employeeId" placeholder="选择员工" clearable filterable style="width: 180px" @change="loadData">
          <el-option v-for="e in employees" :key="e.id" :label="e.name" :value="e.id" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        <template v-if="userStore.isAdmin">
          <el-date-picker v-model="genMonth" type="month" value-format="YYYY-MM" placeholder="生成月份" />
          <el-button type="warning" :icon="Money" @click="handleGenerate">生成/重算工资单</el-button>
        </template>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="salaryMonth" label="月份" width="90" />
        <el-table-column v-if="userStore.isAdmin" prop="employeeName" label="员工" width="90" />
        <el-table-column v-if="userStore.isAdmin" prop="departmentName" label="部门" width="90" />
        <el-table-column prop="baseSalary" label="基本工资" width="100" />
        <el-table-column prop="attendanceBonus" label="全勤奖" width="90" />
        <el-table-column prop="overtimePay" label="加班费" width="90" />
        <el-table-column label="扣款合计" width="100">
          <template #default="{ row }">{{ (Number(row.lateDeduct) + Number(row.absentDeduct) + Number(row.leaveDeduct) + Number(row.otherDeduct)).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="grossSalary" label="应发" width="100" />
        <el-table-column prop="tax" label="个税" width="90" />
        <el-table-column prop="netSalary" label="实发" width="100">
          <template #default="{ row }"><b style="color:#f5222d">{{ row.netSalary }}</b></template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PAID' ? 'success' : 'info'">{{ row.status === 'PAID' ? '已发放' : '已生成' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="说明" min-width="200" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">明细</el-button>
            <el-button v-if="userStore.isAdmin && row.status !== 'PAID'" link type="success" @click="pay(row.id)">发放</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pager" layout="total, prev, pager, next" :total="total"
        :page-size="query.size" :current-page="query.current" @current-change="handlePage" />
    </el-card>

    <el-dialog v-model="detailVisible" title="工资条明细" width="420px">
      <el-descriptions :column="1" border v-if="current">
        <el-descriptions-item label="月份">{{ current.salaryMonth }}</el-descriptions-item>
        <el-descriptions-item label="员工">{{ current.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="基本工资">{{ current.baseSalary }}</el-descriptions-item>
        <el-descriptions-item label="全勤奖">+ {{ current.attendanceBonus }}</el-descriptions-item>
        <el-descriptions-item label="加班费">+ {{ current.overtimePay }}</el-descriptions-item>
        <el-descriptions-item label="迟到扣款">- {{ current.lateDeduct }}</el-descriptions-item>
        <el-descriptions-item label="缺勤扣款">- {{ current.absentDeduct }}</el-descriptions-item>
        <el-descriptions-item label="事假扣款">- {{ current.leaveDeduct }}</el-descriptions-item>
        <el-descriptions-item label="应发工资">{{ current.grossSalary }}</el-descriptions-item>
        <el-descriptions-item label="个人所得税">- {{ current.tax }}</el-descriptions-item>
        <el-descriptions-item label="实发工资"><b style="color:#f5222d">{{ current.netSalary }}</b></el-descriptions-item>
        <el-descriptions-item label="说明">{{ current.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Money } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { pagePayrolls, generatePayroll, markPayrollPaid, listAllEmployees } from '@/api'

const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const employees = ref([])
const detailVisible = ref(false)
const current = ref(null)
const genMonth = ref('')

const query = reactive({ current: 1, size: 10, employeeId: null, month: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await pagePayrolls(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handlePage = (p) => { query.current = p; loadData() }

const handleGenerate = () => {
  if (!genMonth.value) { ElMessage.warning('请选择要生成的月份'); return }
  ElMessageBox.confirm(`确定生成/重算 ${genMonth.value} 的工资单吗？将覆盖该月已有工资单。`, '提示', { type: 'warning' })
    .then(async () => {
      const res = await generatePayroll(genMonth.value)
      ElMessage.success(res.message)
      query.month = genMonth.value
      loadData()
    }).catch(() => {})
}

const pay = (id) => {
  ElMessageBox.confirm('确认标记为已发放？', '提示', { type: 'warning' }).then(async () => {
    await markPayrollPaid(id)
    ElMessage.success('已标记发放')
    loadData()
  }).catch(() => {})
}

const showDetail = (row) => {
  current.value = row
  detailVisible.value = true
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
.pager { margin-top: 16px; justify-content: flex-end; }
</style>

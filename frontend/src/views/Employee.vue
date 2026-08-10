<template>
  <div>
    <el-card shadow="never">
      <div class="page-toolbar">
        <el-input v-model="query.keyword" placeholder="姓名/工号/手机号" clearable style="width: 200px" @keyup.enter="loadData" />
        <el-select v-model="query.departmentId" placeholder="部门" clearable style="width: 150px" @change="loadData">
          <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        <el-button type="success" :icon="Plus" @click="openAdd">新增员工</el-button>
      </div>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="empNo" label="工号" width="90" />
        <el-table-column prop="name" label="姓名" width="90" />
        <el-table-column prop="gender" label="性别" width="60" />
        <el-table-column prop="departmentName" label="部门" width="100" />
        <el-table-column prop="position" label="职位" width="140" />
        <el-table-column prop="baseSalary" label="基本工资" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="hireDate" label="入职日期" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '在职' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pager" layout="total, prev, pager, next" :total="total"
        :page-size="query.size" :current-page="query.current" @current-change="handlePage" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑员工' : '新增员工'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="工号">
          <el-input v-model="form.empNo" :disabled="!!form.id" placeholder="如 E006" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio label="男" />
            <el-radio label="女" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="form.departmentId" style="width: 100%">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="职位">
          <el-input v-model="form.position" />
        </el-form-item>
        <el-form-item label="基本工资">
          <el-input-number v-model="form.baseSalary" :min="0" :step="500" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="入职日期">
          <el-date-picker v-model="form.hireDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="在职" value="在职" />
            <el-option label="离职" value="离职" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageEmployees, createEmployee, updateEmployee, deleteEmployee, listDepartments } from '@/api'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const departments = ref([])
const dialogVisible = ref(false)

const query = reactive({ current: 1, size: 10, keyword: '', departmentId: null })
const emptyForm = () => ({ id: null, empNo: '', name: '', gender: '男', departmentId: null, position: '', baseSalary: 8000, phone: '', email: '', hireDate: '', status: '在职' })
const form = reactive(emptyForm())

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageEmployees(query)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handlePage = (p) => { query.current = p; loadData() }

const openAdd = () => {
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const save = async () => {
  if (!form.empNo || !form.name) { ElMessage.warning('请填写工号和姓名'); return }
  if (form.id) {
    await updateEmployee(form)
    ElMessage.success('修改成功')
  } else {
    const res = await createEmployee(form)
    ElMessage.success(res.message || '新增成功')
  }
  dialogVisible.value = false
  loadData()
}

const handleDelete = (id) => {
  ElMessageBox.confirm('删除员工将同时删除其登录账号，确定吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteEmployee(id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

onMounted(async () => {
  const res = await listDepartments()
  departments.value = res.data
  loadData()
})
</script>

<style scoped>
.pager { margin-top: 16px; justify-content: flex-end; }
</style>

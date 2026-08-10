<template>
  <div>
    <el-card shadow="never">
      <div class="page-toolbar">
        <el-button type="success" :icon="Plus" @click="openAdd">新增部门</el-button>
      </div>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="部门名称" width="200" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑部门' : '新增部门'" width="440px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="部门名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
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
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listDepartments, saveDepartment, deleteDepartment } from '@/api'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '', remark: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await listDepartments()
    list.value = res.data
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  Object.assign(form, { id: null, name: '', remark: '' })
  dialogVisible.value = true
}
const openEdit = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}
const save = async () => {
  if (!form.name) { ElMessage.warning('请填写部门名称'); return }
  await saveDepartment(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}
const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除该部门吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteDepartment(id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

onMounted(loadData)
</script>

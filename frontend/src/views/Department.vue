<template>
  <div>
    <el-card shadow="never">
      <div class="page-toolbar">
        <el-button type="success" :icon="Plus" @click="openAdd">新增部门</el-button>
      </div>
      <el-table
        :data="list"
        border
        stripe
        v-loading="loading"
        row-key="id"
        :tree-props="{ children: 'children' }"
        default-expand-all
      >
        <el-table-column prop="name" label="部门名称" min-width="200" />
        <el-table-column prop="employeeCount" label="人数" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.employeeCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '启用' ? 'success' : 'info'" size="small">{{ row.status || '启用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openAddChild(row)">新增子部门</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑部门' : '新增部门'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="form.parentId"
            :data="treeData"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            check-strictly
            placeholder="顶级部门"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="启用">启用</el-radio>
            <el-radio label="禁用">禁用</el-radio>
          </el-radio-group>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listDepartments, getDepartmentTree, saveDepartment, deleteDepartment } from '@/api'

const list = ref([])
const treeData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ id: null, parentId: 0, name: '', sort: 0, status: '启用', remark: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDepartmentTree()
    list.value = res.data
    treeData.value = [{ id: 0, name: '顶级部门', children: list.value }]
  } finally {
    loading.value = false
  }
}

const buildTree = (data, parentId) => {
  const children = data.filter(d => (d.parentId || 0) === parentId)
  children.forEach(c => c.children = buildTree(data, c.id))
  return children
}

const openAdd = () => {
  Object.assign(form, { id: null, parentId: 0, name: '', sort: 0, status: '启用', remark: '' })
  dialogVisible.value = true
}

const openAddChild = (row) => {
  Object.assign(form, { id: null, parentId: row.id, name: '', sort: 0, status: '启用', remark: '' })
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, row)
  if (!form.parentId) form.parentId = 0
  if (!form.sort) form.sort = 0
  if (!form.status) form.status = '启用'
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

<style scoped>
</style>

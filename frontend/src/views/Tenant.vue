<template>
  <div>
    <el-card shadow="never">
      <div class="page-header">
        <h2>🏢 租户管理</h2>
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon> 新增租户
        </el-button>
      </div>

      <el-form :inline="true" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="租户名称/编码" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" stripe>
        <el-table-column prop="tenantCode" label="租户编码" width="140" />
        <el-table-column prop="tenantName" label="企业名称" min-width="180" />
        <el-table-column prop="contactPerson" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="140" />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑租户' : '新增租户'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="租户编码" prop="tenantCode">
          <el-input v-model="form.tenantCode" placeholder="请输入租户编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="企业名称" prop="tenantName">
          <el-input v-model="form.tenantName" placeholder="请输入企业名称" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="ACTIVE">正常</el-radio>
            <el-radio value="DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { pageTenants, createTenant, updateTenant, deleteTenant } from '@/api'

const tableData = ref([])
const searchForm = reactive({ keyword: '' })
const page = reactive({ current: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  tenantCode: '',
  tenantName: '',
  contactPerson: '',
  contactPhone: '',
  address: '',
  status: 'ACTIVE'
})

const rules = {
  tenantCode: [{ required: true, message: '请输入租户编码', trigger: 'blur' }],
  tenantName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }]
}

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

async function loadData() {
  const res = await pageTenants(page.current, page.size, searchForm.keyword)
  tableData.value = res.data.records
  page.total = res.data.total
}

function resetSearch() {
  searchForm.keyword = ''
  page.current = 1
  loadData()
}

function showAddDialog() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    tenantCode: '',
    tenantName: '',
    contactPerson: '',
    contactPhone: '',
    address: '',
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

function showEditDialog(row) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (isEdit.value) {
    await updateTenant(form)
    ElMessage.success('更新成功')
  } else {
    await createTenant(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除租户「${row.tenantName}」吗？`, '提示', {
    type: 'warning'
  })
  await deleteTenant(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-header h2 {
  margin: 0;
  font-size: 20px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.search-form {
  margin-bottom: 16px;
}
</style>

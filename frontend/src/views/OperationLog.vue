<template>
  <div>
    <el-card shadow="never">
      <div class="page-header">
        <h2>📋 操作日志</h2>
      </div>

      <el-form :inline="true" class="search-form">
        <el-form-item label="模块">
          <el-input v-model="searchForm.module" placeholder="操作模块" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="searchForm.username" placeholder="用户名/姓名" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAIL" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" stripe>
        <el-table-column prop="operationTime" label="操作时间" width="170">
          <template #default="{ row }">{{ formatTime(row.operationTime) }}</template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="operation" label="操作" width="80" />
        <el-table-column prop="realName" label="操作人" width="100" />
        <el-table-column prop="username" label="账号" width="100" />
        <el-table-column prop="ip" label="IP地址" width="130" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时" width="80">
          <template #default="{ row }">{{ row.costTime }}ms</template>
        </el-table-column>
        <el-table-column prop="description" label="方法" min-width="150" show-overflow-tooltip />
        <el-table-column label="参数" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="params-text" :title="row.params">{{ row.params || '-' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageOperationLogs } from '@/api'

const tableData = ref([])
const searchForm = reactive({ module: '', username: '', status: '' })
const page = reactive({ current: 1, size: 10, total: 0 })

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

async function loadData() {
  const res = await pageOperationLogs(page.current, page.size, searchForm.module, searchForm.username, searchForm.status)
  tableData.value = res.data.records
  page.total = res.data.total
}

function resetSearch() {
  searchForm.module = ''
  searchForm.username = ''
  searchForm.status = ''
  page.current = 1
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-header {
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
.params-text {
  font-family: monospace;
  font-size: 12px;
  color: #606266;
}
</style>

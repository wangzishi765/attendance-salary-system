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
          @change="onQueryChange"
        >
          <el-option v-for="e in employees" :key="e.id" :label="e.name" :value="e.id" />
        </el-select>
        <el-date-picker
          v-model="query.month"
          type="month"
          value-format="YYYY-MM"
          placeholder="选择月份"
          @change="onQueryChange"
        />
        <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
        <el-button v-if="userStore.isAdmin" type="success" :icon="Plus" @click="openManual">补录考勤</el-button>
        <div class="view-switch">
          <el-radio-group v-model="viewMode" size="default">
            <el-radio-button label="list">列表</el-radio-button>
            <el-radio-button label="calendar">月历</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-if="viewMode === 'list'">
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
      </div>

      <!-- 月历视图 -->
      <div v-if="viewMode === 'calendar'" v-loading="loading" class="calendar-wrap">
        <!-- 统计 -->
        <div class="cal-stat">
          <div class="stat-item normal"><span class="num">{{ calStat.normal || 0 }}</span><span class="label">正常</span></div>
          <div class="stat-item late"><span class="num">{{ calStat.late || 0 }}</span><span class="label">迟到</span></div>
          <div class="stat-item early"><span class="num">{{ calStat.early || 0 }}</span><span class="label">早退</span></div>
          <div class="stat-item absent"><span class="num">{{ calStat.absent || 0 }}</span><span class="label">缺勤</span></div>
          <div class="stat-item total"><span class="num">{{ calStat.total || 0 }}</span><span class="label">总打卡</span></div>
        </div>

        <!-- 星期表头 -->
        <div class="cal-weekdays">
          <div v-for="w in ['一', '二', '三', '四', '五', '六', '日']" :key="w" class="cal-weekday">{{ w }}</div>
        </div>

        <!-- 日期格子 -->
        <div class="cal-grid">
          <!-- 月初空白 -->
          <div v-for="n in firstWeekdayOffset" :key="'blank-' + n" class="cal-cell blank"></div>
          <div
            v-for="day in calDays"
            :key="day.date"
            class="cal-cell"
            :class="[
              'status-' + day.status.toLowerCase(),
              { weekend: day.isWeekend, today: isToday(day.date) }
            ]"
          >
            <div class="cal-day-num">{{ day.day }}</div>
            <div class="cal-status">{{ statusText(day.status) }}</div>
            <div v-if="day.checkInTime" class="cal-time">上 {{ day.checkInTime?.substring(0,5) }}</div>
            <div v-if="day.checkOutTime" class="cal-time">下 {{ day.checkOutTime?.substring(0,5) }}</div>
          </div>
        </div>

        <!-- 图例 -->
        <div class="cal-legend">
          <span class="legend-item"><i class="dot normal"></i>正常</span>
          <span class="legend-item"><i class="dot late"></i>迟到</span>
          <span class="legend-item"><i class="dot early"></i>早退</span>
          <span class="legend-item"><i class="dot absent"></i>缺勤</span>
          <span class="legend-item"><i class="dot weekend"></i>休息日</span>
        </div>
      </div>
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import {
  pageAttendance, listAllEmployees, saveManualAttendance, deleteAttendance,
  getAttendanceCalendar
} from '@/api'

const userStore = useUserStore()
const viewMode = ref('list')
const list = ref([])
const total = ref(0)
const loading = ref(false)
const employees = ref([])
const dialogVisible = ref(false)

const calDays = ref([])
const calStat = ref({})

const query = reactive({ current: 1, size: 10, employeeId: null, month: '' })
const form = reactive({ employeeId: null, attendDate: '', status: 'NORMAL', remark: '' })

const formatTime = (t) => (t ? t.substring(11, 19) : '—')
const statusText = (s) => ({ NORMAL: '正常', LATE: '迟到', EARLY: '早退', ABSENT: '缺勤', WEEKEND: '休息' }[s] || s)
const statusType = (s) => ({ NORMAL: 'success', LATE: 'warning', EARLY: 'warning', ABSENT: 'danger' }[s] || 'info')

const firstWeekdayOffset = computed(() => {
  if (!calDays.value.length) return 0
  // 第一天是周几（1=周一），前面空几格
  const first = calDays.value[0]
  return first.weekday - 1
})

const isToday = (dateStr) => {
  const today = new Date().toISOString().slice(0, 10)
  return dateStr === today
}

const onQueryChange = () => {
  query.current = 1
  loadData()
}

// 切换视图时重新加载
watch(viewMode, () => {
  query.current = 1
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    if (viewMode.value === 'list') {
      const res = await pageAttendance(query)
      list.value = res.data.records
      total.value = res.data.total
    } else {
      const res = await getAttendanceCalendar({ employeeId: query.employeeId, month: query.month })
      calDays.value = res.data.days
      calStat.value = res.data.stat || {}
    }
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
  // 默认当月
  const now = new Date()
  query.month = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  if (userStore.isAdmin) {
    const res = await listAllEmployees()
    employees.value = res.data
    // 默认选第一个员工
    if (res.data && res.data.length > 0) {
      query.employeeId = res.data[0].id
    }
  }
  loadData()
})
</script>

<style scoped>
.view-switch {
  margin-left: auto;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}

/* 月历 */
.calendar-wrap {
  margin-top: 8px;
}
.cal-stat {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.cal-stat .stat-item {
  flex: 1;
  min-width: 80px;
  text-align: center;
  padding: 12px 8px;
  border-radius: 8px;
  background: #f5f7fa;
}
.cal-stat .num {
  display: block;
  font-size: 24px;
  font-weight: bold;
}
.cal-stat .label {
  font-size: 13px;
  color: #909399;
}
.cal-stat .normal .num { color: #67c23a; }
.cal-stat .late .num { color: #e6a23c; }
.cal-stat .early .num { color: #f56c6c; }
.cal-stat .absent .num { color: #f56c6c; }
.cal-stat .total .num { color: #409eff; }

.cal-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
  margin-bottom: 6px;
}
.cal-weekday {
  text-align: center;
  font-weight: bold;
  color: #606266;
  padding: 8px 0;
  background: #f5f7fa;
  border-radius: 6px;
}

.cal-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}
.cal-cell {
  min-height: 90px;
  padding: 8px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  background: #fff;
  position: relative;
  transition: all 0.2s;
}
.cal-cell:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.cal-cell.blank {
  border: none;
  background: transparent;
  min-height: 0;
}
.cal-cell.weekend {
  background: #fafafa;
}
.cal-cell.today {
  border: 2px solid #409eff;
}
.cal-day-num {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}
.cal-status {
  font-size: 12px;
  margin-top: 4px;
  font-weight: 500;
}
.cal-time {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}

/* 状态颜色 */
.status-normal { background: #f0f9eb; border-color: #e1f3d8; }
.status-normal .cal-status { color: #67c23a; }
.status-late { background: #fdf6ec; border-color: #faecd8; }
.status-late .cal-status { color: #e6a23c; }
.status-early { background: #fef0f0; border-color: #fde2e2; }
.status-early .cal-status { color: #f56c6c; }
.status-absent { background: #fef0f0; border-color: #fde2e2; }
.status-absent .cal-status { color: #f56c6c; }
.status-weekend { background: #f5f7fa; border-color: #ebeef5; }
.status-weekend .cal-status { color: #909399; }

.cal-legend {
  margin-top: 16px;
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}
.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}
.dot.normal { background: #67c23a; }
.dot.late { background: #e6a23c; }
.dot.early { background: #f56c6c; }
.dot.absent { background: #f56c6c; }
.dot.weekend { background: #dcdfe6; }
</style>

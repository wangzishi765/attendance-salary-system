<template>
  <div>
    <el-card shadow="never" class="report-header">
      <div class="header-content">
        <h2>📊 统计报表中心</h2>
        <el-radio-group v-model="activeTab" @change="loadReport">
          <el-radio-button label="employee">👥 人员统计</el-radio-button>
          <el-radio-button label="attendance">📅 考勤统计</el-radio-button>
          <el-radio-button label="salary">💰 薪资成本</el-radio-button>
        </el-radio-group>
        <el-date-picker
          v-if="activeTab !== 'employee'"
          v-model="selectedMonth"
          type="month"
          placeholder="选择月份"
          value-format="YYYY-MM"
          @change="loadReport"
          style="margin-left: 16px"
        />
      </div>
    </el-card>

    <!-- 人员统计报表 -->
    <div v-if="activeTab === 'employee'">
      <el-row :gutter="16" class="stat-row">
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2)">
              <el-icon :size="26"><User /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ employeeData.totalEmployees || 0 }}</div>
              <div class="stat-label">在职员工</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #11998e, #38ef7d)">
              <el-icon :size="26"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ employeeData.departmentDistribution?.length || 0 }}</div>
              <div class="stat-label">部门数量</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c)">
              <el-icon :size="26"><Male /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ getGenderCount('男') }}</div>
              <div class="stat-label">男员工</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe)">
              <el-icon :size="26"><Female /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ getGenderCount('女') }}</div>
              <div class="stat-label">女员工</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :xs="24" :sm="12">
          <el-card shadow="never">
            <template #header>部门人数分布</template>
            <div ref="deptPieRef" class="chart"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-card shadow="never">
            <template #header>性别比例</template>
            <div ref="genderPieRef" class="chart"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" style="margin-top: 16px">
        <el-col :xs="24" :sm="12">
          <el-card shadow="never">
            <template #header>薪资区间分布</template>
            <div ref="salaryBarRef" class="chart"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-card shadow="never">
            <template #header>员工状态统计</template>
            <div ref="statusPieRef" class="chart"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 考勤统计报表 -->
    <div v-if="activeTab === 'attendance'">
      <el-row :gutter="16" class="stat-row">
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2)">
              <el-icon :size="26"><Clock /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ attendanceData.totalCheckIns || 0 }}</div>
              <div class="stat-label">总打卡次数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #11998e, #38ef7d)">
              <el-icon :size="26"><CircleCheck /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ attendanceData.statusStat?.normal || 0 }}</div>
              <div class="stat-label">正常出勤</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c)">
              <el-icon :size="26"><Warning /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ attendanceData.statusStat?.late || 0 }}</div>
              <div class="stat-label">迟到次数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe)">
              <el-icon :size="26"><TrendCharts /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ attendanceData.attendanceRate || 0 }}%</div>
              <div class="stat-label">出勤率</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :xs="24" :sm="12">
          <el-card shadow="never">
            <template #header>考勤状态分布</template>
            <div ref="attStatusPieRef" class="chart"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-card shadow="never">
            <template #header>各部门考勤对比</template>
            <div ref="deptAttBarRef" class="chart"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" style="margin-top: 16px">
        <template #header>各部门考勤明细</template>
        <el-table :data="attendanceData.departmentAttendance || []" stripe>
          <el-table-column prop="department" label="部门" />
          <el-table-column prop="total" label="部门人数" />
          <el-table-column prop="normal" label="正常出勤" />
          <el-table-column prop="late" label="迟到" />
          <el-table-column prop="absent" label="缺勤" />
        </el-table>
      </el-card>
    </div>

    <!-- 薪资成本报表 -->
    <div v-if="activeTab === 'salary'">
      <el-row :gutter="16" class="stat-row">
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2)">
              <el-icon :size="26"><Money /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">¥{{ formatMoney(salaryData.costStat?.totalGross) }}</div>
              <div class="stat-label">应发工资总额</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #11998e, #38ef7d)">
              <el-icon :size="26"><Wallet /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">¥{{ formatMoney(salaryData.costStat?.totalNet) }}</div>
              <div class="stat-label">实发工资总额</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c)">
              <el-icon :size="26"><User /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">{{ salaryData.costStat?.employeeCount || 0 }}</div>
              <div class="stat-label">发放人数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe)">
              <el-icon :size="26"><TrendCharts /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-value">¥{{ formatMoney(salaryData.costStat?.avgNet) }}</div>
              <div class="stat-label">人均实发工资</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :xs="24" :sm="12">
          <el-card shadow="never">
            <template #header>各部门薪资对比</template>
            <div ref="deptSalaryBarRef" class="chart"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-card shadow="never">
            <template #header>薪资构成分析</template>
            <div ref="salaryCompPieRef" class="chart"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" style="margin-top: 16px">
        <template #header>各部门薪资明细</template>
        <el-table :data="salaryData.departmentSalary || []" stripe>
          <el-table-column prop="department" label="部门" />
          <el-table-column prop="employeeCount" label="人数" />
          <el-table-column label="应发工资总额">
            <template #default="{ row }">¥{{ formatMoney(row.grossSalary) }}</template>
          </el-table-column>
          <el-table-column label="实发工资总额">
            <template #default="{ row }">¥{{ formatMoney(row.netSalary) }}</template>
          </el-table-column>
          <el-table-column label="人均实发">
            <template #default="{ row }">¥{{ formatMoney(row.avgNet) }}</template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { getEmployeeReport, getAttendanceReport, getSalaryReport } from '@/api'
import {
  User, OfficeBuilding, Male, Female, Clock, CircleCheck,
  Warning, TrendCharts, Money, Wallet
} from '@element-plus/icons-vue'

const activeTab = ref('employee')
const selectedMonth = ref(getCurrentMonth())
const employeeData = reactive({})
const attendanceData = reactive({})
const salaryData = reactive({})

const deptPieRef = ref(null)
const genderPieRef = ref(null)
const salaryBarRef = ref(null)
const statusPieRef = ref(null)
const attStatusPieRef = ref(null)
const deptAttBarRef = ref(null)
const deptSalaryBarRef = ref(null)
const salaryCompPieRef = ref(null)

let charts = {}

function getCurrentMonth() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

function formatMoney(val) {
  if (!val) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getGenderCount(gender) {
  const item = employeeData.genderDistribution?.find(i => i.name === gender)
  return item ? item.value : 0
}

const purpleColors = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe', '#00f2fe', '#11998e', '#38ef7d']

function initPieChart(ref, data, title) {
  if (!ref.value) return
  if (charts[title]) charts[title].dispose()
  charts[title] = echarts.init(ref.value)
  charts[title].setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    color: purpleColors,
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
      data: data
    }]
  })
}

function initBarChart(ref, xData, yData, color) {
  if (!ref.value) return
  const key = ref.value.__v_oid || 'bar'
  if (charts[key]) charts[key].dispose()
  charts[key] = echarts.init(ref.value)
  charts[key].setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: xData, axisLabel: { interval: 0 } },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: yData,
      barWidth: '50%',
      itemStyle: {
        borderRadius: [8, 8, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: color || '#667eea' },
          { offset: 1, color: color || '#764ba2' }
        ])
      }
    }]
  })
}

function initGroupedBarChart(ref, xData, series) {
  if (!ref.value) return
  const key = 'grouped-' + (ref.value.__v_oid || 'bar')
  if (charts[key]) charts[key].dispose()
  charts[key] = echarts.init(ref.value)
  charts[key].setOption({
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: { type: 'category', data: xData },
    yAxis: { type: 'value' },
    color: purpleColors,
    series: series
  })
}

async function loadReport() {
  if (activeTab.value === 'employee') {
    const res = await getEmployeeReport()
    Object.assign(employeeData, res.data)
    await nextTick()
    initPieChart(deptPieRef, employeeData.departmentDistribution || [], 'deptPie')
    initPieChart(genderPieRef, employeeData.genderDistribution || [], 'genderPie')
    initPieChart(statusPieRef, [
      { name: '在职', value: employeeData.statusStat?.active || 0 },
      { name: '离职', value: employeeData.statusStat?.leave || 0 }
    ], 'statusPie')
    const salaryDist = employeeData.salaryDistribution || []
    initBarChart(salaryBarRef, salaryDist.map(i => i.name), salaryDist.map(i => i.value), '#11998e')
  } else if (activeTab.value === 'attendance') {
    const res = await getAttendanceReport(selectedMonth.value)
    Object.assign(attendanceData, res.data)
    await nextTick()
    const stat = attendanceData.statusStat || {}
    initPieChart(attStatusPieRef, [
      { name: '正常', value: stat.normal || 0 },
      { name: '迟到', value: stat.late || 0 },
      { name: '早退', value: stat.early || 0 },
      { name: '缺勤', value: stat.absent || 0 }
    ], 'attStatusPie')
    const deptAtt = attendanceData.departmentAttendance || []
    initGroupedBarChart(deptAttBarRef, deptAtt.map(i => i.department), [
      { name: '正常', type: 'bar', data: deptAtt.map(i => i.normal) },
      { name: '迟到', type: 'bar', data: deptAtt.map(i => i.late) },
      { name: '缺勤', type: 'bar', data: deptAtt.map(i => i.absent) }
    ])
  } else if (activeTab.value === 'salary') {
    const res = await getSalaryReport(selectedMonth.value)
    Object.assign(salaryData, res.data)
    await nextTick()
    const deptSal = salaryData.departmentSalary || []
    initGroupedBarChart(deptSalaryBarRef, deptSal.map(i => i.department), [
      { name: '应发工资', type: 'bar', data: deptSal.map(i => Number(i.grossSalary || 0)) },
      { name: '实发工资', type: 'bar', data: deptSal.map(i => Number(i.netSalary || 0)) }
    ])
    initPieChart(salaryCompPieRef, (salaryData.salaryComposition || []).map(i => ({
      name: i.name,
      value: Number(i.value || 0)
    })), 'salaryCompPie')
  }
}

onMounted(() => {
  loadReport()
  window.addEventListener('resize', () => {
    Object.values(charts).forEach(c => c && c.resize())
  })
})
</script>

<style scoped>
.report-header {
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
.stat-row {
  margin-bottom: 16px;
}
.stat-card {
  border-radius: 12px;
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  padding: 20px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 16px;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
.chart {
  height: 320px;
  width: 100%;
}
</style>

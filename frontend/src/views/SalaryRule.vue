<template>
  <div>
    <el-card shadow="never" style="max-width: 640px">
      <template #header>
        <span>薪资规则配置</span>
      </template>
      <el-form :model="form" label-width="180px">
        <el-form-item label="规则名称">
          <el-input v-model="form.name" style="width: 260px" />
        </el-form-item>
        <el-divider content-position="left">考勤与加班</el-divider>
        <el-form-item label="每次迟到扣款（元）">
          <el-input-number v-model="form.lateDeduct" :min="0" :step="10" />
        </el-form-item>
        <el-form-item label="每天缺勤扣款（元）">
          <el-input-number v-model="form.absentDeduct" :min="0" :step="50" />
        </el-form-item>
        <el-form-item label="每天事假扣款（元）">
          <el-input-number v-model="form.leaveDeduct" :min="0" :step="50" />
        </el-form-item>
        <el-form-item label="每小时加班费（元）">
          <el-input-number v-model="form.overtimeRate" :min="0" :step="5" />
        </el-form-item>
        <el-form-item label="全勤奖（元）">
          <el-input-number v-model="form.fullAttendanceBonus" :min="0" :step="50" />
        </el-form-item>
        <el-divider content-position="left">社保与公积金</el-divider>
        <el-form-item label="社保个人缴纳比例">
          <el-input-number v-model="form.socialSecurityRate" :min="0" :max="1" :step="0.005" :precision="4" />
          <span style="margin-left: 8px; color: #999">（如 0.105 表示 10.5%）</span>
        </el-form-item>
        <el-form-item label="公积金个人缴纳比例">
          <el-input-number v-model="form.housingFundRate" :min="0" :max="1" :step="0.01" :precision="4" />
          <span style="margin-left: 8px; color: #999">（如 0.07 表示 7%）</span>
        </el-form-item>
        <el-divider content-position="left">个税计算</el-divider>
        <el-form-item label="个税起征点（元）">
          <el-input-number v-model="form.taxThreshold" :min="0" :step="500" />
        </el-form-item>
        <el-form-item label="专项附加扣除（元/月）">
          <el-input-number v-model="form.specialDeduction" :min="0" :step="100" />
          <span style="margin-left: 8px; color: #999">（子女教育/赡养老人等）</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保存规则</el-button>
        </el-form-item>
      </el-form>
      <el-alert type="info" :closable="false" show-icon
        title="计算规则：应发 = 基本工资 + 全勤奖 + 加班费 − 迟到/缺勤/事假扣款；社保 = 基本工资 × 社保比例；公积金 = 基本工资 × 公积金比例；个税 = (应发 − 社保 − 公积金 − 专项附加扣除 − 起征点) × 3%；实发 = 应发 − 社保 − 公积金 − 个税。" />
    </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSalaryRule, updateSalaryRule } from '@/api'

const form = reactive({
  name: '默认薪资规则', lateDeduct: 50, absentDeduct: 200,
  leaveDeduct: 100, overtimeRate: 30, fullAttendanceBonus: 300,
  socialSecurityRate: 0.105, housingFundRate: 0.07,
  taxThreshold: 5000, specialDeduction: 0
})

const load = async () => {
  const res = await getSalaryRule()
  Object.assign(form, res.data)
}

const save = async () => {
  await updateSalaryRule(form)
  ElMessage.success('保存成功')
  load()
}

onMounted(load)
</script>

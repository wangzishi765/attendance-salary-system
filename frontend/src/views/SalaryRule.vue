<template>
  <div>
    <el-card shadow="never" style="max-width: 640px">
      <template #header>
        <span>薪资规则配置</span>
      </template>
      <el-form :model="form" label-width="160px">
        <el-form-item label="规则名称">
          <el-input v-model="form.name" style="width: 260px" />
        </el-form-item>
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
        <el-form-item>
          <el-button type="primary" @click="save">保存规则</el-button>
        </el-form-item>
      </el-form>
      <el-alert type="info" :closable="false" show-icon
        title="说明：应发 = 基本工资 + 全勤奖 + 加班费 − 迟到/缺勤/事假扣款；个税按超过5000部分的3%简化计算；实发 = 应发 − 个税。全勤奖需当月无迟到/早退/缺勤/请假。" />
    </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSalaryRule, updateSalaryRule } from '@/api'

const form = reactive({
  name: '默认薪资规则', lateDeduct: 50, absentDeduct: 200,
  leaveDeduct: 100, overtimeRate: 30, fullAttendanceBonus: 300
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

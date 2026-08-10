<template>
  <div class="login-wrapper">
    <div class="login-box">
      <div class="login-title">
        <el-icon :size="32"><Coordinate /></el-icon>
        <h2>考勤薪资管理系统</h2>
        <p>中小企业员工考勤与薪资核算</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent>
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            size="large"
            placeholder="请输入用户名"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            size="large"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-tip">
        <p class="tip-title">演示账号：</p>
        <div class="tip-row">
          <span class="tip-role">管理员</span>
          <span class="tip-account">admin / admin123</span>
        </div>
        <div class="tip-row">
          <span class="tip-role">人事</span>
          <span class="tip-account">hr / hr123456</span>
        </div>
        <div class="tip-row">
          <span class="tip-role">员工</span>
          <span class="tip-account">E001 / 123456</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    // 错误已在拦截器提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}
.login-wrapper::before {
  content: '';
  position: absolute;
  width: 600px;
  height: 600px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
  top: -200px;
  left: -200px;
}
.login-wrapper::after {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 50%;
  bottom: -100px;
  right: -100px;
}
.login-box {
  width: 420px;
  padding: 48px 40px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
}
.login-title {
  text-align: center;
  margin-bottom: 36px;
}
.login-title .el-icon {
  font-size: 48px;
  color: #667eea;
  margin-bottom: 12px;
}
.login-title h2 {
  margin: 10px 0 6px;
  color: #1a1a2e;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 1px;
}
.login-title p {
  color: #888;
  font-size: 14px;
  margin: 0;
}
.login-box :deep(.el-form-item) {
  margin-bottom: 24px;
}
.login-box :deep(.el-input__wrapper) {
  border-radius: 8px;
  padding: 4px 16px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: all 0.3s;
}
.login-box :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #667eea inset;
}
.login-box :deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #667eea inset, 0 0 0 3px rgba(102, 126, 234, 0.1);
}
.login-box :deep(.el-button--primary) {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  transition: all 0.3s;
}
.login-box :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.5);
}
.login-tip {
  margin-top: 24px;
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  border-radius: 10px;
  font-size: 13px;
  color: #666;
  line-height: 1.8;
}
.tip-title {
  margin: 0 0 10px 0;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}
.tip-row {
  display: flex;
  justify-content: space-between;
  padding: 2px 0;
}
.tip-role {
  color: #667eea;
  font-weight: 500;
}
.tip-account {
  color: #333;
  font-family: monospace;
}
</style>

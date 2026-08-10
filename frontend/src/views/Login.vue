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
        <p>演示账号：</p>
        <p>管理员　admin / admin123</p>
        <p>员工　　E001 / 123456</p>
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
  background: linear-gradient(135deg, #1890ff 0%, #0050b3 100%);
}
.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}
.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #1890ff;
}
.login-title h2 {
  margin: 10px 0 4px;
  color: #001529;
}
.login-title p {
  color: #999;
  font-size: 13px;
}
.login-tip {
  margin-top: 10px;
  padding: 12px;
  background: #f6f8fa;
  border-radius: 6px;
  font-size: 12px;
  color: #888;
  line-height: 1.8;
}
.login-tip p {
  margin: 0;
}
</style>

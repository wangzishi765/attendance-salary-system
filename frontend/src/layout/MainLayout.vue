<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon><Coordinate /></el-icon>
        <span>考勤薪资系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#c0c4cc"
        active-text-color="#ffffff"
      >
        <el-menu-item
          v-for="item in menus"
          :key="item.path"
          :index="'/' + item.path"
        >
          <el-icon><component :is="item.meta.icon" /></el-icon>
          <span>{{ item.meta.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-tag :type="userStore.isAdmin ? 'danger' : 'success'" effect="dark">
            {{ userStore.isAdmin ? '管理员' : '员工' }}
          </el-tag>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ userStore.realName || userStore.username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const menus = computed(() => {
  const layout = router.options.routes.find((r) => r.path === '/')
  return layout.children.filter((c) => {
    if (!c.meta || !c.meta.title) return false
    if (c.meta.roles) return c.meta.roles.includes(userStore.role)
    return true
  })
})

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
      .then(() => {
        userStore.logout()
        router.push('/login')
      })
      .catch(() => {})
  }
}
</script>

<style scoped>
.layout {
  height: 100%;
}
.aside {
  background-color: #001529;
  overflow-x: hidden;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background: linear-gradient(135deg, #1890ff, #0050b3);
}
.aside .el-menu {
  border-right: none;
}
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}
.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #333;
}
.main {
  background: #f0f2f5;
  padding: 16px;
}
</style>

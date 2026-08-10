import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: () => import('@/views/Attendance.vue'),
        meta: { title: '考勤打卡', icon: 'Clock' }
      },
      {
        path: 'leave',
        name: 'Leave',
        component: () => import('@/views/Leave.vue'),
        meta: { title: '请假管理', icon: 'Calendar' }
      },
      {
        path: 'overtime',
        name: 'Overtime',
        component: () => import('@/views/Overtime.vue'),
        meta: { title: '加班管理', icon: 'Timer' }
      },
      {
        path: 'payroll',
        name: 'Payroll',
        component: () => import('@/views/Payroll.vue'),
        meta: { title: '工资单', icon: 'Money' }
      },
      {
        path: 'employee',
        name: 'Employee',
        component: () => import('@/views/Employee.vue'),
        meta: { title: '员工管理', icon: 'User', roles: ['ADMIN'] }
      },
      {
        path: 'department',
        name: 'Department',
        component: () => import('@/views/Department.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding', roles: ['ADMIN'] }
      },
      {
        path: 'salary-rule',
        name: 'SalaryRule',
        component: () => import('@/views/SalaryRule.vue'),
        meta: { title: '薪资规则', icon: 'Setting', roles: ['ADMIN'] }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('hrms_token')
  const role = localStorage.getItem('hrms_role')
  if (to.meta.public) {
    next()
    return
  }
  if (!token) {
    next('/login')
    return
  }
  if (to.meta.roles && !to.meta.roles.includes(role)) {
    next('/dashboard')
    return
  }
  next()
})

export default router

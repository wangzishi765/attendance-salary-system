import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('hrms_token') || '',
    userId: null,
    username: '',
    realName: localStorage.getItem('hrms_realName') || '',
    role: localStorage.getItem('hrms_role') || '',
    employeeId: localStorage.getItem('hrms_employeeId')
      ? Number(localStorage.getItem('hrms_employeeId'))
      : null,
    tenantId: localStorage.getItem('hrms_tenantId')
      ? Number(localStorage.getItem('hrms_tenantId'))
      : null,
    tenantName: localStorage.getItem('hrms_tenantName') || ''
  }),
  getters: {
    isAdmin: (state) => state.role === 'ADMIN',
    isAdminOrHr: (state) => state.role === 'ADMIN' || state.role === 'HR',
    isLogin: (state) => !!state.token
  },
  actions: {
    async login(form) {
      const res = await request.post('/api/auth/login', form)
      const data = res.data
      this.token = data.token
      this.userId = data.userId
      this.username = data.username
      this.realName = data.realName
      this.role = data.role
      this.employeeId = data.employeeId
      this.tenantId = data.tenantId
      this.tenantName = data.tenantName
      localStorage.setItem('hrms_token', data.token)
      localStorage.setItem('hrms_role', data.role)
      localStorage.setItem('hrms_realName', data.realName || '')
      localStorage.setItem('hrms_employeeId', data.employeeId ?? '')
      localStorage.setItem('hrms_tenantId', data.tenantId ?? '')
      localStorage.setItem('hrms_tenantName', data.tenantName || '')
      return data
    },
    logout() {
      this.token = ''
      this.role = ''
      this.realName = ''
      this.employeeId = null
      this.tenantId = null
      this.tenantName = ''
      localStorage.removeItem('hrms_token')
      localStorage.removeItem('hrms_role')
      localStorage.removeItem('hrms_realName')
      localStorage.removeItem('hrms_employeeId')
      localStorage.removeItem('hrms_tenantId')
      localStorage.removeItem('hrms_tenantName')
    }
  }
})

import request from '@/utils/request'

// ==================== 认证 ====================
export const login = (data) => request.post('/api/auth/login', data)
export const getMe = () => request.get('/api/auth/me')
export const changePassword = (data) => request.post('/api/auth/change-password', data)

// ==================== 仪表盘 ====================
export const getDashboardStat = () => request.get('/api/dashboard/stat')

// ==================== 部门 ====================
export const listDepartments = () => request.get('/api/departments')
export const getDepartmentTree = () => request.get('/api/departments/tree')
export const saveDepartment = (data) => request.post('/api/departments', data)
export const deleteDepartment = (id) => request.delete(`/api/departments/${id}`)

// ==================== 员工 ====================
export const pageEmployees = (params) => request.get('/api/employees', { params })
export const listAllEmployees = () => request.get('/api/employees/all')
export const getEmployee = (id) => request.get(`/api/employees/${id}`)
export const createEmployee = (data) => request.post('/api/employees', data)
export const updateEmployee = (data) => request.put('/api/employees', data)
export const deleteEmployee = (id) => request.delete(`/api/employees/${id}`)
export const importEmployees = (file) => {
  const form = new FormData()
  form.append('file', file)
  return request.post('/api/employees/import', form, { headers: { 'Content-Type': 'multipart/form-data' } })
}

// ==================== 考勤 ====================
export const checkIn = () => request.post('/api/attendance/check-in')
export const checkOut = () => request.post('/api/attendance/check-out')
export const getTodayAttendance = () => request.get('/api/attendance/today')
export const pageAttendance = (params) => request.get('/api/attendance', { params })
export const getAttendanceStat = (params) => request.get('/api/attendance/stat', { params })
export const getAttendanceCalendar = (params) => request.get('/api/attendance/calendar', { params })
export const saveManualAttendance = (data) => request.post('/api/attendance/manual', data)
export const deleteAttendance = (id) => request.delete(`/api/attendance/${id}`)
export const importAttendance = (file) => {
  const form = new FormData()
  form.append('file', file)
  return request.post('/api/attendance/import', form, { headers: { 'Content-Type': 'multipart/form-data' } })
}

// ==================== 请假 ====================
export const pageLeaves = (params) => request.get('/api/leaves', { params })
export const applyLeave = (data) => request.post('/api/leaves', data)
export const auditLeave = (id, status) => request.put(`/api/leaves/${id}/audit`, null, { params: { status } })
export const deleteLeave = (id) => request.delete(`/api/leaves/${id}`)

// ==================== 加班 ====================
export const pageOvertimes = (params) => request.get('/api/overtimes', { params })
export const applyOvertime = (data) => request.post('/api/overtimes', data)
export const auditOvertime = (id, status) => request.put(`/api/overtimes/${id}/audit`, null, { params: { status } })
export const deleteOvertime = (id) => request.delete(`/api/overtimes/${id}`)

// ==================== 薪资规则 ====================
export const getSalaryRule = () => request.get('/api/salary-rule')
export const updateSalaryRule = (data) => request.put('/api/salary-rule', data)

// ==================== 工资单 ====================
export const generatePayroll = (month) => request.post('/api/payrolls/generate', null, { params: { month } })
export const pagePayrolls = (params) => request.get('/api/payrolls', { params })
export const markPayrollPaid = (id) => request.put(`/api/payrolls/${id}/pay`)
export const deletePayroll = (id) => request.delete(`/api/payrolls/${id}`)
export const exportPayroll = (params) => request.get('/api/payrolls/export', { params, responseType: 'blob' })

// ==================== 系统监控 ====================
export const getSystemMonitor = () => request.get('/api/system/monitor')

// ==================== 统计报表 ====================
export const getEmployeeReport = () => request.get('/api/reports/employee')
export const getAttendanceReport = (month) => request.get('/api/reports/attendance', { params: { month } })
export const getSalaryReport = (month) => request.get('/api/reports/salary', { params: { month } })

// ==================== 工作流 ====================
export const getWorkflowProcesses = () => request.get('/api/workflow/processes')
export const startWorkflow = (data) => request.post('/api/workflow/start', data)
export const approveWorkflowTask = (id, status, comment) => request.put(`/api/workflow/tasks/${id}/approve`, { status, comment })
export const getWorkflowPending = (current, size) => request.get('/api/workflow/tasks/pending', { params: { current, size } })
export const getWorkflowApproved = (current, size) => request.get('/api/workflow/tasks/approved', { params: { current, size } })
export const getWorkflowStarted = (current, size) => request.get('/api/workflow/instances/my', { params: { current, size } })
export const getWorkflowInstance = (id) => request.get(`/api/workflow/instances/${id}`)
export const getWorkflowHistory = (id) => request.get(`/api/workflow/instances/${id}/history`)

// ==================== 租户管理 ====================
export const pageTenants = (current, size, keyword) => request.get('/api/tenants', { params: { current, size, keyword } })
export const listAllTenants = () => request.get('/api/tenants/all')
export const getTenant = (id) => request.get(`/api/tenants/${id}`)
export const createTenant = (data) => request.post('/api/tenants', data)
export const updateTenant = (data) => request.put('/api/tenants', data)
export const deleteTenant = (id) => request.delete(`/api/tenants/${id}`)

// ==================== 操作日志 ====================
export const pageOperationLogs = (current, size, module, username, status) => request.get('/api/operation-logs', { params: { current, size, module, username, status } })

// ==================== 消息通知 ====================
export const getNotifications = (params) => request.get('/api/notifications', { params })
export const getUnreadCount = () => request.get('/api/notifications/unread-count')
export const getNotificationOverview = () => request.get('/api/notifications/overview')
export const markNotificationRead = (id) => request.put(`/api/notifications/${id}/read`)
export const markAllNotificationsRead = () => request.put('/api/notifications/read-all')

// ==================== 数据备份 ====================
export const createBackupApi = () => request.post('/api/backup/create')
export const listBackups = () => request.get('/api/backup/list')
export const deleteBackupApi = (name) => request.delete(`/api/backup/${name}`)
export const downloadBackup = (name) => request.get(`/api/backup/download/${name}`, { responseType: 'blob' })

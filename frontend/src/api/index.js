import request from '@/utils/request'

// ==================== 认证 ====================
export const login = (data) => request.post('/api/auth/login', data)
export const getMe = () => request.get('/api/auth/me')

// ==================== 仪表盘 ====================
export const getDashboardStat = () => request.get('/api/dashboard/stat')

// ==================== 部门 ====================
export const listDepartments = () => request.get('/api/departments')
export const saveDepartment = (data) => request.post('/api/departments', data)
export const deleteDepartment = (id) => request.delete(`/api/departments/${id}`)

// ==================== 员工 ====================
export const pageEmployees = (params) => request.get('/api/employees', { params })
export const listAllEmployees = () => request.get('/api/employees/all')
export const getEmployee = (id) => request.get(`/api/employees/${id}`)
export const createEmployee = (data) => request.post('/api/employees', data)
export const updateEmployee = (data) => request.put('/api/employees', data)
export const deleteEmployee = (id) => request.delete(`/api/employees/${id}`)

// ==================== 考勤 ====================
export const checkIn = () => request.post('/api/attendance/check-in')
export const checkOut = () => request.post('/api/attendance/check-out')
export const getTodayAttendance = () => request.get('/api/attendance/today')
export const pageAttendance = (params) => request.get('/api/attendance', { params })
export const getAttendanceStat = (params) => request.get('/api/attendance/stat', { params })
export const saveManualAttendance = (data) => request.post('/api/attendance/manual', data)
export const deleteAttendance = (id) => request.delete(`/api/attendance/${id}`)

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

# -*- coding: utf-8 -*-
"""
考勤薪资管理系统 - API 自动化测试程序
测试所有模块接口和三角色权限控制
"""

import requests
import json
import time
from datetime import datetime
from colorama import init, Fore, Style

from config import BASE_URL, ACCOUNTS, TEST_MONTH, TIMEOUT

# 初始化彩色输出
init(autoreset=True)

# 全局统计
stats = {
    "total": 0,
    "passed": 0,
    "failed": 0,
    "skipped": 0,
    "details": []
}

# 存储 token
tokens = {}


def print_header(title):
    """打印测试模块标题"""
    print(f"\n{'='*60}")
    print(f"  {Fore.CYAN}{Style.BRIGHT}{title}{Style.RESET_ALL}")
    print(f"{'='*60}")


def print_result(test_name, passed, message="", status_code=None, data=None):
    """打印单个测试结果"""
    stats["total"] += 1
    if passed:
        stats["passed"] += 1
        icon = f"{Fore.GREEN}✅ PASS{Style.RESET_ALL}"
    else:
        stats["failed"] += 1
        icon = f"{Fore.RED}❌ FAIL{Style.RESET_ALL}"

    status_info = f" [HTTP {status_code}]" if status_code else ""
    print(f"  {icon} {test_name}{status_info}")
    if message:
        print(f"         {Fore.WHITE}{message}{Style.RESET_ALL}")

    stats["details"].append({
        "name": test_name,
        "passed": passed,
        "message": message,
        "status_code": status_code
    })


def login(username, password):
    """登录获取token"""
    url = f"{BASE_URL}/api/auth/login"
    try:
        resp = requests.post(url, json={"username": username, "password": password}, timeout=TIMEOUT)
        if resp.status_code == 200:
            data = resp.json()
            if data.get("code") == 200:
                return data["data"]["token"]
    except Exception as e:
        print(f"  {Fore.RED}登录失败: {e}{Style.RESET_ALL}")
    return None


def get_headers(role="admin"):
    """获取请求头"""
    token = tokens.get(role)
    if not token:
        return {}
    return {"Authorization": f"Bearer {token}"}


def api_get(path, role="admin", params=None):
    """GET请求"""
    url = f"{BASE_URL}{path}"
    try:
        resp = requests.get(url, headers=get_headers(role), params=params, timeout=TIMEOUT)
        return resp
    except Exception as e:
        return None


def api_post(path, role="admin", json_data=None):
    """POST请求"""
    url = f"{BASE_URL}{path}"
    try:
        resp = requests.post(url, headers=get_headers(role), json=json_data, timeout=TIMEOUT)
        return resp
    except Exception as e:
        return None


def api_put(path, role="admin", json_data=None):
    """PUT请求"""
    url = f"{BASE_URL}{path}"
    try:
        resp = requests.put(url, headers=get_headers(role), json=json_data, timeout=TIMEOUT)
        return resp
    except Exception as e:
        return None


def api_delete(path, role="admin"):
    """DELETE请求"""
    url = f"{BASE_URL}{path}"
    try:
        resp = requests.delete(url, headers=get_headers(role), timeout=TIMEOUT)
        return resp
    except Exception as e:
        return None


def check_success(resp, test_name, success_msg=""):
    """检查请求是否成功（code==200）"""
    if resp is None:
        print_result(test_name, False, "请求异常")
        return None
    try:
        data = resp.json()
        if data.get("code") == 200:
            print_result(test_name, True, success_msg, resp.status_code)
            return data.get("data")
        else:
            print_result(test_name, False, f"业务错误: {data.get('msg', '未知')}", resp.status_code)
            return None
    except:
        print_result(test_name, False, f"响应解析失败: {resp.text[:100]}", resp.status_code)
        return None


def check_403(resp, test_name):
    """检查是否返回403（权限拒绝）"""
    if resp is None:
        print_result(test_name, False, "请求异常")
        return
    if resp.status_code == 403:
        print_result(test_name, True, "权限拒绝正常（403）", resp.status_code)
    else:
        print_result(test_name, False, f"期望403，实际{resp.status_code}", resp.status_code)


# ============================================================
# 1. 登录测试
# ============================================================
def test_login():
    print_header("1. 登录测试")

    for role, account in ACCOUNTS.items():
        token = login(account["username"], account["password"])
        if token:
            tokens[role] = token
            print_result(f"{role} 登录成功", True, f"用户: {account['username']}")
        else:
            print_result(f"{role} 登录失败", False)

    # 测试错误密码
    resp = api_post("/api/auth/login", json_data={"username": "admin", "password": "wrong"})
    if resp and resp.status_code == 200:
        data = resp.json()
        if data.get("code") != 200:
            print_result("错误密码登录被拒绝", True, data.get("msg", ""))
        else:
            print_result("错误密码登录被拒绝", False)
    else:
        print_result("错误密码登录被拒绝", False)

    # 测试未登录访问
    resp = requests.get(f"{BASE_URL}/api/dashboard/stat", timeout=TIMEOUT)
    if resp.status_code == 401:
        print_result("未登录访问返回401", True)
    else:
        print_result("未登录访问返回401", False, f"实际{resp.status_code}")


# ============================================================
# 2. 仪表盘测试
# ============================================================
def test_dashboard():
    print_header("2. 仪表盘测试")

    for role in ["admin", "hr", "employee"]:
        resp = api_get("/api/dashboard/stat", role)
        data = check_success(resp, f"{role} 仪表盘统计", "")
        if data:
            print(f"         员工数: {data.get('employeeCount')}, 部门数: {data.get('departmentCount')}")


# ============================================================
# 3. 员工管理测试
# ============================================================
def test_employee():
    print_header("3. 员工管理测试")

    # 管理员查询员工列表
    resp = api_get("/api/employees", "admin", {"current": 1, "size": 10})
    data = check_success(resp, "管理员-员工列表", "")
    if data:
        print(f"         总数: {data.get('total')}, 本页: {len(data.get('records', []))}")

    # HR查询员工列表
    resp = api_get("/api/employees", "hr", {"current": 1, "size": 10})
    check_success(resp, "HR-员工列表")

    # 员工查询（应被拒绝）
    resp = api_get("/api/employees", "employee", {"current": 1, "size": 10})
    check_403(resp, "员工-员工列表被拒绝")

    # 新增员工
    new_emp = {
        "empNo": "E999",
        "name": "测试员工",
        "gender": "男",
        "phone": "13800138000",
        "email": "test@example.com",
        "departmentId": 1,
        "position": "测试工程师",
        "baseSalary": 8000,
        "hireDate": "2026-01-01",
        "status": "在职"
    }
    resp = api_post("/api/employees", "admin", new_emp)
    if resp and resp.status_code == 200:
        result = resp.json()
        if result.get("code") == 200:
            print_result("管理员-新增员工", True, result.get("msg", "新增成功"))
            # 查询刚新增的员工ID
            resp2 = api_get("/api/employees", "admin", {"current": 1, "size": 100, "keyword": "E999"})
            if resp2 and resp2.status_code == 200:
                data2 = resp2.json()
                if data2.get("code") == 200 and data2.get("data", {}).get("records"):
                    for emp in data2["data"]["records"]:
                        if emp.get("employeeNo") == "E999":
                            new_emp_id = emp.get("id")
                            print(f"         新员工ID: {new_emp_id}")
                            # 编辑员工
                            new_emp["id"] = new_emp_id
                            new_emp["position"] = "高级测试工程师"
                            resp3 = api_put("/api/employees", "admin", new_emp)
                            check_success(resp3, "管理员-编辑员工")
                            # 删除员工
                            resp4 = api_delete(f"/api/employees/{new_emp_id}", "admin")
                            check_success(resp4, "管理员-删除员工")
                            break
        else:
            print_result("管理员-新增员工", False, result.get("msg", "未知错误"))
    else:
        print_result("管理员-新增员工", False, "请求失败")


# ============================================================
# 4. 部门管理测试
# ============================================================
def test_department():
    print_header("4. 部门管理测试")

    # 部门列表
    resp = api_get("/api/departments", "admin")
    check_success(resp, "管理员-部门列表")

    # 部门树
    resp = api_get("/api/departments/tree", "admin")
    data = check_success(resp, "管理员-部门树", "")
    if data:
        print(f"         部门数: {len(data)}")

    # HR访问部门（应被拒绝）
    resp = api_get("/api/departments", "hr")
    check_403(resp, "HR-部门列表被拒绝")

    # 员工访问部门（应被拒绝）
    resp = api_get("/api/departments", "employee")
    check_403(resp, "员工-部门列表被拒绝")


# ============================================================
# 5. 考勤管理测试
# ============================================================
def test_attendance():
    print_header("5. 考勤管理测试")

    # 考勤列表
    resp = api_get("/api/attendance", "admin", {"current": 1, "size": 10})
    check_success(resp, "管理员-考勤列表")

    # 考勤统计（管理员没有关联员工，用HR或员工测试）
    resp = api_get("/api/attendance/stat", "employee", {"month": TEST_MONTH})
    if resp and resp.status_code == 200:
        data = resp.json()
        if data.get("code") == 200:
            print_result("员工-考勤统计", True, "", resp.status_code)
        else:
            print_result("员工-考勤统计", False, data.get("msg", ""), resp.status_code)
    else:
        print_result("员工-考勤统计", False, "请求失败")

    # 考勤月历
    resp = api_get("/api/attendance/calendar", "admin", {"employeeId": 1, "month": TEST_MONTH})
    data = check_success(resp, "管理员-考勤月历", "")
    if data:
        print(f"         天数: {len(data)}")

    # 员工查看自己的考勤
    resp = api_get("/api/attendance", "employee", {"current": 1, "size": 10})
    check_success(resp, "员工-考勤列表（仅自己）")


# ============================================================
# 6. 请假管理测试
# ============================================================
def test_leave():
    print_header("6. 请假管理测试")

    # 请假列表
    resp = api_get("/api/leaves", "admin", {"current": 1, "size": 10})
    check_success(resp, "管理员-请假列表")

    # 员工申请请假
    leave_data = {
        "type": "事假",
        "startDate": "2026-08-20",
        "endDate": "2026-08-21",
        "days": 2,
        "reason": "测试请假"
    }
    resp = api_post("/api/leaves", "employee", leave_data)
    if resp and resp.status_code == 200:
        result = resp.json()
        if result.get("code") == 200:
            print_result("员工-申请请假", True, result.get("msg", "申请成功"), resp.status_code)
            # 查询刚申请的请假记录
            resp2 = api_get("/api/leaves", "employee", {"current": 1, "size": 100})
            if resp2 and resp2.status_code == 200:
                data2 = resp2.json()
                if data2.get("code") == 200 and data2.get("data", {}).get("records"):
                    for leave in data2["data"]["records"]:
                        if leave.get("reason") == "测试请假" and leave.get("status") == "PENDING":
                            leave_id = leave.get("id")
                            # HR审批请假（参数是RequestParam）
                            resp3 = api_put(f"/api/leaves/{leave_id}/audit?status=APPROVED", "hr")
                            check_success(resp3, "HR-审批请假")
                            break
        else:
            print_result("员工-申请请假", False, result.get("msg", ""), resp.status_code)
    else:
        print_result("员工-申请请假", False, "请求失败")

    # 员工查看自己的请假
    resp = api_get("/api/leaves", "employee", {"current": 1, "size": 10})
    check_success(resp, "员工-请假列表（仅自己）")


# ============================================================
# 7. 加班管理测试
# ============================================================
def test_overtime():
    print_header("7. 加班管理测试")

    # 加班列表
    resp = api_get("/api/overtimes", "admin", {"current": 1, "size": 10})
    check_success(resp, "管理员-加班列表")

    # 员工申请加班
    ot_data = {
        "overtimeDate": "2026-08-15",
        "hours": 2,
        "reason": "测试加班"
    }
    resp = api_post("/api/overtimes", "employee", ot_data)
    data = check_success(resp, "员工-申请加班", "")
    ot_id = None
    if data:
        ot_id = data if isinstance(data, int) else data.get("id")

    # HR审批加班
    if ot_id:
        resp = api_put(f"/api/overtimes/{ot_id}/approve", "hr", {"status": "APPROVED", "remark": "同意"})
        check_success(resp, "HR-审批加班")


# ============================================================
# 8. 薪资规则测试
# ============================================================
def test_salary_rule():
    print_header("8. 薪资规则测试")

    # 管理员查询薪资规则
    resp = api_get("/api/salary-rule", "admin")
    data = check_success(resp, "管理员-薪资规则", "")
    if data:
        print(f"         社保比例: {data.get('socialSecurityRate')}, 公积金: {data.get('housingFundRate')}")

    # HR访问（应被拒绝）
    resp = api_get("/api/salary-rule", "hr")
    check_403(resp, "HR-薪资规则被拒绝")

    # 员工访问（应被拒绝）
    resp = api_get("/api/salary-rule", "employee")
    check_403(resp, "员工-薪资规则被拒绝")

    # 更新薪资规则
    if data:
        data["specialDeduction"] = 1000
        resp = api_put("/api/salary-rule", "admin", data)
        check_success(resp, "管理员-更新薪资规则（专项附加扣除1000）")


# ============================================================
# 9. 工资单测试
# ============================================================
def test_payroll():
    print_header("9. 工资单测试")

    # 生成工资单
    resp = api_post(f"/api/payrolls/generate?month={TEST_MONTH}", "admin")
    check_success(resp, "管理员-生成工资单")

    # 工资单列表
    resp = api_get("/api/payrolls", "admin", {"current": 1, "size": 10, "month": TEST_MONTH})
    data = check_success(resp, "管理员-工资单列表", "")
    if data:
        records = data.get("records", [])
        if records:
            print(f"         发放人数: {data.get('total')}, 第一人实发: {records[0].get('netSalary')}")

    # HR查看工资单
    resp = api_get("/api/payrolls", "hr", {"current": 1, "size": 10, "month": TEST_MONTH})
    check_success(resp, "HR-工资单列表")

    # 员工查看自己的工资单
    resp = api_get("/api/payrolls", "employee", {"current": 1, "size": 10, "month": TEST_MONTH})
    check_success(resp, "员工-工资单列表（仅自己）")

    # Excel导出
    resp = api_get("/api/payrolls/export", "admin", {"month": TEST_MONTH})
    if resp and resp.status_code == 200 and len(resp.content) > 1000:
        print_result("管理员-工资单Excel导出", True, f"文件大小: {len(resp.content)} bytes")
    else:
        print_result("管理员-工资单Excel导出", False, f"状态: {resp.status_code if resp else 'None'}")


# ============================================================
# 10. 系统监控测试
# ============================================================
def test_system_monitor():
    print_header("10. 系统监控测试")

    # 管理员访问
    resp = api_get("/api/system/monitor", "admin")
    data = check_success(resp, "管理员-系统监控", "")
    if data:
        print(f"         CPU核心: {data.get('cpuCores')}, JVM内存: {data.get('jvmUsed')}/{data.get('jvmMax')}")

    # HR访问（应被拒绝）
    resp = api_get("/api/system/monitor", "hr")
    check_403(resp, "HR-系统监控被拒绝")

    # 员工访问（应被拒绝）
    resp = api_get("/api/system/monitor", "employee")
    check_403(resp, "员工-系统监控被拒绝")


# ============================================================
# 11. 统计报表测试
# ============================================================
def test_reports():
    print_header("11. 统计报表测试")

    # 人员统计
    resp = api_get("/api/reports/employee", "admin")
    data = check_success(resp, "管理员-人员统计报表", "")
    if data:
        print(f"         在职员工: {data.get('totalEmployees')}")

    # 考勤统计
    resp = api_get("/api/reports/attendance", "admin", {"month": TEST_MONTH})
    data = check_success(resp, "管理员-考勤统计报表", "")
    if data:
        print(f"         正常: {data.get('normalCount')}, 迟到: {data.get('lateCount')}")

    # 薪资统计
    resp = api_get("/api/reports/salary", "admin", {"month": TEST_MONTH})
    data = check_success(resp, "管理员-薪资成本报表", "")
    if data:
        print(f"         应发总额: {data.get('totalGross')}, 实发总额: {data.get('totalNet')}")

    # HR访问报表
    resp = api_get("/api/reports/employee", "hr")
    check_success(resp, "HR-人员统计报表")

    # 员工访问（应被拒绝）
    resp = api_get("/api/reports/employee", "employee")
    check_403(resp, "员工-统计报表被拒绝")


# ============================================================
# 12. 工作流测试
# ============================================================
def test_workflow():
    print_header("12. 工作流测试")

    # 流程定义
    resp = api_get("/api/workflow/processes", "admin")
    data = check_success(resp, "工作流-流程定义", "")
    if data:
        print(f"         流程数: {len(data)}")

    # 发起流程
    resp = api_post("/api/workflow/start", "employee", {
        "processCode": "LEAVE_APPROVAL",
        "businessType": "LEAVE",
        "title": "测试请假审批"
    })
    data = check_success(resp, "员工-发起工作流", "")
    instance_id = None
    if data:
        instance_id = data.get("id") if isinstance(data, dict) else None
        print(f"         流程实例ID: {instance_id}")

    # 待办任务（HR）- 返回分页对象
    resp = api_get("/api/workflow/tasks/pending", "hr", {"current": 1, "size": 10})
    task_id = None
    if resp and resp.status_code == 200:
        result = resp.json()
        if result.get("code") == 200:
            page_data = result.get("data", {})
            records = page_data.get("records", []) if isinstance(page_data, dict) else []
            print_result("HR-待办任务", True, f"待办数: {len(records)}", resp.status_code)
            if records:
                task_id = records[0].get("id")
        else:
            print_result("HR-待办任务", False, result.get("msg", ""), resp.status_code)
    else:
        print_result("HR-待办任务", False, "请求失败")

    # HR审批
    if task_id:
        resp = api_put(f"/api/workflow/tasks/{task_id}/approve", "hr",
                       {"status": "APPROVED", "comment": "HR同意"})
        check_success(resp, "HR-审批工作流任务")

    # 已办任务
    resp = api_get("/api/workflow/tasks/approved", "hr", {"current": 1, "size": 10})
    if resp and resp.status_code == 200:
        result = resp.json()
        if result.get("code") == 200:
            page_data = result.get("data", {})
            records = page_data.get("records", []) if isinstance(page_data, dict) else []
            print_result("HR-已办任务", True, f"已办数: {len(records)}", resp.status_code)
        else:
            print_result("HR-已办任务", False, result.get("msg", ""), resp.status_code)
    else:
        print_result("HR-已办任务", False, "请求失败")

    # 我发起的
    resp = api_get("/api/workflow/instances/my", "employee", {"current": 1, "size": 10})
    if resp and resp.status_code == 200:
        result = resp.json()
        if result.get("code") == 200:
            page_data = result.get("data", {})
            records = page_data.get("records", []) if isinstance(page_data, dict) else []
            print_result("员工-我发起的流程", True, f"发起数: {len(records)}", resp.status_code)
        else:
            print_result("员工-我发起的流程", False, result.get("msg", ""), resp.status_code)
    else:
        print_result("员工-我发起的流程", False, "请求失败")

    # 审批历史
    if instance_id:
        resp = api_get(f"/api/workflow/instances/{instance_id}/history", "employee")
        check_success(resp, "员工-审批历史")


# ============================================================
# 13. 租户管理测试
# ============================================================
def test_tenant():
    print_header("13. 租户管理测试")

    # 管理员查询租户
    resp = api_get("/api/tenants", "admin", {"current": 1, "size": 10})
    check_success(resp, "管理员-租户列表")

    # 所有启用租户
    resp = api_get("/api/tenants/all", "admin")
    check_success(resp, "管理员-所有启用租户")

    # 新增租户
    new_tenant = {
        "tenantCode": "TEST",
        "tenantName": "测试企业",
        "contactPerson": "测试联系人",
        "contactPhone": "400-888-8888",
        "address": "测试地址",
        "status": "启用"
    }
    resp = api_post("/api/tenants", "admin", new_tenant)
    if resp and resp.status_code == 200:
        result = resp.json()
        if result.get("code") == 200:
            print_result("管理员-新增租户", True, result.get("msg", "新增成功"), resp.status_code)
            # 查询刚新增的租户
            resp2 = api_get("/api/tenants", "admin", {"current": 1, "size": 100})
            if resp2 and resp2.status_code == 200:
                data2 = resp2.json()
                if data2.get("code") == 200 and data2.get("data", {}).get("records"):
                    for tenant in data2["data"]["records"]:
                        if tenant.get("tenantCode") == "TEST":
                            tenant_id = tenant.get("id")
                            # 删除租户
                            resp3 = api_delete(f"/api/tenants/{tenant_id}", "admin")
                            check_success(resp3, "管理员-删除租户")
                            break
        else:
            print_result("管理员-新增租户", False, result.get("msg", "未知错误"), resp.status_code)
    else:
        print_result("管理员-新增租户", False, "请求失败")

    # HR访问（应被拒绝）
    resp = api_get("/api/tenants", "hr")
    check_403(resp, "HR-租户管理被拒绝")

    # 员工访问（应被拒绝）
    resp = api_get("/api/tenants", "employee")
    check_403(resp, "员工-租户管理被拒绝")


# ============================================================
# 14. 操作日志测试
# ============================================================
def test_operation_log():
    print_header("14. 操作日志测试")

    # 管理员查询
    resp = api_get("/api/operation-logs", "admin", {"current": 1, "size": 10})
    data = check_success(resp, "管理员-操作日志", "")
    if data:
        print(f"         总记录: {data.get('total')}")

    # HR访问（应被拒绝）
    resp = api_get("/api/operation-logs", "hr")
    check_403(resp, "HR-操作日志被拒绝")

    # 员工访问（应被拒绝）
    resp = api_get("/api/operation-logs", "employee")
    check_403(resp, "员工-操作日志被拒绝")


# ============================================================
# 15. 消息通知测试
# ============================================================
def test_notification():
    print_header("15. 消息通知测试")

    # 未读消息数
    resp = api_get("/api/notifications/unread-count", "admin")
    data = check_success(resp, "管理员-未读消息数", "")
    if data is not None:
        print(f"         未读数: {data}")

    # 消息列表
    resp = api_get("/api/notifications", "admin", {"current": 1, "size": 10})
    data = check_success(resp, "管理员-消息列表", "")
    msg_id = None
    if data:
        records = data.get("records", [])
        if records:
            msg_id = records[0].get("id")
            print(f"         消息数: {data.get('total')}")

    # 消息概览
    resp = api_get("/api/notifications/overview", "admin")
    check_success(resp, "管理员-消息概览")

    # 标记已读
    if msg_id:
        resp = api_put(f"/api/notifications/{msg_id}/read", "admin")
        check_success(resp, "管理员-标记单条已读")

    # 全部已读
    resp = api_put("/api/notifications/read-all", "admin")
    check_success(resp, "管理员-全部标记已读")

    # 员工查看自己的消息
    resp = api_get("/api/notifications", "employee", {"current": 1, "size": 10})
    check_success(resp, "员工-消息列表（仅自己）")


# ============================================================
# 16. 数据备份测试
# ============================================================
def test_backup():
    print_header("16. 数据备份测试")

    # 创建备份
    resp = api_post("/api/backup/create", "admin")
    data = check_success(resp, "管理员-创建备份", "")
    backup_name = None
    if data:
        backup_name = data.get("fileName") if isinstance(data, dict) else None
        print(f"         文件名: {backup_name}, 大小: {data.get('fileSize') if isinstance(data, dict) else ''}")

    # 备份列表
    resp = api_get("/api/backup/list", "admin")
    data = check_success(resp, "管理员-备份列表", "")
    if data:
        print(f"         备份数: {len(data)}")

    # 下载备份
    if backup_name:
        resp = api_get(f"/api/backup/download/{backup_name}", "admin")
        if resp and resp.status_code == 200 and len(resp.content) > 1000:
            print_result("管理员-下载备份", True, f"大小: {len(resp.content)} bytes")
        else:
            print_result("管理员-下载备份", False)

        # 删除备份
        resp = api_delete(f"/api/backup/{backup_name}", "admin")
        check_success(resp, "管理员-删除备份")

    # HR访问（应被拒绝）
    resp = api_get("/api/backup/list", "hr")
    check_403(resp, "HR-数据备份被拒绝")

    # 员工访问（应被拒绝）
    resp = api_get("/api/backup/list", "employee")
    check_403(resp, "员工-数据备份被拒绝")


# ============================================================
# 17. 修改密码测试
# ============================================================
def test_change_password():
    print_header("17. 修改密码测试")

    # 修改密码（测试后改回）
    pwd_data = {
        "oldPassword": "123456",
        "newPassword": "654321"
    }
    resp = api_post("/api/auth/change-password", "employee", pwd_data)
    check_success(resp, "员工-修改密码")

    # 用新密码登录
    new_token = login("E001", "654321")
    if new_token:
        tokens["employee"] = new_token
        print_result("新密码登录成功", True)
    else:
        print_result("新密码登录成功", False)

    # 改回原密码
    pwd_data2 = {
        "oldPassword": "654321",
        "newPassword": "123456"
    }
    resp = api_post("/api/auth/change-password", "employee", pwd_data2)
    check_success(resp, "员工-密码改回原值")

    # 恢复token
    tokens["employee"] = login("E001", "123456")


# ============================================================
# 生成测试报告
# ============================================================
def generate_report():
    """生成测试报告"""
    print_header("测试报告")

    total = stats["total"]
    passed = stats["passed"]
    failed = stats["failed"]
    pass_rate = (passed / total * 100) if total > 0 else 0

    print(f"\n  总测试数: {total}")
    print(f"  {Fore.GREEN}通过: {passed}{Style.RESET_ALL}")
    print(f"  {Fore.RED}失败: {failed}{Style.RESET_ALL}")
    print(f"  通过率: {pass_rate:.1f}%")

    if failed > 0:
        print(f"\n  {Fore.RED}失败详情:{Style.RESET_ALL}")
        for d in stats["details"]:
            if not d["passed"]:
                print(f"    - {d['name']}: {d['message']}")

    # 保存报告到文件
    report_file = "test_report.txt"
    with open(report_file, "w", encoding="utf-8") as f:
        f.write("=" * 60 + "\n")
        f.write("考勤薪资管理系统 - API 自动化测试报告\n")
        f.write(f"测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write("=" * 60 + "\n\n")
        f.write(f"总测试数: {total}\n")
        f.write(f"通过: {passed}\n")
        f.write(f"失败: {failed}\n")
        f.write(f"通过率: {pass_rate:.1f}%\n\n")
        f.write("详细结果:\n")
        f.write("-" * 60 + "\n")
        for d in stats["details"]:
            status = "PASS" if d["passed"] else "FAIL"
            f.write(f"[{status}] {d['name']}")
            if d["message"]:
                f.write(f" - {d['message']}")
            f.write("\n")
        f.write("-" * 60 + "\n")

    print(f"\n  报告已保存到: {report_file}")
    return pass_rate == 100


# ============================================================
# 主函数
# ============================================================
def main():
    print(f"\n{Fore.MAGENTA}{Style.BRIGHT}")
    print("╔══════════════════════════════════════════════════════╗")
    print("║   考勤薪资管理系统 - API 自动化测试程序              ║")
    print("║   Attendance & Salary System - API Test Suite        ║")
    print("╚══════════════════════════════════════════════════════╝")
    print(f"{Style.RESET_ALL}")
    print(f"  测试地址: {BASE_URL}")
    print(f"  测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"  测试模块: 17个模块，覆盖所有API和三角色权限")

    # 检查服务是否启动
    try:
        resp = requests.get(f"{BASE_URL}/api/auth/login", timeout=5)
    except:
        print(f"\n  {Fore.RED}❌ 无法连接到服务器 {BASE_URL}")
        print(f"  请先启动后端服务后再运行测试！{Style.RESET_ALL}")
        return

    # 按顺序执行测试（每个测试加异常保护）
    test_funcs = [
        test_login, test_dashboard, test_employee, test_department,
        test_attendance, test_leave, test_overtime, test_salary_rule,
        test_payroll, test_system_monitor, test_reports, test_workflow,
        test_tenant, test_operation_log, test_notification, test_backup,
        test_change_password
    ]

    for test_func in test_funcs:
        try:
            test_func()
        except Exception as e:
            print(f"\n  {Fore.RED}⚠️  测试异常: {test_func.__name__}: {e}{Style.RESET_ALL}")
            import traceback
            traceback.print_exc()

    # 生成报告
    all_passed = generate_report()

    if all_passed:
        print(f"\n  {Fore.GREEN}{Style.BRIGHT}🎉 所有测试通过！{Style.RESET_ALL}")
    else:
        print(f"\n  {Fore.YELLOW}⚠️  部分测试失败，请检查上方详情{Style.RESET_ALL}")


if __name__ == "__main__":
    main()

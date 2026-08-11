# -*- coding: utf-8 -*-
"""
测试配置文件
"""

# 服务器地址
BASE_URL = "http://localhost:8080"

# 测试账号
ACCOUNTS = {
    "admin": {
        "username": "admin",
        "password": "admin123",
        "role": "ADMIN"
    },
    "hr": {
        "username": "hr",
        "password": "hr123456",
        "role": "HR"
    },
    "employee": {
        "username": "E001",
        "password": "123456",
        "role": "EMPLOYEE"
    }
}

# 测试月份
TEST_MONTH = "2026-08"

# 请求超时（秒）
TIMEOUT = 10

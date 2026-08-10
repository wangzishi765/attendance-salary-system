package com.example.hrms.controller;

import com.example.hrms.common.Result;
import com.example.hrms.dto.LoginRequest;
import com.example.hrms.dto.LoginResponse;
import com.example.hrms.entity.SysUser;
import com.example.hrms.security.SecurityUtil;
import com.example.hrms.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        return Result.success("登录成功", authService.login(request));
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        SysUser user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return Result.error(401, "未登录");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getId());
        map.put("username", user.getUsername());
        map.put("realName", user.getRealName());
        map.put("role", user.getRole());
        map.put("employeeId", user.getEmployeeId());
        return Result.success(map);
    }
}

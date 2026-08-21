package com.example.hrms.controller;

import com.example.hrms.common.Result;
import com.example.hrms.dto.ChangePasswordRequest;
import com.example.hrms.dto.LoginRequest;
import com.example.hrms.dto.LoginResponse;
import com.example.hrms.entity.SysUser;
import com.example.hrms.security.SecurityUtil;
import com.example.hrms.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        return Result.success("登录成功", authService.login(request));
    }

    @ApiOperation("获取当前用户信息")
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

    @PostMapping("/change-password")
    public Result<?> changePassword(@Validated @RequestBody ChangePasswordRequest request) {
        SysUser user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return Result.error(401, "未登录");
        }
        authService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());
        return Result.success("密码修改成功，请重新登录", null);
    }
}

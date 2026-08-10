package com.example.hrms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hrms.common.BizException;
import com.example.hrms.dto.LoginRequest;
import com.example.hrms.dto.LoginResponse;
import com.example.hrms.entity.SysUser;
import com.example.hrms.mapper.SysUserMapper;
import com.example.hrms.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (user.getEnabled() == null || user.getEnabled() != 1) {
            throw new BizException("账号已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(),
                user.getRole(), user.getEmployeeId());
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .employeeId(user.getEmployeeId())
                .build();
    }

    /** 修改当前登录用户密码 */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BizException("新密码长度不能少于6位");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);
    }
}

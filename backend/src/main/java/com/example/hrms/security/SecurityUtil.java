package com.example.hrms.security;

import com.example.hrms.entity.SysUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前登录用户的工具
 */
public class SecurityUtil {

    public static SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            return null;
        }
        return ((LoginUser) authentication.getPrincipal()).getSysUser();
    }

    public static boolean isAdmin() {
        SysUser user = getCurrentUser();
        return user != null && "ADMIN".equals(user.getRole());
    }

    public static boolean isAdminOrHr() {
        SysUser user = getCurrentUser();
        return user != null && ("ADMIN".equals(user.getRole()) || "HR".equals(user.getRole()));
    }
}

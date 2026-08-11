package com.example.hrms.config;

import com.example.hrms.entity.OperationLog;
import com.example.hrms.entity.SysUser;
import com.example.hrms.security.SecurityUtil;
import com.example.hrms.service.OperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志AOP切面
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 拦截所有Controller的方法，排除登录和查询类接口（避免日志过多）
    @Around("execution(* com.example.hrms.controller..*.*(..)) && !execution(* com.example.hrms.controller.AuthController.login(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        String method = request != null ? request.getMethod() : "";
        String uri = request != null ? request.getRequestURI() : "";
        String ip = request != null ? getClientIp(request) : "";

        // 只记录写操作（POST/PUT/DELETE），GET操作不记录（避免日志过多）
        boolean isWriteOperation = "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);

        if (!isWriteOperation) {
            return joinPoint.proceed();
        }

        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        // 解析模块名（从Controller类名提取）
        String module = className.replace("Controller", "");

        // 操作类型
        String operation = method;
        if ("POST".equals(method)) operation = "新增";
        else if ("PUT".equals(method)) operation = "修改";
        else if ("DELETE".equals(method)) operation = "删除";

        // 获取请求参数
        String params = "";
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                // 过滤掉不能序列化的参数（如HttpServletRequest/Response）
                Object[] serializableArgs = Arrays.stream(args)
                        .filter(arg -> !(arg instanceof javax.servlet.ServletRequest)
                                && !(arg instanceof javax.servlet.ServletResponse))
                        .toArray();
                if (serializableArgs.length > 0) {
                    params = objectMapper.writeValueAsString(serializableArgs);
                    if (params.length() > 2000) {
                        params = params.substring(0, 2000) + "...";
                    }
                }
            }
        } catch (Exception e) {
            params = "参数解析失败";
        }

        // 获取当前用户
        String username = "anonymous";
        String realName = "匿名用户";
        Long userId = null;
        try {
            SysUser user = SecurityUtil.getCurrentUser();
            if (user != null) {
                userId = user.getId();
                username = user.getUsername();
                realName = user.getRealName();
            }
        } catch (Exception e) {
            // 未登录用户
        }

        // 执行目标方法
        Object result = null;
        String status = "SUCCESS";
        String errorMsg = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            status = "FAIL";
            errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 500) {
                errorMsg = errorMsg.substring(0, 500);
            }
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;

            // 保存日志
            try {
                OperationLog opLog = new OperationLog();
                opLog.setUserId(userId);
                opLog.setUsername(username);
                opLog.setRealName(realName);
                opLog.setModule(module);
                opLog.setOperation(operation);
                opLog.setDescription(methodName);
                opLog.setMethod(className + "." + methodName);
                opLog.setParams(params);
                opLog.setIp(ip);
                opLog.setStatus(status);
                opLog.setErrorMsg(errorMsg);
                opLog.setCostTime(costTime);
                opLog.setOperationTime(LocalDateTime.now());
                operationLogService.save(opLog);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }

        return result;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

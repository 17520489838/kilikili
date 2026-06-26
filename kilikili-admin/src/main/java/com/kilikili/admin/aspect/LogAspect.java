package com.kilikili.admin.aspect;

import com.kilikili.entity.po.OperationLog;
import com.kilikili.service.OperationLogService;
import com.kilikili.utils.StringTools;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;

import static com.kilikili.entity.constants.Constants.LENGTH_10;

@Aspect
@Component
public class LogAspect {

    @Resource
    private OperationLogService operationLogService;

    @Around("execution(* com.kilikili.admin.controller.*.*(..))")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // Skip logging for non-operation endpoints
        if (className.contains("IndexController") || className.contains("AccountController")
                || className.contains("ABaseController") || className.contains("GlobalExceptionHandler")) {
            return joinPoint.proceed();
        }

        // Determine module name from controller
        String operModule = getModuleName(className);
        String operType = getOperationType(methodName);
        String operDesc = operModule + " - " + operType;

        // Get request params (truncate long strings)
        String params = Arrays.toString(joinPoint.getArgs());
        if (params.length() > 1000) {
            params = params.substring(0, 1000) + "...";
        }

        String operUserId = "";
        String operUserName = "";

        // Try to get admin info from session/attribute
        if (request.getAttribute("adminInfo") != null) {
            Object adminInfo = request.getAttribute("adminInfo");
            try {
                Method getUserId = adminInfo.getClass().getMethod("getUserId");
                Method getNickName = adminInfo.getClass().getMethod("getNickName");
                operUserId = String.valueOf(getUserId.invoke(adminInfo));
                operUserName = String.valueOf(getNickName.invoke(adminInfo));
            } catch (Exception ignored) {}
        }

        String operIp = getIpAddress(request);
        String requestUrl = request.getRequestURI();
        int status = 1;
        String errorMsg = null;

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            status = 0;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            try {
                OperationLog log = new OperationLog();
                log.setOperId(StringTools.getRandomNumber(LENGTH_10));
                log.setOperModule(operModule);
                log.setOperType(operType);
                log.setOperDesc(operDesc);
                log.setRequestUrl(requestUrl);
                log.setRequestParams(params);
                log.setOperUserId(operUserId);
                log.setOperUserName(operUserName);
                log.setOperIp(operIp);
                log.setStatus(status);
                log.setErrorMsg(errorMsg);
                log.setCreateTime(new Date());
                operationLogService.saveLog(log);
            } catch (Exception ignored) {
                // Don't let logging failure affect the main operation
            }
        }
        return result;
    }

    private String getModuleName(String className) {
        if (className.contains("Video")) return "视频管理";
        if (className.contains("Comment") || className.contains("Interact")) return "互动管理";
        if (className.contains("Danmu")) return "弹幕管理";  // for future
        if (className.contains("UserController")) return "用户管理";
        if (className.contains("Category")) return "分类管理";
        if (className.contains("Setting")) return "系统设置";
        if (className.contains("File")) return "文件管理";
        if (className.contains("OperationLog")) return "操作日志";
        return "其他";
    }

    private String getOperationType(String methodName) {
        if (methodName.startsWith("del") || methodName.startsWith("delete")) return "删除";
        if (methodName.startsWith("save") || methodName.startsWith("insert")
                || methodName.startsWith("add") || methodName.startsWith("create")) return "新增";
        if (methodName.startsWith("update") || methodName.startsWith("edit")
                || methodName.startsWith("modify")) return "修改";
        if (methodName.startsWith("load") || methodName.startsWith("get")
                || methodName.startsWith("select") || methodName.startsWith("query")) return "查询";
        if (methodName.contains("Top") || methodName.contains("top")) return "置顶";
        return methodName;
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip : "";
    }
}

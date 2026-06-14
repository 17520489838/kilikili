package com.kilikili.admin.interceptor;

import com.kilikili.component.RedisComponent;
import com.kilikili.utils.StringTools;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AppInterceptor implements HandlerInterceptor {
    private final static String URL_ACCOUNT = "/account";
    private final static String URL_FILE = "/file";

    @Resource
    private RedisComponent redisComponent;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (null == handler) {
            return false;
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        String path = request.getRequestURI();
        if (path.contains(URL_ACCOUNT) || path.contains(URL_FILE)) {
            return true;
        }
        // 优先从 Cookie 获取 admin_token，确保不被 web 端的 token Cookie 干扰
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("admin_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        // 如果没有 admin_token，再尝试从请求头获取（用于 API 调用场景）
        if (StringTools.isEmpty(token)) {
            token = request.getHeader("token");
        }
        if (StringTools.isEmpty(token)) {
            throw new RuntimeException("请先登录");
        }
        Object adminInfo = redisComponent.getTokenUserInfoAdmin(token);
        if (null == adminInfo) {
            throw new RuntimeException("登录已过期，请重新登录");
        }
        request.setAttribute("token", token);
        return true;
    }
}

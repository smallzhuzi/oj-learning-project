package com.ojplatform.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojplatform.common.Result;
import com.ojplatform.entity.User;
import com.ojplatform.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理请求拦截器。
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            writeForbidden(response, "未登录");
            return false;
        }

        User user = userService.getById(userId);
        if (user == null || !"admin".equals(user.getRole())) {
            writeForbidden(response, "权限不足，仅管理员可访问");
            return false;
        }

        return true;
    }

    private void writeForbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(403, message)));
    }
}

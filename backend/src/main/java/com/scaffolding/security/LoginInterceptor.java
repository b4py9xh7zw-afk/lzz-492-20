package com.scaffolding.security;

import com.scaffolding.entity.User;
import com.scaffolding.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录态拦截器：读取前端请求头 X-User-Id，加载用户放入 ThreadLocal
 *
 * @author scaffolding
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String userIdHeader = request.getHeader("X-User-Id");
        if (StringUtils.hasText(userIdHeader)) {
            try {
                Long userId = Long.valueOf(userIdHeader);
                User user = userService.getById(userId);
                if (user != null) {
                    String role = StringUtils.hasText(user.getRole()) ? user.getRole() : "admin";
                    CurrentUserHolder.set(new LoginUser(user.getId(), user.getNickname(), role,
                            user.getProjectId(), user.getCompanyName()));
                }
            } catch (NumberFormatException ignored) {
                // 非法头部按未登录处理
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        CurrentUserHolder.clear();
    }
}

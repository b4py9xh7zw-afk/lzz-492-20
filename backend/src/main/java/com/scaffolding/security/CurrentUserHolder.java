package com.scaffolding.security;

import com.scaffolding.exception.BusinessException;

/**
 * 当前登录用户 ThreadLocal
 *
 * @author scaffolding
 */
public class CurrentUserHolder {

    private static final ThreadLocal<LoginUser> CONTEXT = new ThreadLocal<>();

    public static void set(LoginUser user) {
        CONTEXT.set(user);
    }

    public static LoginUser get() {
        return CONTEXT.get();
    }

    /** 获取登录用户，未登录直接抛业务异常 */
    public static LoginUser require() {
        LoginUser user = CONTEXT.get();
        if (user == null || user.getUserId() == null) {
            throw new BusinessException(401, "未登录或登录已失效");
        }
        return user;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

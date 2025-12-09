package com.coffee.common.context;

/**
 * 用户上下文工具类 (基于 ThreadLocal)
 * 用于在当前线程中存储和获取当前登录用户的 ID
 */
public class UserContext {
    
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 必须在请求结束时调用，防止内存泄漏 (ThreadLocal 在线程池环境下必须清理)
     */
    public static void remove() {
        USER_ID_HOLDER.remove();
    }
}
package com.coffee.common.cache;

/**
 * 缓存 Key 常量
 * 统一管理缓存键，避免硬编码和拼写错误
 */
public class CacheKeyConstants {
    
    /**
     * 商品相关缓存
     */
    public static class Product {
        private static final String PREFIX = "product:";
        public static final String LIST = PREFIX + "list"; // 商品列表
        public static final String DETAIL = PREFIX + "detail:"; // 商品详情，需要拼接 ID
        public static final String APP_DETAIL = PREFIX + "app:detail:"; // C端商品详情(包含规格)，需要拼接 ID -> 新增这个
        public static final String MENU = PREFIX + "menu"; // 菜单数据
    }
    
    /**
     * 分类相关缓存
     */
    public static class Category {
        private static final String PREFIX = "category:";
        public static final String ENABLED = PREFIX + "enabled"; // 启用的分类列表
        public static final String LIST = PREFIX + "list"; // 分类列表
        public static final String DETAIL = PREFIX + "detail:"; // 分类详情，需要拼接 ID
    }
    
    /**
     * 订单相关缓存
     */
    public static class Order {
        private static final String PREFIX = "order:";
        public static final String LIST = PREFIX + "list"; // 订单列表
        public static final String DETAIL = PREFIX + "detail:"; // 订单详情，需要拼接 ID
    }
    
    /**
     * 用户相关缓存
     */
    public static class User {
        private static final String PREFIX = "user";
        public static final String INFO = PREFIX + ":info"; // 用户信息
        public static final String STATISTICS = PREFIX + ":statistics"; // 用户统计
    }
    
    /**
     * 生成带参数的缓存 key
     */
    public static String buildKey(String prefix, Object... params) {
        StringBuilder key = new StringBuilder(prefix);
        for (Object param : params) {
            key.append(":").append(param == null ? "null" : param);
        }
        return key.toString();
    }
}


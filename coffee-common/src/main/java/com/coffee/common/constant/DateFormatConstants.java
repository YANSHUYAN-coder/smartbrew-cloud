package com.coffee.common.constant;

import java.time.format.DateTimeFormatter;

/**
 * 日期格式化常量类
 * 统一管理项目中使用的日期格式化模式
 */
public class DateFormatConstants {
    
    /**
     * 日期时间格式：yyyyMMddHHmmss（用于订单号等）
     */
    public static final DateTimeFormatter DATETIME_COMPACT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    /**
     * 日期格式：yyyyMMdd（用于优惠券代码等）
     */
    public static final DateTimeFormatter DATE_COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /**
     * 日期格式：MM-dd（用于图表显示等）
     */
    public static final DateTimeFormatter MONTH_DAY = DateTimeFormatter.ofPattern("MM-dd");
    
    private DateFormatConstants() {
        // 工具类，禁止实例化
    }
}


package com.coffee.system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 数据大屏服务接口
 */
public interface DashboardService {
    /**
     * 获取今日销售额
     * @return 今日销售额
     */
    BigDecimal getTodaySales();

    /**
     * 获取今日订单量
     * @return 今日订单数
     */
    Long getTodayOrderCount();

    /**
     * 获取今日新增会员数
     * @return 今日新增会员数
     */
    Long getTodayNewMembers();

    /**
     * 获取销售趋势数据（最近7天）
     * @return 日期和销售额的列表
     */
    List<Map<String, Object>> getSalesTrend(int days);

    /**
     * 获取热销商品Top N
     * @param topN Top数量
     * @return 商品名称和销量列表
     */
    List<Map<String, Object>> getTopProducts(int topN);
}


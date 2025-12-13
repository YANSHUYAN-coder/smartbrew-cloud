package com.coffee.admin.controller;

import com.coffee.common.result.Result;
import com.coffee.system.service.DashboardService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据大屏控制器
 */
@RestController
@RequestMapping("/admin/dashboard")
@AllArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('dashboard:view')")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 今日销售额
        statistics.put("todaySales", dashboardService.getTodaySales());
        
        // 今日订单量
        statistics.put("todayOrderCount", dashboardService.getTodayOrderCount());
        
        // AI服务调用次数（暂时使用模拟数据，后续可对接真实AI服务统计）
        statistics.put("aiServiceCalls", 5230L);
        
        // 今日新增会员
        statistics.put("todayNewMembers", dashboardService.getTodayNewMembers());
        
        return Result.success(statistics);
    }

    /**
     * 获取销售趋势（最近7天）
     */
    @GetMapping("/sales-trend")
    @PreAuthorize("hasAuthority('dashboard:view')")
    public Result<List<Map<String, Object>>> getSalesTrend(
            @RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> trend = dashboardService.getSalesTrend(days);
        return Result.success(trend);
    }

    /**
     * 获取热销商品Top 5
     */
    @GetMapping("/top-products")
    @PreAuthorize("hasAuthority('dashboard:view')")
    public Result<List<Map<String, Object>>> getTopProducts(
            @RequestParam(defaultValue = "5") int topN) {
        List<Map<String, Object>> topProducts = dashboardService.getTopProducts(topN);
        return Result.success(topProducts);
    }
}


package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffee.common.constant.DateFormatConstants;
import com.coffee.system.domain.entity.OmsOrder;
import com.coffee.system.domain.entity.OmsOrderItem;
import com.coffee.system.domain.entity.UmsMember;
import com.coffee.system.mapper.OrderItemMapper;
import com.coffee.system.mapper.OmsOrderMapper;
import com.coffee.system.mapper.UmsMemberMapper;
import com.coffee.system.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据大屏服务实现
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private OmsOrderMapper orderMapper;

    @Autowired
    private UmsMemberMapper memberMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public static final String AI_CALL_COUNT_KEY = "stats:ai:call_count";

    @Override
    public BigDecimal getTodaySales() {
        return getSalesByDate(LocalDate.now());
    }

    private BigDecimal getSalesByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(OmsOrder::getCreateTime, startOfDay, endOfDay)
               .eq(OmsOrder::getStatus, 3); // 只统计已完成的订单

        List<OmsOrder> orders = orderMapper.selectList(wrapper);
        return orders.stream()
                .filter(order -> order.getPayAmount() != null)
                .map(OmsOrder::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Long getTodayOrderCount() {
        return getOrderCountByDate(LocalDate.now());
    }

    private Long getOrderCountByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(OmsOrder::getCreateTime, startOfDay, endOfDay);

        return orderMapper.selectCount(wrapper);
    }

    @Override
    public Long getTodayNewMembers() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        LambdaQueryWrapper<UmsMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(UmsMember::getCreateTime, startOfDay, endOfDay);

        return memberMapper.selectCount(wrapper);
    }

    @Override
    public Long getAiServiceCalls() {
        String count = redisTemplate.opsForValue().get(AI_CALL_COUNT_KEY);
        return count != null ? Long.parseLong(count) : 0L;
    }

    @Override
    public BigDecimal getSalesGrowth() {
        BigDecimal todaySales = getTodaySales();
        BigDecimal yesterdaySales = getSalesByDate(LocalDate.now().minusDays(1));
        return calculateGrowth(todaySales, yesterdaySales);
    }

    @Override
    public BigDecimal getOrderGrowth() {
        Long todayCount = getTodayOrderCount();
        Long yesterdayCount = getOrderCountByDate(LocalDate.now().minusDays(1));
        return calculateGrowth(new BigDecimal(todayCount), new BigDecimal(yesterdayCount));
    }

    private BigDecimal calculateGrowth(BigDecimal today, BigDecimal yesterday) {
        if (yesterday.compareTo(BigDecimal.ZERO) == 0) {
            return today.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100.0") : BigDecimal.ZERO;
        }
        return today.subtract(yesterday)
                .divide(yesterday, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(1, RoundingMode.HALF_UP);
    }

    @Override
    public List<Map<String, Object>> getSalesTrend(int days) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateFormatConstants.MONTH_DAY;

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);

            LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(OmsOrder::getCreateTime, startOfDay, endOfDay)
                   .eq(OmsOrder::getStatus, 3); // 只统计已完成的订单

            List<OmsOrder> orders = orderMapper.selectList(wrapper);
            BigDecimal totalSales = orders.stream()
                    .filter(order -> order.getPayAmount() != null)
                    .map(OmsOrder::getPayAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(formatter));
            dayData.put("sales", totalSales.doubleValue());
            result.add(dayData);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getTopProducts(int topN) {
        // 查询已完成的订单
        LambdaQueryWrapper<OmsOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(OmsOrder::getStatus, 3); // 已完成
        List<OmsOrder> completedOrders = orderMapper.selectList(orderWrapper);

        if (completedOrders.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取订单ID列表
        List<Long> orderIds = completedOrders.stream()
                .map(OmsOrder::getId)
                .collect(Collectors.toList());

        // 查询订单商品明细
        LambdaQueryWrapper<OmsOrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OmsOrderItem::getOrderId, orderIds);
        List<OmsOrderItem> orderItems = orderItemMapper.selectList(itemWrapper);

        // 按商品名称统计销量
        Map<String, Integer> productSales = new HashMap<>();
        for (OmsOrderItem item : orderItems) {
            String productName = item.getProductName();
            Integer quantity = item.getProductQuantity() != null ? item.getProductQuantity() : 0;
            productSales.put(productName, productSales.getOrDefault(productName, 0) + quantity);
        }

        // 排序并取Top N
        return productSales.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(topN)
                .map(entry -> {
                    Map<String, Object> product = new HashMap<>();
                    product.put("name", entry.getKey());
                    product.put("sales", entry.getValue());
                    return product;
                })
                .collect(Collectors.toList());
    }
}


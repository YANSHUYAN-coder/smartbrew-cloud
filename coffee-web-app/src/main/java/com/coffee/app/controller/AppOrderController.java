package com.coffee.app.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.common.dict.OrderStatus;
import com.coffee.common.context.UserContext;
import com.coffee.common.dto.CreateOrderRequest;
import com.coffee.common.dto.PageParam;
import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.OmsOrder;
import com.coffee.system.domain.vo.OrderVO;
import com.coffee.system.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * C 端订单接口
 * 提供小程序/APP 端使用的下单、查询订单列表和订单详情等接口
 */
@RestController
@RequestMapping("/app/order")
@Tag(name = "C端-订单接口", description = "提供小程序/APP 端使用的下单与订单查询能力")
public class AppOrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('app:order:create')")
    @Operation(summary = "创建订单", description = "根据前端提交的收货地址与购物车商品信息生成订单")
    public Result<OmsOrder> createOrder(
            @RequestBody CreateOrderRequest request) {
        OmsOrder order = orderService.createOrder(request);
        return Result.success(order);
    }

    /**
     * 获取当前用户订单列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('app:order:list')")
    @Operation(summary = "获取当前用户订单列表", description = "按创建时间倒序分页返回当前登录用户的订单列表（包含商品明细）")
    public Result<Page<OrderVO>> list(
            @ModelAttribute PageParam pageParam,
            @Parameter(description = "订单状态，可选") @RequestParam(required = false) Integer status) {
        Page<OrderVO> page = orderService.listCurrent(pageParam, status);
        return Result.success(page);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('app:order:detail')")
    @Operation(summary = "获取订单详情", description = "根据订单ID获取订单基础信息及订单商品明细")
    public Result<OrderVO> detail(
            @Parameter(description = "订单ID") @PathVariable("id") Long id) {
        return Result.success(orderService.getDetail(id));
    }

    /**
     * 取消订单（简单将状态置为“已取消”）
     */
    @PostMapping("/cancel/{id}")
    @Operation(summary = "取消订单", description = "仅允许取消属于当前登录用户且仍在进行中的订单")
    public Result<String> cancel(
            @Parameter(description = "订单ID") @PathVariable("id") Long id) {

        // 1. 获取当前登录用户ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.failed("请先登录");
        }

        // 2. 查询订单，校验所有权和状态
        OmsOrder order = orderService.getById(id);
        if (order == null) {
            return Result.failed("订单不存在");
        }
        if (!order.getMemberId().equals(userId)) {
            return Result.failed("非法操作：无权取消他人订单");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode()) {
            return Result.failed("当前状态不允许取消（仅待付款订单可手动取消）");
        }

        // 3. 执行取消逻辑
        return orderService.updateStatus(
                Map.of("id", id, "status", OrderStatus.CANCELLED.getCode()))
                ? Result.success("取消成功")
                : Result.failed("取消失败");
    }

    /**
     * 确认收货（将状态置为“已完成”）
     */
    @PostMapping("/confirm/{id}")
    @PreAuthorize("hasAuthority('app:order:confirm')")
    @Operation(summary = "确认收货", description = "仅允许当前登录用户对自己的待取餐订单执行确认收货")
    public Result<String> confirm(
            @Parameter(description = "订单ID") @PathVariable("id") Long id) {

        // 1. 获取当前登录用户ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.failed("请先登录");
        }

        // 2. 查询订单，校验所有权和状态
        OmsOrder order = orderService.getById(id);
        if (order == null) {
            return Result.failed("订单不存在");
        }
        if (!order.getMemberId().equals(userId)) {
            return Result.failed("非法操作：无权操作他人订单");
        }
        if (order.getStatus() != OrderStatus.PENDING_PICKUP.getCode()) {
            return Result.failed("当前状态不允许确认收货（仅待取餐订单可确认收货）");
        }

        // 3. 执行状态更新为已完成
        return orderService.updateStatus(
                Map.of("id", id, "status", OrderStatus.COMPLETED.getCode()))
                ? Result.success("确认收货成功")
                : Result.failed("确认收货失败");
    }
}



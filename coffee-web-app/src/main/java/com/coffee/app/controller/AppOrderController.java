package com.coffee.app.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
     * 取消订单（简单将状态置为“已关闭”）
     */
    @PostMapping("/cancel/{id}")
    @PreAuthorize("hasAuthority('app:order:cancel')")
    @Operation(summary = "取消订单", description = "仅允许取消属于当前登录用户且仍在进行中的订单")
    public Result<String> cancel(
            @Parameter(description = "订单ID") @PathVariable("id") Long id) {
        // 这里为了简单直接复用 updateStatus 逻辑，也可以单独在 Service 中封装 cancelOrder 方法
        return orderService.updateStatus(
                java.util.Map.of("id", id, "status", com.coffee.common.dict.OrderStatus.CLOSED.getCode()))
                ? Result.success("取消成功")
                : Result.failed("取消失败");
    }
}



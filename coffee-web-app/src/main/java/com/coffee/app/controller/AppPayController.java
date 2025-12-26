package com.coffee.app.controller;

import com.coffee.common.result.Result;
import com.coffee.system.service.AliPayService;
import com.coffee.system.service.OrderService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "C端-支付接口")
@RestController
@RequestMapping("/app/pay")
@RequiredArgsConstructor
@Slf4j
public class AppPayController {

    private final AliPayService aliPayService;
    private final OrderService orderService;

    @Operation(summary = "支付宝支付", description = "获取支付宝 App 支付串")
    @PostMapping("/alipay")
    public Result<String> alipay(@RequestParam Long orderId) {
        // 返回 orderStr 给前端
        String orderStr = aliPayService.payByAlipay(orderId);
        return Result.success(orderStr);
    }

    @Operation(summary = "支付宝异步回调", description = "接收支付宝服务端的支付结果通知")
    @PostMapping("/notify")
    public String alipayNotify(HttpServletRequest request) {
        // 1. 解析请求参数
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();

        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        log.info("收到支付宝回调: {}", params);

        // 2. 交给 Service 处理验签和业务
        return aliPayService.handleAlipayNotify(params);
    }

    @Operation(summary = "咖啡卡支付", description = "使用咖啡卡余额支付订单")
    @PostMapping("/coffee-card")
    public Result<String> payByCoffeeCard(@RequestParam Long orderId) {
        boolean success = orderService.payByCoffeeCard(orderId);
        return success ? Result.success("支付成功") : Result.failed("支付失败");
    }
}
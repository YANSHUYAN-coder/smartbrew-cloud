package com.coffee.app.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.common.dto.PageParam;
import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.GiftCard;
import com.coffee.system.domain.entity.GiftCardTxn;
import com.coffee.system.service.GiftCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * C 端咖啡卡接口
 * 对应小程序首页"咖啡卡"入口：列表 + 简单购卡
 */
@RestController
@RequestMapping("/app/giftCard")
@Tag(name = "C端-咖啡卡接口", description = "提供咖啡卡列表查询与简单购卡能力")
@RequiredArgsConstructor
public class AppGiftCardController {

    private final GiftCardService giftCardService;

    /**
     * 获取当前用户咖啡卡列表
     */
    @GetMapping("/list")
    // @PreAuthorize("hasAuthority('app:gift:list')")
    @Operation(summary = "获取当前用户咖啡卡列表", description = "按创建时间倒序返回当前登录用户持有的咖啡卡")
    public Result<Page<GiftCard>> list(@ModelAttribute PageParam pageParam) {
        Page<GiftCard> page = giftCardService.listCurrent(pageParam);
        return Result.success(page);
    }

    /**
     * 创建咖啡卡订单（用于支付）
     * 先创建订单，支付成功后再创建咖啡卡
     *
     * body 示例：
     * {
     *   "amount": 100,
     *   "name": "咖啡会员卡",
     *   "greeting": "送你一杯咖啡",
     *   "validDays": 365
     * }
     */
    @PostMapping("/createOrder")
    // @PreAuthorize("hasAuthority('app:gift:create')")
    @Operation(summary = "创建咖啡卡订单", description = "创建咖啡卡订单，返回订单ID用于支付，支付成功后会自动创建并激活咖啡卡")
    public Result<com.coffee.system.domain.entity.OmsOrder> createOrder(@RequestBody Map<String, Object> body) {
        Object amountObj = body.get("amount");
        if (amountObj == null) {
            return Result.failed("金额不能为空");
        }
        BigDecimal amount = new BigDecimal(amountObj.toString());
        String name = body.getOrDefault("name", "咖啡会员卡").toString();
        String greeting = body.getOrDefault("greeting", "").toString();
        Integer validDays = null;
        Object validDaysObj = body.get("validDays");
        if (validDaysObj != null) {
            validDays = Integer.valueOf(validDaysObj.toString());
        }

        com.coffee.system.domain.entity.OmsOrder order = giftCardService.createGiftCardOrder(amount, name, greeting, validDays);
        return Result.success(order);
    }

    /**
     * 查询某张咖啡卡的交易明细（使用记录）
     * 用于前端展示"使用记录"，让用户知道余额是如何变化的
     */
    @GetMapping("/{cardId}/transactions")
    // @PreAuthorize("hasAuthority('app:gift:detail')")
    @Operation(summary = "查询咖啡卡交易明细", description = "分页返回某张咖啡卡的所有交易记录（发卡、消费、退款等），用于前端展示使用记录")
    public Result<Page<GiftCardTxn>> getTransactions(
            @Parameter(description = "咖啡卡ID") @PathVariable("cardId") Long cardId,
            @ModelAttribute PageParam pageParam) {
        Page<GiftCardTxn> page = giftCardService.getTransactions(cardId, pageParam);
        return Result.success(page);
    }
}



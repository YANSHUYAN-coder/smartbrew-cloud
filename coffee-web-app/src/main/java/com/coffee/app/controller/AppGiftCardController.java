package com.coffee.app.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.common.dto.PageParam;
import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.GiftCard;
import com.coffee.system.service.GiftCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * C 端礼品卡接口
 * 对应小程序首页“礼品卡”入口：列表 + 简单购卡
 */
@RestController
@RequestMapping("/app/giftCard")
@Tag(name = "C端-礼品卡接口", description = "提供礼品卡列表查询与简单购卡能力")
@RequiredArgsConstructor
public class AppGiftCardController {

    private final GiftCardService giftCardService;

    /**
     * 获取当前用户礼品卡列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('app:gift:list')")
    @Operation(summary = "获取当前用户礼品卡列表", description = "按创建时间倒序返回当前登录用户持有的礼品卡")
    public Result<Page<GiftCard>> list(@ModelAttribute PageParam pageParam) {
        Page<GiftCard> page = giftCardService.listCurrent(pageParam);
        return Result.success(page);
    }

    /**
     * 为当前用户创建一张礼品卡（简化版购卡）
     *
     * body 示例：
     * {
     *   "amount": 100,
     *   "name": "咖啡礼品卡",
     *   "greeting": "送你一杯咖啡",
     *   "validDays": 365
     * }
     */
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('app:gift:create')")
    @Operation(summary = "创建礼品卡（购卡）", description = "根据金额等信息为当前登录用户创建一张礼品卡，后续可接入支付流程")
    public Result<GiftCard> create(@RequestBody Map<String, Object> body) {
        Object amountObj = body.get("amount");
        if (amountObj == null) {
            return Result.failed("金额不能为空");
        }
        BigDecimal amount = new BigDecimal(amountObj.toString());
        String name = body.getOrDefault("name", "咖啡礼品卡").toString();
        String greeting = body.getOrDefault("greeting", "").toString();
        Integer validDays = null;
        Object validDaysObj = body.get("validDays");
        if (validDaysObj != null) {
            validDays = Integer.valueOf(validDaysObj.toString());
        }

        GiftCard card = giftCardService.createForCurrent(amount, name, greeting, validDays);
        return Result.success(card);
    }
}



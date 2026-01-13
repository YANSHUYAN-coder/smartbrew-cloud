package com.coffee.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.SmsCoupon;
import com.coffee.system.service.SmsCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "优惠券管理")
@RestController
@RequestMapping("/admin/coupon")
@RequiredArgsConstructor
public class AdminSmsCouponController {

    private final SmsCouponService smsCouponService;

    @Operation(summary = "获取优惠券列表")
    @GetMapping("/list")
    public Result<Page<SmsCoupon>> list(@RequestParam(name = "pageNum",defaultValue = "1") Integer pageNum,
                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                        @RequestParam(name = "name", required = false) String name,
                                        @RequestParam(name = "type", required = false) Integer type) {
        Page<SmsCoupon> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SmsCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), SmsCoupon::getName, name);
        wrapper.eq(type != null, SmsCoupon::getType, type);
        wrapper.orderByDesc(SmsCoupon::getCreateTime);
        return Result.success(smsCouponService.page(page, wrapper));
    }

    @Operation(summary = "获取优惠券详情")
    @GetMapping("/{id}")
    public Result<SmsCoupon> getItem(@PathVariable Long id) {
        return Result.success(smsCouponService.getById(id));
    }

    @Operation(summary = "添加优惠券")
    @PostMapping("/create")
    public Result<Boolean> create(@RequestBody SmsCoupon coupon) {
        coupon.setUseCount(0);
        coupon.setReceiveCount(0);
        coupon.setPublishCount(coupon.getCount());
        return Result.success(smsCouponService.save(coupon));
    }

    @Operation(summary = "修改优惠券")
    @PostMapping("/update/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody SmsCoupon coupon) {
        coupon.setId(id);
        return Result.success(smsCouponService.updateById(coupon));
    }

    @Operation(summary = "删除优惠券")
    @PostMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(smsCouponService.removeById(id));
    }
}
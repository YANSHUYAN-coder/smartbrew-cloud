package com.coffee.app.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.common.dto.PageParam;
import com.coffee.common.result.Result;
import com.coffee.system.domain.vo.CouponHistoryVO;
import com.coffee.system.domain.vo.RedeemableCouponVO;
import com.coffee.system.service.SmsCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/coupon")
public class AppCouponController {

    @Autowired
    private SmsCouponService couponService;


    /**
     * 获取积分商城可兑换的优惠券列表（包含用户是否已领取信息）
     */
    @GetMapping("/redeem/list")
    public Result<Page<RedeemableCouponVO>> listRedeemableCoupons(PageParam pageParam) {
        return Result.success(couponService.listRedeemableCoupons(pageParam));
    }

    /**
     * 积分兑换优惠券
     */
    @PostMapping("/redeem/{id}")
    public Result<Long> redeemCoupon(@PathVariable Long id) {
        return Result.success(couponService.redeemCoupon(id));
    }
    
    
    /**
     * 获取我的优惠券（包含详情）
     */
    @GetMapping("/my/list")
    public Result<List<CouponHistoryVO>> listMyCoupons(@RequestParam(required = false) Integer useStatus) {
        return Result.success(couponService.listMemberCouponsWithDetail(useStatus));
    }
}


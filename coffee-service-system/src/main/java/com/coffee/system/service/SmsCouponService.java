package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.SmsCoupon;
import com.coffee.system.domain.entity.SmsCouponHistory;
import com.coffee.system.domain.vo.CouponHistoryVO;
import com.coffee.system.domain.vo.RedeemableCouponVO;

import java.util.List;

public interface SmsCouponService extends IService<SmsCoupon> {
    /**
     * 获取可兑换的优惠券列表（包含用户是否已领取信息）
     */
    Page<RedeemableCouponVO> listRedeemableCoupons(PageParam pageParam);
    
    /**
     * 获取可兑换的优惠券列表（旧方法，保留兼容）
     */
    @Deprecated
    Page<SmsCoupon> listRedeemableCouponsOld(PageParam pageParam);

    /**
     * 积分兑换优惠券
     * @param couponId 优惠券ID
     * @return 兑换历史记录ID
     */
    Long redeemCoupon(Long couponId);
    
    /**
     * 获取当前用户可用的优惠券（包含详情）
     */
    List<CouponHistoryVO> listMemberCouponsWithDetail(Integer useStatus);
    
    /**
     * 获取当前用户可用的优惠券（旧方法，保留兼容）
     */
    List<SmsCouponHistory> listMemberCoupons(Integer useStatus);

    /**
     * 释放/退回优惠券
     * @param couponHistoryId 优惠券领取记录ID
     */
    void releaseCoupon(Long couponHistoryId);
}

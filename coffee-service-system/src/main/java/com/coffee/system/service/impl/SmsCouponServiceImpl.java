package com.coffee.system.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.context.UserContext;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.SmsCoupon;
import com.coffee.system.domain.entity.SmsCouponHistory;
import com.coffee.system.domain.entity.UmsMember;
import com.coffee.system.domain.vo.CouponHistoryVO;
import com.coffee.system.domain.vo.RedeemableCouponVO;
import com.coffee.system.mapper.SmsCouponHistoryMapper;
import com.coffee.system.mapper.SmsCouponMapper;
import com.coffee.system.mapper.UmsMemberMapper;
import com.coffee.system.service.SmsCouponService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SmsCouponServiceImpl extends ServiceImpl<SmsCouponMapper, SmsCoupon> implements SmsCouponService {

    @Autowired
    private UmsMemberMapper memberMapper;

    @Autowired
    private SmsCouponHistoryMapper couponHistoryMapper;

    @Override
    public Page<RedeemableCouponVO> listRedeemableCoupons(PageParam pageParam) {
        Long userId = UserContext.getUserId();
        
        // 查询未过期、积分大于0（表示可兑换）的优惠券
        LocalDateTime now = LocalDateTime.now();
        Page<SmsCoupon> page = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<SmsCoupon> wrapper = new LambdaQueryWrapper<SmsCoupon>()
                .gt(SmsCoupon::getEndTime, now)
                .le(SmsCoupon::getStartTime, now)
                .gt(SmsCoupon::getPoints, 0) // 必须设置了积分兑换值
                .orderByAsc(SmsCoupon::getPoints);
        Page<SmsCoupon> couponPage = this.page(page, wrapper);
        
        // 转换为 VO 并填充用户是否已领取信息
        Page<RedeemableCouponVO> voPage = new Page<>(couponPage.getCurrent(), couponPage.getSize(), couponPage.getTotal());
        List<RedeemableCouponVO> voList = couponPage.getRecords().stream().map(coupon -> {
            RedeemableCouponVO vo = new RedeemableCouponVO();
            BeanUtils.copyProperties(coupon, vo);
            
            // 检查用户是否已领取（如果用户已登录）
            if (userId != null) {
                Long redeemedCount = couponHistoryMapper.selectCount(new LambdaQueryWrapper<SmsCouponHistory>()
                        .eq(SmsCouponHistory::getMemberId, userId)
                        .eq(SmsCouponHistory::getCouponId, coupon.getId()));
                vo.setRedeemedCount(redeemedCount.intValue());
                
                // 判断是否已领取（达到限领数量）
                if (coupon.getPerLimit() != null && coupon.getPerLimit() > 0) {
                    boolean isRedeemed = redeemedCount >= coupon.getPerLimit();
                    vo.setIsRedeemed(isRedeemed);
                    log.debug("优惠券ID: {}, 用户ID: {}, 已领取数量: {}, 限领数量: {}, 是否已领取: {}", 
                        coupon.getId(), userId, redeemedCount, coupon.getPerLimit(), isRedeemed);
                } else {
                    // 如果没有限领，则只要领取过就认为已领取
                    boolean isRedeemed = redeemedCount > 0;
                    vo.setIsRedeemed(isRedeemed);
                    log.debug("优惠券ID: {}, 用户ID: {}, 已领取数量: {}, 无限领限制, 是否已领取: {}", 
                        coupon.getId(), userId, redeemedCount, isRedeemed);
                }
            } else {
                vo.setRedeemedCount(0);
                vo.setIsRedeemed(false);
                log.debug("用户未登录，优惠券ID: {}, isRedeemed设为false", coupon.getId());
            }
            
            return vo;
        }).collect(java.util.stream.Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    @Deprecated
    public Page<SmsCoupon> listRedeemableCouponsOld(PageParam pageParam) {
        // 查询未过期、积分大于0（表示可兑换）的优惠券
        LocalDateTime now = LocalDateTime.now();
        Page<SmsCoupon> page = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<SmsCoupon> wrapper = new LambdaQueryWrapper<SmsCoupon>()
                .gt(SmsCoupon::getEndTime, now)
                .le(SmsCoupon::getStartTime, now)
                .gt(SmsCoupon::getPoints, 0) // 必须设置了积分兑换值
                .orderByAsc(SmsCoupon::getPoints);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long redeemCoupon(Long couponId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        // 1. 获取优惠券信息
        SmsCoupon coupon = this.getById(couponId);
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }
        if (coupon.getCount() <= 0) {
            throw new RuntimeException("优惠券已领完");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new RuntimeException("优惠券不在领取时间内");
        }

        // 2. 检查用户积分
        UmsMember member = memberMapper.selectById(userId);
        if (member == null) {
            throw new RuntimeException("用户不存在");
        }
        if (member.getIntegration() == null || member.getIntegration() < coupon.getPoints()) {
            throw new RuntimeException("积分不足");
        }
        
        // 3. 检查限领数量
        if (coupon.getPerLimit() > 0) {
            Long count = couponHistoryMapper.selectCount(new LambdaQueryWrapper<SmsCouponHistory>()
                    .eq(SmsCouponHistory::getMemberId, userId)
                    .eq(SmsCouponHistory::getCouponId, couponId));
            if (count >= coupon.getPerLimit()) {
                throw new RuntimeException("每人限领" + coupon.getPerLimit() + "张");
            }
        }

        // 4. 扣除积分
        int updatedPoints = member.getIntegration() - coupon.getPoints();
        memberMapper.update(null, new LambdaUpdateWrapper<UmsMember>()
                .eq(UmsMember::getId, userId)
                .set(UmsMember::getIntegration, updatedPoints));

        // 5. 生成领取记录
        SmsCouponHistory history = new SmsCouponHistory();
        history.setCouponId(couponId);
        history.setMemberId(userId);
        history.setCouponCode(generateCouponCode(member.getId()));
        history.setMemberNickname(member.getNickname());
        history.setGetType(1); // 1->主动获取(积分兑换)
        history.setCreateTime(now);
        history.setUseStatus(0); // 0->未使用
        
        // 计算个人过期时间
        // 如果优惠券设置了 valid_days，则从领取时间开始计算
        // 否则使用优惠券模板的 end_time
        if (coupon.getValidDays() != null && coupon.getValidDays() > 0) {
            // 从领取时间 + 有效期天数
            LocalDateTime expireTime = now.plusDays(coupon.getValidDays());
            history.setExpireTime(expireTime);
            log.debug("优惠券ID: {}, 有效期天数: {}, 个人过期时间: {}", couponId, coupon.getValidDays(), expireTime);
        } else {
            // 使用优惠券模板的结束时间
            history.setExpireTime(coupon.getEndTime());
            log.debug("优惠券ID: {}, 使用模板结束时间: {}", couponId, coupon.getEndTime());
        }
        
        couponHistoryMapper.insert(history);

        // 6. 更新优惠券库存和领取数量
        this.update(new LambdaUpdateWrapper<SmsCoupon>()
                .eq(SmsCoupon::getId, couponId)
                .setSql("count = count - 1, receive_count = receive_count + 1"));

        return history.getId();
    }
    
    @Override
    public List<CouponHistoryVO> listMemberCouponsWithDetail(Integer useStatus) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        return couponHistoryMapper.selectMemberCouponsWithDetail(userId, useStatus);
    }
    
    @Override
    public List<SmsCouponHistory> listMemberCoupons(Integer useStatus) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        LambdaQueryWrapper<SmsCouponHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsCouponHistory::getMemberId, userId);
        if (useStatus != null) {
            wrapper.eq(SmsCouponHistory::getUseStatus, useStatus);
        }
        wrapper.orderByDesc(SmsCouponHistory::getCreateTime);
        return couponHistoryMapper.selectList(wrapper);
    }

    private String generateCouponCode(Long memberId) {
        return LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) 
                + String.format("%04d", memberId % 10000) 
                + IdUtil.simpleUUID().substring(0, 6);
    }
}

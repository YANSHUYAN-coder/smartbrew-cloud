package com.coffee.system.domain.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 可兑换优惠券 VO（包含用户是否已领取信息）
 */
@Data
public class RedeemableCouponVO implements Serializable {
    private Long id;
    private String name;
    private BigDecimal amount;
    private Integer points;
    private String note;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer count; // 库存数量
    
    /**
     * 用户是否已领取（根据限领数量判断）
     * true: 已领取（达到限领数量）
     * false: 未领取或未达到限领数量
     */
    private Boolean isRedeemed;
    
    /**
     * 用户已领取数量
     */
    private Integer redeemedCount;
    
    /**
     * 每人限领数量
     */
    private Integer perLimit;
}


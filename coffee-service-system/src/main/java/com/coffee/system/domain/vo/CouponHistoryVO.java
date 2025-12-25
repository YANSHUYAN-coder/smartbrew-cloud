package com.coffee.system.domain.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券 VO（包含优惠券详情）
 */
@Data
public class CouponHistoryVO implements Serializable {
    private Long id;
    private Long couponId;
    private Long memberId;
    private String couponCode;
    private String memberNickname;
    private Integer getType;
    private Integer useStatus;
    private LocalDateTime useTime;
    private Long orderId;
    private String orderSn;
    private LocalDateTime createTime;
    
    // 关联的优惠券信息
    private String couponName;
    private BigDecimal amount;
    private BigDecimal minPoint;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String note;
    
    /**
     * 个人过期时间（优先使用，如果没有则使用endTime）
     */
    private LocalDateTime expireTime;
}


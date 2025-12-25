package com.coffee.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 优惠券领取记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sms_coupon_history")
public class SmsCouponHistory extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券id
     */
    @TableField("coupon_id")
    private Long couponId;

    /**
     * 会员id
     */
    @TableField("member_id")
    private Long memberId;

    /**
     * 优惠码
     */
    @TableField("coupon_code")
    private String couponCode;

    /**
     * 领取人昵称
     */
    @TableField("member_nickname")
    private String memberNickname;

    /**
     * 获取类型：0->后台赠送；1->主动获取(积分兑换)
     */
    @TableField("get_type")
    private Integer getType;

    /**
     * 使用状态：0->未使用；1->已使用；2->已过期
     */
    @TableField("use_status")
    private Integer useStatus;

    /**
     * 使用时间
     */
    @TableField("use_time")
    private LocalDateTime useTime;

    /**
     * 订单id
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 订单号码
     */
    @TableField("order_sn")
    private String orderSn;
    
    /**
     * 个人过期时间（从领取时间+有效期天数计算）
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;
}


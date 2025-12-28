package com.coffee.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sms_coupon")
public class SmsCoupon extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券类型；0->全场赠券；1->会员赠券；2->购物赠券；3->注册赠券
     */
    @TableField("type")
    private Integer type;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 使用平台：0->全部；1->移动；2->PC
     */
    @TableField("platform")
    private Integer platform;

    /**
     * 数量
     */
    @TableField("count")
    private Integer count;

    /**
     * 金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 每人限领张数
     */
    @TableField("per_limit")
    private Integer perLimit;

    /**
     * 使用门槛；0表示无门槛
     */
    @TableField("min_point")
    private BigDecimal minPoint;

    /**
     * 开始时间
     */
    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 使用类型：0->全场通用；1->指定分类；2->指定商品
     */
    @TableField("use_type")
    private Integer useType;

    /**
     * 备注
     */
    @TableField("note")
    private String note;

    /**
     * 发行数量
     */
    @TableField("publish_count")
    private Integer publishCount;

    /**
     * 已使用数量
     */
    @TableField("use_count")
    private Integer useCount;

    /**
     * 领取数量
     */
    @TableField("receive_count")
    private Integer receiveCount;

    /**
     * 可以领取的日期
     */
    @TableField("enable_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime enableTime;

    /**
     * 优惠码
     */
    @TableField("code")
    private String code;

    /**
     * 可领取的会员等级：0->无限制
     */
    @TableField("member_level")
    private Integer memberLevel;

    /**
     * 兑换所需积分
     */
    @TableField("points")
    private Integer points;
    
    /**
     * 有效期天数（从领取时开始计算，如果为NULL则使用end_time）
     */
    @TableField("valid_days")
    private Integer validDays;
}


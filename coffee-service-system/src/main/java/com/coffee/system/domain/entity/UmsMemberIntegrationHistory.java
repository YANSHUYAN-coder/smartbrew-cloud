package com.coffee.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户积分明细表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ums_member_integration_history")
public class UmsMemberIntegrationHistory extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableField("member_id")
    private Long memberId;

    /**
     * 变动类型：1->签到获得；2->兑换优惠券扣除；3->订单完成获得；4->其他
     */
    @TableField("change_type")
    private Integer changeType;

    /**
     * 变动积分（正数表示增加，负数表示减少）
     */
    @TableField("change_points")
    private Integer changePoints;

    /**
     * 来源类型（如：sign_in、coupon_redeem、order_complete）
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 来源ID（如：签到记录ID、优惠券ID、订单ID）
     */
    @TableField("source_id")
    private Long sourceId;

    /**
     * 备注说明
     */
    @TableField("note")
    private String note;
}


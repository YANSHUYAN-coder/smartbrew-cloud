package com.coffee.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 礼品卡主表实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gift_card")
public class GiftCard extends BaseEntity {

    /**
     * 礼品卡卡号
     */
    @TableField("card_no")
    private String cardNo;

    /**
     * 当前持卡人用户ID
     */
    @TableField("member_id")
    private Long memberId;

    /**
     * 礼品卡名称
     */
    @TableField("name")
    private String name;

    /**
     * 初始面值金额
     */
    @TableField("original_amount")
    private BigDecimal originalAmount;

    /**
     * 当前可用余额
     */
    @TableField("balance")
    private BigDecimal balance;

    /**
     * 状态：0->未激活；1->可用；2->已用完；3->已过期
     */
    @TableField("status")
    private Integer status;

    /**
     * 到期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 祝福语/备注
     */
    @TableField("greeting")
    private String greeting;
}



package com.coffee.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 礼品卡收支流水实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("gift_card_txn")
public class GiftCardTxn extends BaseEntity {

    /**
     * 礼品卡ID
     */
    @TableField("card_id")
    private Long cardId;

    /**
     * 关联用户ID
     */
    @TableField("member_id")
    private Long memberId;

    /**
     * 类型：0->充值/发卡；1->消费；2->退款；3->调整
     */
    @TableField("type")
    private Integer type;

    /**
     * 变动金额（正数表示增加，负数表示扣减）
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 关联订单ID（消费时）
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}



package com.coffee.system.domain.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分明细 VO
 */
@Data
public class IntegrationHistoryVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 变动类型：1->签到获得；2->兑换优惠券扣除；3->订单完成获得；4->其他
     */
    private Integer changeType;
    
    /**
     * 变动类型描述
     */
    private String changeTypeDesc;
    
    /**
     * 变动积分（正数表示增加，负数表示减少）
     */
    private Integer changePoints;
    
    /**
     * 来源类型（如：sign_in、coupon_redeem、order_complete）
     */
    private String sourceType;
    
    /**
     * 来源ID（如：签到记录ID、优惠券ID、订单ID）
     */
    private Long sourceId;
    
    /**
     * 备注说明
     */
    private String note;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}


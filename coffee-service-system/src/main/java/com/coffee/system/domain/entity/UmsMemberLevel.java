package com.coffee.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 会员等级实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ums_member_level")
public class UmsMemberLevel extends BaseEntity {
    /**
     * 等级名称（如：普通会员、银卡会员、金卡会员、钻石会员）
     */
    private String name;
    
    /**
     * 成长值门槛（达到此成长值可升级到此等级）
     */
    private Integer growthPoint;
    
    /**
     * 折扣率（0.00-1.00，1.00表示无折扣）
     */
    private BigDecimal discount;
    
    /**
     * 积分倍率（积分获取倍率，1.00表示正常倍率）
     */
    private BigDecimal integrationRate;
    
    /**
     * 等级图标
     */
    private String icon;
    
    /**
     * 等级描述
     */
    private String description;
    
    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;
}


package com.coffee.system.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 签到结果 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignInResultVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 奖励积分
     */
    private Integer points;
    
    /**
     * 连续签到天数
     */
    private Integer continuousDays;
    
    /**
     * 是否首次签到
     */
    private Boolean isFirstTime;
}


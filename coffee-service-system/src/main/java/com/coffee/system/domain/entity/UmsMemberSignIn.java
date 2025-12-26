package com.coffee.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户签到记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ums_member_sign_in")
public class UmsMemberSignIn extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("member_id")
    private Long memberId;

    /**
     * 签到日期（YYYY-MM-DD）
     */
    @TableField("sign_date")
    private LocalDate signDate;

    /**
     * 签到奖励积分
     */
    @TableField("points")
    private Integer points;

    /**
     * 连续签到天数
     */
    @TableField("continuous_days")
    private Integer continuousDays;

    /**
     * 签到时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}


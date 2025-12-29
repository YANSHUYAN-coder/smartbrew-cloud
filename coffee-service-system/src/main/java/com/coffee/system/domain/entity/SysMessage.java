package com.coffee.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 系统消息表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_message")
public class SysMessage extends BaseEntity{


    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：0-系统通知, 1-订单通知, 2-资产通知
     */
    private Integer type;

    /**
     * 关联业务ID (如订单ID)
     */
    private Long bizId;

    /**
     * 是否已读：0-未读, 1-已读
     */
    private Integer isRead;
}
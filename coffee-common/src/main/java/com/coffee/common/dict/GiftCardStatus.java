package com.coffee.common.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 礼品卡状态枚举
 */
@Getter
@AllArgsConstructor
public enum GiftCardStatus {

    /**
     * 0 -> 未激活
     */
    INACTIVE(0, "未激活"),

    /**
     * 1 -> 可用
     */
    ACTIVE(1, "可用"),

    /**
     * 2 -> 已用完
     */
    USED_UP(2, "已用完"),

    /**
     * 3 -> 已过期
     */
    EXPIRED(3, "已过期");

    private final Integer code;
    private final String desc;

    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "";
        }
        for (GiftCardStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return "";
    }
}



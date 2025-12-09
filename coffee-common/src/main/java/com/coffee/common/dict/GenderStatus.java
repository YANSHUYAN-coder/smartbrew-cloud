package com.coffee.common.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GenderStatus {

    /**
     * 0 -> 保密
     */
    PRIVATE(0, "保密"),

    /**
     * 1 -> 男
     */
    MALE(1, "男"),


    /**
     * 2 -> 女
     */
    FEMALe(2, "女");

    private final Integer code;
    private final String desc;
}

package com.coffee.common.dto;

import lombok.Data;

@Data
public class MemberStatusDTO {
    private Long id;
    private Integer status; // 1-正常 0-禁用
}
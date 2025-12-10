package com.coffee.common.dto;

import lombok.Data;

/**
 * 修改密码请求参数
 */
@Data
public class UpdatePasswordDTO {
    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
}
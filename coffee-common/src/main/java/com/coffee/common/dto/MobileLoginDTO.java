package com.coffee.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 手机号登录/注册参数
 */
@Data
@Schema(description = "手机号登录参数")
public class MobileLoginDTO {
    
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    private String mobile;
    
    @Schema(description = "短信验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String code;
}
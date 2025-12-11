package com.coffee.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分页参数")
public class PageParam {
    
    @Schema(description = "页码", defaultValue = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", defaultValue = "10")
    private Integer pageSize = 10;
}


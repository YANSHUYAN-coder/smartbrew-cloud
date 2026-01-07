package com.coffee.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "滚动分页参数")
public class CursorPageParam {

    @Schema(description = "游标ID（上一页最后一条的ID，第一页传null）")
    private Long lastId;

    @Schema(description = "每页数量")
    private Integer pageSize = 10;
}


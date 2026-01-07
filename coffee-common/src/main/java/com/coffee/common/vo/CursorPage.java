package com.coffee.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "滚动分页结果")
public class CursorPage<T> {

    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "下一页游标ID（如果没有更多数据，返回null）")
    private Long nextCursor;
    
    @Schema(description = "是否有更多数据")
    private Boolean hasMore;
}


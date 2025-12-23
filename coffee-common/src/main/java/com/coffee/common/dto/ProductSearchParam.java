package com.coffee.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品搜索参数
 * 用于 C 端和管理端的商品搜索功能
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品搜索参数")
public class ProductSearchParam extends PageParam {
    
    @Schema(description = "搜索关键词（商品名称或描述）")
    private String keyword;
    
    @Schema(description = "商品状态：1-上架, 0-下架（管理端使用，C端不传此参数）")
    private Integer status;
    
    @Schema(description = "分类ID（可选）")
    private Long categoryId;
}


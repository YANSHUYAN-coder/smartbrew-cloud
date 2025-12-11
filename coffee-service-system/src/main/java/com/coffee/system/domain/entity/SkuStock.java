package com.coffee.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品 SKU 库存信息
 * 例如：拿铁-大杯-热-全糖
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_sku_stock")
public class SkuStock extends BaseEntity {
    
    private Long productId;
    
    /**
     * SKU编码 (例如: 2024001)
     */
    private String skuCode;
    
    /**
     * 价格
     */
    private BigDecimal price;
    
    /**
     * 库存
     */
    private Integer stock;
    
    /**
     * 预警库存
     */
    private Integer lowStock;
    
    /**
     * 规格属性 (JSON格式)
     * 例如: [{"key":"容量","value":"大杯"},{"key":"温度","value":"热"}]
     */
    private String spec;
}
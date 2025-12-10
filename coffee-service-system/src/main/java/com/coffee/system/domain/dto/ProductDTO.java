package com.coffee.system.domain.dto;

import com.coffee.system.domain.Product;
import com.coffee.system.domain.SkuStock;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 商品数据传输对象
 * 包含：商品基本信息 + SKU 库存列表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductDTO extends Product {
    
    /**
     * 商品的 SKU 列表
     * 例如：
     * [
     * { "skuCode": "202401", "spec": "[{\"key\":\"杯型\",\"value\":\"大杯\"}]", "price": 22.00, "stock": 100 },
     * { "skuCode": "202402", "spec": "[{\"key\":\"杯型\",\"value\":\"中杯\"}]", "price": 19.00, "stock": 100 }
     * ]
     */
    private List<SkuStock> skuStockList;
}
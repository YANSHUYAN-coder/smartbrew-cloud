package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.system.domain.entity.SkuStock;

import java.util.List;

public interface SkuStockService extends IService<SkuStock> {
    /**
     * 根据商品ID查询所有 SKU
     */
    List<SkuStock> listByProductId(Long productId);

    /**
     * 释放订单占用的库存
     * @param orderId 订单ID
     */
    void releaseStock(Long orderId);
}
package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.system.domain.entity.SkuStock;

import java.util.List;

public interface SkuStockService extends IService<SkuStock> {
    /**
     * 根据商品ID查询所有 SKU
     */
    List<SkuStock> listByProductId(Long productId);
}
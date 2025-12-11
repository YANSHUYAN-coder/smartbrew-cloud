package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.system.domain.entity.SkuStock;
import com.coffee.system.mapper.SkuStockMapper;
import com.coffee.system.service.SkuStockService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkuStockServiceImpl extends ServiceImpl<SkuStockMapper, SkuStock> implements SkuStockService {

    @Override
    public List<SkuStock> listByProductId(Long productId) {
        return this.list(new LambdaQueryWrapper<SkuStock>()
                .eq(SkuStock::getProductId, productId));
    }
}
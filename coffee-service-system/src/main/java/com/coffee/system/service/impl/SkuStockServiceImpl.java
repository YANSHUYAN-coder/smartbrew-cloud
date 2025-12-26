package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.system.domain.entity.OmsOrderItem;
import com.coffee.system.domain.entity.SkuStock;
import com.coffee.system.mapper.OrderItemMapper;
import com.coffee.system.mapper.SkuStockMapper;
import com.coffee.system.service.SkuStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SkuStockServiceImpl extends ServiceImpl<SkuStockMapper, SkuStock> implements SkuStockService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public List<SkuStock> listByProductId(Long productId) {
        return this.list(new LambdaQueryWrapper<SkuStock>()
                .eq(SkuStock::getProductId, productId));
    }

    /**
     * 释放库存实现
     * 1. 查询订单包含的商品项
     * 2. 遍历商品项，将 quantity 加回 sku_stock 的 stock 字段
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseStock(Long orderId) {
        // 1. 查询订单关联的所有商品项
        List<OmsOrderItem> orderItemList = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId)
        );

        if (orderItemList == null || orderItemList.isEmpty()) {
            return;
        }

        // 2. 遍历恢复库存
        for (OmsOrderItem item : orderItemList) {
            SkuStock sku = this.getById(item.getProductSkuId());
            if (sku != null) {
                // 简单实现：直接增加库存
                // 进阶实现：如果是预扣减模式（lock_stock），则减少 lock_stock
                sku.setStock(sku.getStock() + item.getProductQuantity());
                this.updateById(sku);
            }
        }
    }
}
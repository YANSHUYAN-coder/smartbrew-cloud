package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.common.result.Result;
import com.coffee.common.vo.CartVO;
import com.coffee.system.domain.entity.OmsCartItem;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService extends IService<OmsCartItem> {
    
    /**
     * 获取当前用户的购物车列表
     */
    List<CartVO> listCurrent();
    
    /**
     * 添加商品到购物车
     */
    Result<String> addToCart(OmsCartItem cartItem);
    
    /**
     * 更新购物车项数量
     */
    Result<String> updateQuantity(Long cartItemId, Integer quantity);
    
    /**
     * 删除购物车项
     */
    Result<String> deleteCartItem(Long cartItemId);
    
    /**
     * 清空购物车
     */
    Result<String> clearCart();
}


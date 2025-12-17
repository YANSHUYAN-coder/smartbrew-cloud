package com.coffee.app.controller;

import com.coffee.common.result.Result;
import com.coffee.common.vo.CartVO;
import com.coffee.system.domain.entity.OmsCartItem;
import com.coffee.system.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * C端购物车接口
 */
@RestController
@RequestMapping("/app/cart")
public class AppCartController {

    @Autowired
    private CartService cartService;

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('app:cart:list')")
    public Result<List<CartVO>> list() {
        List<CartVO> cartList = cartService.listCurrent();
        return Result.success(cartList);
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('app:cart:add')")
    public Result<String> addToCart(@RequestBody OmsCartItem cartItem) {
        return cartService.addToCart(cartItem);
    }

    /**
     * 更新购物车项数量
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('app:cart:update')")
    public Result<String> updateQuantity(@RequestBody Map<String, Object> params) {
        Long cartItemId = Long.valueOf(params.get("cartItemId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        return cartService.updateQuantity(cartItemId, quantity);
    }

    /**
     * 删除购物车项
     */
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('app:cart:delete')")
    public Result<String> deleteCartItem(@PathVariable("id") Long id) {
        return cartService.deleteCartItem(id);
    }

    /**
     * 清空购物车
     */
    @PostMapping("/clear")
    @PreAuthorize("hasAuthority('app:cart:delete')")
    public Result<String> clearCart() {
        return cartService.clearCart();
    }
}


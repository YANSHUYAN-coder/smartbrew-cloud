package com.coffee.app.controller;

import com.coffee.common.result.Result;
import com.coffee.common.vo.CartVO;
import com.coffee.system.domain.entity.OmsCartItem;
import com.coffee.system.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "C端-购物车接口", description = "提供小程序/APP 端使用的购物车增删改查接口")
public class AppCartController {

    @Autowired
    private CartService cartService;

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('app:cart:list')")
    @Operation(summary = "获取当前用户购物车列表", description = "根据当前登录用户，返回购物车中的全部商品项")
    public Result<List<CartVO>> list() {
        List<CartVO> cartList = cartService.listCurrent();
        return Result.success(cartList);
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('app:cart:add')")
    @Operation(summary = "添加商品到购物车", description = "根据商品ID与SKU信息，将商品加入当前用户的购物车。如果已存在则累加数量")
    public Result<String> addToCart(
            @Parameter(description = "购物车项信息（商品ID、SKU、价格、数量等）")
            @RequestBody OmsCartItem cartItem) {
        return cartService.addToCart(cartItem);
    }

    /**
     * 更新购物车项数量
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('app:cart:update')")
    @Operation(summary = "更新购物车项数量", description = "根据购物车项ID与目标数量，更新购物车中该商品的数量；数量为0时可配合删除接口使用")
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
    @Operation(summary = "删除单个购物车项", description = "根据购物车项ID删除对应的商品记录")
    public Result<String> deleteCartItem(
            @Parameter(description = "购物车项ID") @PathVariable("id") Long id) {
        return cartService.deleteCartItem(id);
    }

    /**
     * 清空购物车
     */
    @PostMapping("/clear")
    @PreAuthorize("hasAuthority('app:cart:delete')")
    @Operation(summary = "清空购物车", description = "清空当前登录用户的全部购物车项")
    public Result<String> clearCart() {
        return cartService.clearCart();
    }
}


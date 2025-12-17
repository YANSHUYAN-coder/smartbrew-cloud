package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.context.UserContext;
import com.coffee.common.result.Result;
import com.coffee.common.vo.CartVO;
import com.coffee.system.domain.entity.OmsCartItem;
import com.coffee.system.mapper.OmsCartItemMapper;
import com.coffee.system.service.CartService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车服务实现
 */
@Service
public class CartServiceImpl extends ServiceImpl<OmsCartItemMapper, OmsCartItem> implements CartService {

    /**
     * 默认单次加入购物车的数量
     */
    private static final int DEFAULT_QUANTITY = 1;

    @Override
    public List<CartVO> listCurrent() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        List<OmsCartItem> cartItems = lambdaQuery()
                .eq(OmsCartItem::getMemberId, userId)
                .orderByDesc(OmsCartItem::getUpdateTime)
                .list();

        return cartItems.stream().map(item -> {
            CartVO vo = new CartVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addToCart(OmsCartItem cartItem) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.failed("用户未登录");
        }

        cartItem.setMemberId(userId);
        cartItem.setId(null); // 防止前端传脏ID

        // 检查是否已存在相同的商品和SKU
        LambdaQueryWrapper<OmsCartItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OmsCartItem::getMemberId, userId)
                .eq(OmsCartItem::getProductId, cartItem.getProductId());

        // 如果有SKU ID，需要匹配SKU
        if (cartItem.getProductSkuId() != null) {
            queryWrapper.eq(OmsCartItem::getProductSkuId, cartItem.getProductSkuId());
        } else {
            // 如果没有SKU ID，查询没有SKU ID的项
            queryWrapper.isNull(OmsCartItem::getProductSkuId);
        }

        OmsCartItem existItem = this.getOne(queryWrapper);

        if (existItem != null) {
            // 如果已存在，增加数量（前端传了数量就累加，否则按默认值 1 处理）
            int increment = cartItem.getQuantity() != null ? cartItem.getQuantity() : DEFAULT_QUANTITY;
            existItem.setQuantity(existItem.getQuantity() + increment);
            boolean flag = this.updateById(existItem);
            return flag ? Result.success("已更新购物车") : Result.failed("更新失败");
        } else {
            // 如果不存在，新增：未传数量或数量非法时使用默认值 1
            if (cartItem.getQuantity() == null || cartItem.getQuantity() <= 0) {
                cartItem.setQuantity(DEFAULT_QUANTITY);
            }
            boolean flag = this.save(cartItem);
            return flag ? Result.success("已加入购物车") : Result.failed("加入失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateQuantity(Long cartItemId, Integer quantity) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.failed("用户未登录");
        }

        OmsCartItem existItem = this.getById(cartItemId);
        if (existItem == null) {
            return Result.failed("购物车项不存在");
        }

        // 权限检查
        if (!existItem.getMemberId().equals(userId)) {
            return Result.failed("非法操作");
        }

        if (quantity <= 0) {
            // 数量为0，删除该项
            boolean flag = this.removeById(cartItemId);
            return flag ? Result.success("已删除") : Result.failed("删除失败");
        }

        existItem.setQuantity(quantity);
        boolean flag = this.updateById(existItem);
        return flag ? Result.success("更新成功") : Result.failed("更新失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteCartItem(Long cartItemId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.failed("用户未登录");
        }

        OmsCartItem existItem = this.getById(cartItemId);
        if (existItem == null) {
            return Result.success("删除成功"); // 不存在视为删除成功
        }

        // 权限检查
        if (!existItem.getMemberId().equals(userId)) {
            return Result.failed("非法操作");
        }

        boolean flag = this.removeById(cartItemId);
        return flag ? Result.success("删除成功") : Result.failed("删除失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> clearCart() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.failed("用户未登录");
        }

        boolean flag = this.remove(new LambdaQueryWrapper<OmsCartItem>()
                .eq(OmsCartItem::getMemberId, userId));
        return flag ? Result.success("清空成功") : Result.failed("清空失败");
    }
}


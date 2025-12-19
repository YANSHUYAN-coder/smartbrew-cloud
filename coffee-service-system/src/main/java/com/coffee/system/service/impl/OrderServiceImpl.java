package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.context.UserContext;
import com.coffee.common.dto.CreateOrderRequest;
import com.coffee.common.dto.PageParam;
import com.coffee.common.dict.OrderStatus;
import com.coffee.system.domain.entity.OmsOrder;
import com.coffee.system.domain.entity.OmsOrderItem;
import com.coffee.system.domain.vo.OrderVO;
import com.coffee.system.mapper.OmsOrderMapper;
import com.coffee.system.mapper.OrderItemMapper;
import com.coffee.system.service.CartService;
import com.coffee.system.service.OrderService;
import com.coffee.system.service.UmsMemberReceiveAddressService;
import com.coffee.system.domain.entity.OmsCartItem;
import com.coffee.system.domain.entity.UmsMemberReceiveAddress;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements OrderService {
    @Autowired
    private OmsOrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private CartService cartService;
    @Autowired
    private UmsMemberReceiveAddressService addressService;

    @Override
    public Page<OmsOrder> getAllList(PageParam pageParam, Integer status) {
        Page<OmsOrder> orderPage = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(OmsOrder::getStatus, status);
        }
        wrapper.orderByDesc(OmsOrder::getCreateTime);
        return orderMapper.selectPage(orderPage, wrapper);
    }


    @Override
    public OrderVO getDetail(Long id) {
        OmsOrder omsOrder = this.getById(id);
        // TODO: 查询 OmsOrderItem
        List<OmsOrderItem> omsOrderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>()
                        .eq(OmsOrderItem::getOrderId, id));
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(omsOrder, orderVO);
        orderVO.setOrderItemList(omsOrderItems);
        return orderVO;
    }

    @Override
    public boolean updateStatus(Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        OmsOrder order = new OmsOrder();
        order.setId(id);
        order.setStatus(status);
        return this.update(new LambdaUpdateWrapper<OmsOrder>()
                .eq(OmsOrder::getId, id)
                .set(OmsOrder::getStatus, status));
    }

    @Override
    public Page<OrderVO> listCurrent(PageParam pageParam, Integer status) {
        Long userId = UserContext.getUserId();
        Page<OmsOrder> orderPage = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsOrder::getMemberId, userId);
        if (status != null) {
            wrapper.eq(OmsOrder::getStatus, status);
        }
        wrapper.orderByDesc(OmsOrder::getCreateTime);
        Page<OmsOrder> page = orderMapper.selectPage(orderPage, wrapper);

        // 转换为 OrderVO 并补充商品明细
        Page<OrderVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            List<OrderVO> voList = new java.util.ArrayList<>();
            for (OmsOrder order : page.getRecords()) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                
                // 查询订单商品明细
                List<OmsOrderItem> items = orderItemMapper.selectList(
                        new LambdaQueryWrapper<OmsOrderItem>()
                                .eq(OmsOrderItem::getOrderId, order.getId())
                );
                orderVO.setOrderItemList(items);
                
                voList.add(orderVO);
            }
            voPage.setRecords(voList);
        }

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OmsOrder createOrder(CreateOrderRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        // 1. 根据购物车项ID查询购物车数据
        if (request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
            throw new RuntimeException("购物车项不能为空");
        }
        List<OmsCartItem> cartItems = cartService.listByIds(request.getCartItemIds());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车项不存在");
        }
        // 校验购物车项是否属于当前用户
        for (OmsCartItem cartItem : cartItems) {
            if (!cartItem.getMemberId().equals(userId)) {
                throw new RuntimeException("非法操作：购物车项不属于当前用户");
            }
        }

        // 2. 根据地址ID查询收货地址
        if (request.getAddressId() == null) {
            throw new RuntimeException("收货地址不能为空");
        }
        UmsMemberReceiveAddress address = addressService.getById(request.getAddressId());
        if (address == null) {
            throw new RuntimeException("收货地址不存在");
        }
        // 校验地址是否属于当前用户
        if (!address.getMemberId().equals(userId)) {
            throw new RuntimeException("非法操作：收货地址不属于当前用户");
        }

        // 3. 计算订单金额（如果前端传了金额，可以用于校验，但后端以实际计算为准）
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OmsCartItem cartItem : cartItems) {
            BigDecimal itemTotal = cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }
        BigDecimal promotionAmount = defaultIfNull(request.getPromotionAmount());
        BigDecimal couponAmount = defaultIfNull(request.getCouponAmount());
        BigDecimal payAmount = totalAmount.subtract(promotionAmount).subtract(couponAmount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }

        // 4. 构建订单基础信息
        OmsOrder order = new OmsOrder();
        order.setMemberId(userId);
        order.setOrderSn(generateOrderSn(userId));
        order.setTotalAmount(totalAmount);
        order.setPromotionAmount(promotionAmount);
        order.setCouponAmount(couponAmount);
        order.setPayAmount(payAmount);
        order.setPayType(request.getPayType() == null ? 0 : request.getPayType());
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setDeliveryCompany(request.getDeliveryCompany() != null ? request.getDeliveryCompany() : "门店自提");

        // 5. 填充收货人信息（从地址对象拷贝）
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverDetailAddress(address.getDetailAddress());
        order.setReceiverPostCode(address.getPostCode());

        order.setNote(request.getRemark());

        LocalDateTime now = LocalDateTime.now();
        order.setCreateTime(now);
        order.setUpdateTime(now);

        // 6. 保存订单
        this.save(order);

        // 7. 保存订单明细（从购物车项转换）
        for (OmsCartItem cartItem : cartItems) {
            OmsOrderItem item = new OmsOrderItem();
            item.setOrderId(order.getId());
            item.setOrderSn(order.getOrderSn());
            item.setProductId(cartItem.getProductId());
            item.setProductPic(cartItem.getProductPic());
            item.setProductName(cartItem.getProductName());
            item.setProductPrice(cartItem.getPrice());
            item.setProductQuantity(cartItem.getQuantity());
            item.setProductSkuId(cartItem.getProductSkuId());
            item.setProductSkuCode(cartItem.getProductSkuCode());
            // 规格属性，存成字符串
            item.setProductAttr(cartItem.getProductSubTitle());
            item.setCreateTime(now);

            orderItemMapper.insert(item);
        }

        // 8. 删除已下单的购物车项
        cartService.removeByIds(request.getCartItemIds());

        return order;
    }

    /**
     * 生成订单编号：时间戳 + 用户ID + 随机串
     */
    private String generateOrderSn(Long userId) {
        String timePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String userPart = userId == null ? "0" : String.valueOf(userId);
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return timePart + userPart + randomPart;
    }

    private BigDecimal defaultIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}


package com.coffee.system.service.impl;

import com.alipay.api.AlipayClient;
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
import com.coffee.system.service.GiftCardService;
import com.coffee.system.service.OrderService;
import com.coffee.system.service.UmsMemberReceiveAddressService;
import com.coffee.system.domain.entity.GiftCard;
import com.coffee.common.dict.GiftCardStatus;
import com.coffee.system.domain.entity.OmsCartItem;
import com.coffee.system.domain.entity.UmsMemberReceiveAddress;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements OrderService {
    @Autowired
    private StringRedisTemplate redisTemplate; // 注入 Redis
    @Autowired
    private OmsOrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private CartService cartService;
    @Autowired
    private UmsMemberReceiveAddressService addressService;
    @Autowired
    private GiftCardService giftCardService;

    @Autowired
    private AlipayClient alipayClient;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    /**
     * 获取所有订单列表（包含商品明细）
     * @param pageParam
     * @param status
     * @return
     */
    @Override
    public Page<OrderVO> getAllList(PageParam pageParam, Integer status) {
        Page<OmsOrder> orderPage = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(OmsOrder::getStatus, status);
        }
        wrapper.orderByDesc(OmsOrder::getCreateTime);
        Page<OmsOrder> page = orderMapper.selectPage(orderPage, wrapper);

        // 转换为 OrderVO 并补充商品明细
        Page<OrderVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            List<OrderVO> voList = new ArrayList<>();
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


    /**
     * 获取订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO getDetail(Long id) {
        OmsOrder omsOrder = this.getById(id);
        List<OmsOrderItem> omsOrderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>()
                        .eq(OmsOrderItem::getOrderId, id));
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(omsOrder, orderVO);
        orderVO.setOrderItemList(omsOrderItems);
        return orderVO;
    }

    /**
     * 更新订单状态
     * @param params
     * @return
     */
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

    /**
     * 获取当前用户的订单列表
     * @param pageParam
     * @param status
     * @return
     */
    @Override
    public Page<OrderVO> listCurrent(PageParam pageParam, Integer status) {
        Long userId = UserContext.getUserId();
        if (userId == null){
            throw new RuntimeException("用户未登录");
        }
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
            List<OrderVO> voList = new ArrayList<>();
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

    /**
     *  创建订单
     * @param request
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OmsOrder createOrder(CreateOrderRequest request) {
        // 1. 生成取餐码 (每日重置)
        String pickupCode = generatePickupCode();
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
        
        // 咖啡卡折扣计算（9折，即0.9倍）
        BigDecimal coffeeCardDiscountAmount = BigDecimal.ZERO;
        Long coffeeCardId = request.getCoffeeCardId();
        if (coffeeCardId != null && request.getPayType() != null && request.getPayType() == 3) {
            // 使用咖啡卡支付，享受9折优惠
            GiftCard coffeeCard = giftCardService.getById(coffeeCardId);
            if (coffeeCard == null) {
                throw new RuntimeException("咖啡卡不存在");
            }
            if (!coffeeCard.getMemberId().equals(userId)) {
                throw new RuntimeException("无权使用他人的咖啡卡");
            }
            if (!coffeeCard.getStatus().equals(GiftCardStatus.ACTIVE.getCode())) {
                throw new RuntimeException("咖啡卡不可用，请检查状态");
            }
            // 计算折扣金额：打九折，折扣金额 = 原价 × 0.1
            BigDecimal amountAfterPromotionAndCoupon = totalAmount.subtract(promotionAmount).subtract(couponAmount);
            coffeeCardDiscountAmount = amountAfterPromotionAndCoupon.multiply(new BigDecimal("0.1"));
        }
        
        BigDecimal payAmount = totalAmount.subtract(promotionAmount).subtract(couponAmount).subtract(coffeeCardDiscountAmount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }

        // 4. 构建订单基础信息
        OmsOrder order = new OmsOrder();
        order.setMemberId(userId);
        order.setOrderSn(generateOrderSn(userId));
        order.setPickupCode(pickupCode);
        order.setTotalAmount(totalAmount);
        order.setPromotionAmount(promotionAmount);
        order.setCouponAmount(couponAmount);
        order.setCoffeeCardId(coffeeCardId);
        order.setCoffeeCardDiscountAmount(coffeeCardDiscountAmount);
        order.setPayAmount(payAmount);
        order.setPayType(request.getPayType() == null ? 0 : request.getPayType());
        
        // 如果使用咖啡卡支付，需要检查余额并立即扣减
        if (coffeeCardId != null && request.getPayType() != null && request.getPayType() == 3) {
            GiftCard coffeeCard = giftCardService.getById(coffeeCardId);
            if (coffeeCard.getBalance().compareTo(payAmount) < 0) {
                throw new RuntimeException("咖啡卡余额不足");
            }
            // 咖啡卡支付，订单状态直接设为待制作（已支付）
            order.setStatus(OrderStatus.PENDING_MAKING.getCode());
            order.setPaymentTime(LocalDateTime.now());
        } else {
            // 其他支付方式，订单状态为待支付
            order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        }
        
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

        // 9. 如果使用咖啡卡支付，立即扣减余额
        if (coffeeCardId != null && request.getPayType() != null && request.getPayType() == 3) {
            try {
                giftCardService.deductBalance(coffeeCardId, payAmount, order.getId());
                log.info("咖啡卡支付成功，订单ID: {}, 扣减金额: {}", order.getId(), payAmount);
            } catch (Exception e) {
                log.error("咖啡卡扣减失败，订单ID: {}", order.getId(), e);
                throw new RuntimeException("咖啡卡扣减失败: " + e.getMessage());
            }
        }

        return order;
    }

    /**
     * 生成取餐码逻辑
     * 规则：日期 + 自增数字，例如 A101 (为了简单，这里直接用数字 101, 102...)
     * Redis Key: coffee:order:pickup:20231223
     */
    private String generatePickupCode() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = "coffee:order:pickup:" + dateStr;

        // Redis 原子递增
        Long increment = redisTemplate.opsForValue().increment(key);

        // 如果是今天第一个单，设置过期时间为 24 小时 (避免 Redis 堆积垃圾数据)
        if (increment != null && increment == 1) {
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
        }

        // 格式化：从 100 开始，显得单子多一点，或者从 1 开始
        // 这里演示：直接从 101 开始 (1 + 100)
        return String.valueOf(100 + increment);

        // 如果想要 "A101" 这种格式：
        // return "A" + (100 + increment);
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


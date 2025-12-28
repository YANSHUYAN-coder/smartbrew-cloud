package com.coffee.system.service.impl;

import com.alipay.api.AlipayClient;
import com.coffee.system.service.*;
import org.springframework.amqp.core.Message;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.config.RabbitMqConfig;
import com.coffee.common.constant.DateFormatConstants;
import com.coffee.common.context.UserContext;
import com.coffee.common.dto.CreateOrderRequest;
import com.coffee.common.dto.PageParam;
import com.coffee.common.dict.OrderStatus;
import com.coffee.system.domain.entity.OmsOrder;
import com.coffee.system.domain.entity.OmsOrderItem;
import com.coffee.system.domain.vo.OrderVO;
import com.coffee.system.mapper.OmsOrderMapper;
import com.coffee.system.mapper.OrderItemMapper;
import com.coffee.system.domain.entity.SmsCouponHistory;
import com.coffee.system.domain.entity.SmsCoupon;
import com.coffee.system.mapper.SmsCouponHistoryMapper;
import com.coffee.system.mapper.SmsCouponMapper;
import com.coffee.system.domain.entity.GiftCard;
import com.coffee.common.dict.GiftCardStatus;
import com.coffee.system.domain.entity.OmsCartItem;
import com.coffee.system.domain.entity.UmsMemberReceiveAddress;
import com.coffee.system.domain.entity.UmsMember;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @Lazy
    private UmsMemberService memberService;

    @Autowired
    private SmsCouponHistoryMapper couponHistoryMapper;

    @Autowired
    private SmsCouponMapper couponMapper;

    @Autowired
    private UmsMemberIntegrationHistoryService integrationHistoryService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SmsCouponService smsCouponService;

    /**
     * 获取所有订单列表（包含商品明细）
     *
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

        // 转换为 OrderVO 并补充商品明细（优化：批量查询，避免 N+1 查询问题）
        Page<OrderVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            // 收集所有订单ID
            List<Long> orderIds = page.getRecords().stream()
                    .map(OmsOrder::getId)
                    .collect(Collectors.toList());

            // 批量查询所有订单项
            List<OmsOrderItem> allItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OmsOrderItem>()
                            .in(OmsOrderItem::getOrderId, orderIds)
            );

            // 按订单ID分组
            Map<Long, List<OmsOrderItem>> itemsMap = allItems.stream()
                    .collect(Collectors.groupingBy(OmsOrderItem::getOrderId));

            // 构建 OrderVO 列表
            List<OrderVO> voList = page.getRecords().stream().map(order -> {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                // 从分组后的 Map 中获取对应的订单项
                orderVO.setOrderItemList(itemsMap.getOrDefault(order.getId(), new ArrayList<>()));
                return orderVO;
            }).collect(Collectors.toList());

            voPage.setRecords(voList);
        }

        return voPage;
    }


    /**
     * 获取订单详情
     *
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
     *
     * @param params
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());

        // 查询订单信息
        OmsOrder order = this.getById(id);
        if (order == null) {
            return false;
        }

        // 更新订单状态
        boolean updateResult = this.update(new LambdaUpdateWrapper<OmsOrder>()
                .eq(OmsOrder::getId, id)
                .set(OmsOrder::getStatus, status));

        // 如果订单状态更新为已取消，恢复优惠券和咖啡卡余额
        if (updateResult && status == OrderStatus.CANCELLED.getCode()) {
            // 恢复优惠券
            smsCouponService.releaseCoupon(order.getCouponId());

            // 恢复咖啡卡余额（如果使用咖啡卡支付）
            if (order.getCoffeeCardId() != null && order.getPayType() != null && order.getPayType() == 3 && order.getPayAmount() != null) {
                try {
                    giftCardService.refundBalance(order.getCoffeeCardId(), order.getPayAmount(), id);
                    log.info("订单取消，恢复咖啡卡余额成功，订单ID: {}, 咖啡卡ID: {}, 恢复金额: {}",
                            id, order.getCoffeeCardId(), order.getPayAmount());
                } catch (Exception e) {
                    log.error("订单取消时恢复咖啡卡余额失败: 订单ID={}", id, e);
                    // 不影响订单状态更新，只记录日志
                }
            }
        }
        return updateResult;
    }

    /**
     * 获取当前用户的订单列表
     *
     * @param pageParam
     * @param status
     * @return
     */
    @Override
    public Page<OrderVO> listCurrent(PageParam pageParam, Integer status) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
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
            // 优化：批量查询所有订单的商品明细，避免 N+1 查询问题
            List<Long> orderIds = page.getRecords().stream()
                    .map(OmsOrder::getId)
                    .collect(Collectors.toList());

            // 批量查询所有订单项
            List<OmsOrderItem> allItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OmsOrderItem>()
                            .in(OmsOrderItem::getOrderId, orderIds)
            );

            // 按订单ID分组
            Map<Long, List<OmsOrderItem>> itemsMap = allItems.stream()
                    .collect(Collectors.groupingBy(OmsOrderItem::getOrderId));

            // 构建 OrderVO 列表
            List<OrderVO> voList = page.getRecords().stream().map(order -> {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);
                // 从分组后的 Map 中获取对应的订单项
                orderVO.setOrderItemList(itemsMap.getOrDefault(order.getId(), new ArrayList<>()));
                return orderVO;
            }).collect(Collectors.toList());

            voPage.setRecords(voList);
        }

        return voPage;
    }

    /**
     * 创建订单
     *
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

        // 4. 处理优惠券（如果使用）- 验证并保存优惠券历史记录对象，后续更新时使用
        Long couponHistoryId = request.getCouponHistoryId();
        SmsCouponHistory couponHistoryForUpdate = null; // 保存验证通过的优惠券历史记录，后续更新时使用
        Long couponId = null; // 保存优惠券模板ID，用于订单记录
        if (couponHistoryId != null && couponAmount.compareTo(BigDecimal.ZERO) > 0) {
            // 直接根据优惠券历史记录ID查询，验证是否属于当前用户且未使用
            couponHistoryForUpdate = couponHistoryMapper.selectOne(
                    new LambdaQueryWrapper<SmsCouponHistory>()
                            .eq(SmsCouponHistory::getId, couponHistoryId)
                            .eq(SmsCouponHistory::getMemberId, userId)
                            .eq(SmsCouponHistory::getUseStatus, 0) // 未使用
            );
            if (couponHistoryForUpdate == null) {
                throw new RuntimeException("优惠券不存在或已被使用");
            }
            couponId = couponHistoryForUpdate.getCouponId(); // 获取优惠券模板ID，用于订单记录
        }

        // 5. 构建订单基础信息
        OmsOrder order = new OmsOrder();
        order.setMemberId(userId);
        order.setOrderSn(generateOrderSn(userId));
        order.setPickupCode(pickupCode);
        order.setTotalAmount(totalAmount);
        order.setPromotionAmount(promotionAmount);
        order.setCouponAmount(couponAmount);
        order.setCouponId(couponId); // 保存优惠券ID
        order.setCoffeeCardId(coffeeCardId);
        order.setCoffeeCardDiscountAmount(coffeeCardDiscountAmount);
        order.setPayAmount(payAmount);
        order.setPayType(request.getPayType() == null ? 0 : request.getPayType());
        // 商品订单
        order.setOrderType(0);

        // 所有支付方式，订单初始状态都为待支付（包括咖啡卡支付）
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());

        order.setDeliveryCompany(request.getDeliveryCompany() != null ? request.getDeliveryCompany() : "门店自提");

        // 6. 填充收货人信息（从地址对象拷贝）
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

        // 7. 保存订单
        this.save(order);

        // 8. 保存订单明细（从购物车项转换）
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

        // 9. 删除已下单的购物车项
        cartService.removeByIds(request.getCartItemIds());

        // 10. 如果使用优惠券，更新优惠券状态（使用之前验证时保存的对象）
        if (couponHistoryId != null && couponAmount.compareTo(BigDecimal.ZERO) > 0 && couponHistoryForUpdate != null) {
            try {
                // 再次确认优惠券状态（防止并发问题）
                SmsCouponHistory currentHistory = couponHistoryMapper.selectById(couponHistoryForUpdate.getId());
                if (currentHistory != null && currentHistory.getUseStatus() == 0) {
                    // 更新优惠券历史记录为已使用
                    couponHistoryForUpdate.setUseStatus(1); // 已使用
                    couponHistoryForUpdate.setUseTime(now);
                    couponHistoryForUpdate.setOrderId(order.getId());
                    couponHistoryForUpdate.setOrderSn(order.getOrderSn());
                    couponHistoryMapper.updateById(couponHistoryForUpdate);

                    // 更新优惠券的使用数量（更新 sms_coupon 表）
                    couponMapper.update(null, new LambdaUpdateWrapper<SmsCoupon>()
                            .eq(SmsCoupon::getId, couponId)
                            .setSql("use_count = use_count + 1")
                    );

                    log.info("优惠券使用成功，订单ID: {}, 优惠券ID: {}, 优惠券历史ID: {}",
                            order.getId(), couponId, couponHistoryForUpdate.getId());
                } else {
                    log.warn("优惠券状态已变更，无法更新。订单ID: {}, 优惠券ID: {}, 当前状态: {}",
                            order.getId(), couponId, currentHistory != null ? currentHistory.getUseStatus() : "null");
                    throw new RuntimeException("优惠券已被使用，无法重复使用");
                }
            } catch (Exception e) {
                log.error("优惠券状态更新失败，订单ID: {}, 优惠券ID: {}", order.getId(), couponId, e);
                throw new RuntimeException("优惠券状态更新失败: " + e.getMessage());
            }
        }

        // 注意：咖啡卡支付不再在创建订单时立即扣减，改为在支付时扣减
        // 优惠券在创建订单时已经使用，如果订单取消需要恢复

        // 假设订单创建成功，orderId 为新生成的订单ID
        Long orderId = order.getId();

        // 【新增】发送延迟消息，15分钟后检查订单是否支付
        sendDelayMessage(orderId);

        return order;
    }

    /**
     * 发送订单超时取消的延迟消息
     */
    private void sendDelayMessage(Long orderId) {
        // 延迟时间：15分钟 (毫秒)
        int delayTime = 15 * 60 * 1000;
//        int delayTime = 30 * 1000; // 测试用：30秒

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.DELAY_EXCHANGE_NAME,
                RabbitMqConfig.ORDER_TIMEOUT_ROUTING_KEY,
                orderId, // 消息内容：订单ID
                new MessagePostProcessor() {
                    @Override
                    public Message postProcessMessage(Message message) throws AmqpException {
                        // 设置延迟头
                        message.getMessageProperties().setDelay(delayTime);
                        return message;
                    }
                }
        );
        log.info("订单创建成功，发送延迟取消消息。OrderId: {}, Delay: {}ms", orderId, delayTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payByCoffeeCard(Long orderId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        // 1. 查询订单
        OmsOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 2. 校验订单所有权
        if (!order.getMemberId().equals(userId)) {
            throw new RuntimeException("无权支付他人订单");
        }

        // 3. 校验订单状态
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getStatus())) {
            throw new RuntimeException("订单状态异常，当前状态：" + OrderStatus.getDescByCode(order.getStatus()) + "，只有待付款订单才能支付");
        }

        // 4. 校验支付方式
        if (order.getPayType() == null || !order.getPayType().equals(3)) {
            throw new RuntimeException("该订单不是咖啡卡支付订单");
        }

        // 5. 校验咖啡卡
        if (order.getCoffeeCardId() == null) {
            throw new RuntimeException("订单未绑定咖啡卡");
        }

        GiftCard coffeeCard = giftCardService.getById(order.getCoffeeCardId());
        if (coffeeCard == null) {
            throw new RuntimeException("咖啡卡不存在");
        }
        if (!coffeeCard.getMemberId().equals(userId)) {
            throw new RuntimeException("无权使用他人的咖啡卡");
        }
        if (!coffeeCard.getStatus().equals(GiftCardStatus.ACTIVE.getCode())) {
            throw new RuntimeException("咖啡卡不可用，请检查状态");
        }
        if (coffeeCard.getBalance().compareTo(order.getPayAmount()) < 0) {
            throw new RuntimeException("咖啡卡余额不足");
        }

        // 6. 扣减咖啡卡余额
        giftCardService.deductBalance(order.getCoffeeCardId(), order.getPayAmount(), orderId);

        // 7. 更新订单状态为待制作（已支付）
        order.setStatus(OrderStatus.PENDING_MAKING.getCode());
        order.setPaymentTime(LocalDateTime.now());
        boolean updateResult = this.updateById(order);

        if (updateResult) {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.ORDER_EVENT_EXCHANGE,
                    RabbitMqConfig.ORDER_PAY_KEY,
                    order.getId() // 发送订单ID
            );
            log.info("发送支付成功消息，订单ID: {}", order.getId());
            log.info("咖啡卡支付成功，订单ID: {}, 扣减金额: {}", orderId, order.getPayAmount());
        }

        return updateResult;
    }

    /**
     * 生成取餐码逻辑
     * 规则：日期 + 自增数字，例如 A101 (为了简单，这里直接用数字 101, 102...)
     * Redis Key: coffee:order:pickup:20231223
     */
    private String generatePickupCode() {
        try {
            String dateStr = LocalDate.now().format(DateFormatConstants.DATE_COMPACT);
            String key = "coffee:order:pickup:" + dateStr;

            // Redis 原子递增
            Long increment = redisTemplate.opsForValue().increment(key);

            if (increment == null) {
                log.warn("Redis increment 返回 null，使用默认值");
                increment = 1L;
            }

            // 如果是今天第一个单，设置过期时间为当天结束时（额外增加1小时缓冲，确保跨天边界也能使用）
            if (increment == 1) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59).plusSeconds(1);
                long secondsUntilEndOfDay = ChronoUnit.SECONDS.between(now, endOfDay);
                // 额外增加 1 小时缓冲，确保在第二天也能使用（处理跨天边界情况）
                redisTemplate.expire(key, secondsUntilEndOfDay + 3600, TimeUnit.SECONDS);
            }

            // 格式化：从 100 开始，显得单子多一点，或者从 1 开始
            // 这里演示：直接从 101 开始 (1 + 100)
            return String.valueOf(100 + increment);
        } catch (Exception e) {
            log.error("生成取餐码失败（Redis 操作异常），使用时间戳作为备选方案", e);
            // 降级方案：使用时间戳后4位作为取餐码
            return String.valueOf(System.currentTimeMillis() % 10000);
        }
    }


    /**
     * 生成订单编号：时间戳 + 用户ID + 随机串
     */
    private String generateOrderSn(Long userId) {
        String timePart = LocalDateTime.now().format(DateFormatConstants.DATETIME_COMPACT);
        String userPart = userId == null ? "0" : String.valueOf(userId);
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return timePart + userPart + randomPart;
    }

    private BigDecimal defaultIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}


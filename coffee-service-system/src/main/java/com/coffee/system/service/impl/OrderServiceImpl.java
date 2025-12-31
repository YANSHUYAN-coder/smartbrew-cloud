package com.coffee.system.service.impl;

import com.coffee.system.domain.entity.*;
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
import com.coffee.system.domain.vo.OrderVO;
import com.coffee.system.mapper.OmsOrderMapper;
import com.coffee.system.mapper.OrderItemMapper;
import com.coffee.system.mapper.SmsCouponHistoryMapper;
import com.coffee.system.mapper.SmsCouponMapper;
import com.coffee.common.dict.GiftCardStatus;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import com.coffee.common.util.AMapUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import java.util.concurrent.TimeUnit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
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
    private SysMessageService sysMessageService;

    @Autowired
    private SmsCouponHistoryMapper couponHistoryMapper;

    @Autowired
    private SmsCouponMapper couponMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SmsCouponService smsCouponService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AMapUtil aMapUtil;

    @Autowired
    private OmsStoreService storeService;

    /**
     * 配送范围限制（米）- 兜底值
     */
    private static final int DEFAULT_DELIVERY_RADIUS = 5000;

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
                // 填充门店名称
                if (order.getStoreId() != null) {
                    OmsStore store = storeService.getById(order.getStoreId());
                    if (store != null) {
                        orderVO.setStoreName(store.getName());
                    }
                }
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
        // 填充门店名称
        if (omsOrder.getStoreId() != null) {
            OmsStore store = storeService.getById(omsOrder.getStoreId());
            if (store != null) {
                orderVO.setStoreName(store.getName());
            }
        }
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
        Object reasonObj = params.get("cancelReason");
        String cancelReason = reasonObj != null ? reasonObj.toString() : null;

        // 查询订单信息
        OmsOrder order = this.getById(id);
        if (order == null) {
            return false;
        }

        // 更新订单状态
        boolean updateResult = this.update(new LambdaUpdateWrapper<OmsOrder>()
                .eq(OmsOrder::getId, id)
                .set(OmsOrder::getStatus, status)
                .set(OmsOrder::getCancelReason, cancelReason));

        if (updateResult && status == OrderStatus.PENDING_PICKUP.getCode()) {
            // 重新查询订单，确保获取最新的数据（包括 pickupCode 等）
            OmsOrder latestOrder = this.getById(id);
            if (latestOrder != null) {
                sysMessageService.sendMessage(
                        order.getMemberId(),
                        "取餐提醒",
                        "您的订单 " + order.getOrderSn() + " (取餐码：" + order.getPickupCode() + ") 已制作完成，请及时取餐。",
                        1, // 1-订单通知
                        order.getId()
                );
                sendPickupNotification(latestOrder);
            } else {
                log.warn("订单状态更新为待取餐，但重新查询订单失败，订单ID: {}", id);
            }
        }

        // 如果订单状态更新为已取消，恢复优惠券和咖啡卡余额
        if (updateResult && status == OrderStatus.CANCELLED.getCode()) {
            sysMessageService.sendMessage(
                    order.getMemberId(),
                    "订单取消通知",
                    "您的订单 " + order.getOrderSn() + " 已取消。原因：" + (cancelReason != null ? cancelReason : "无"),
                    1, // 1-订单通知
                    order.getId()
            );
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
     * 发送取餐通知消息到 MQ
     */
    private void sendPickupNotification(OmsOrder order) {
        try {
            Map<String, Object> message = new HashMap<>();
            // 确保 userId 是 String 类型，方便接收端解析
            message.put("userId", String.valueOf(order.getMemberId()));
            message.put("orderId", order.getId());
            message.put("orderSn", order.getOrderSn());
            message.put("pickupCode", order.getPickupCode()); // 带上取餐码
            message.put("type", "PICKUP_READY"); // 消息类型

            // 发送到 Notification Exchange (在 RabbitMqConfig 中定义的)
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.NOTIFICATION_EXCHANGE,
                    RabbitMqConfig.PICKUP_ROUTING_KEY,
                    message,
                    new CorrelationData("pickup-" + order.getOrderSn())
            );

            log.info("取餐通知已发送 MQ -> 用户: {}, 订单: {}", order.getMemberId(), order.getOrderSn());
        } catch (Exception e) {
            log.error("发送取餐通知 MQ 失败", e);
            // 这里可以考虑加一个补偿表，或者只是记录日志，不要影响主流程回滚
        }
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
                // 填充门店名称
                if (order.getStoreId() != null) {
                    OmsStore store = storeService.getById(order.getStoreId());
                    if (store != null) {
                        orderVO.setStoreName(store.getName());
                    }
                }
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
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        // 防重复提交：使用分布式锁，锁的key基于用户ID和购物车项ID的hash
        String lockKey = "lock:order:create:" + userId + ":" + 
                        (request.getCartItemIds() != null ? request.getCartItemIds().hashCode() : 0);
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // 尝试加锁，不等待（0秒），锁定 5 秒后自动释放（防止重复提交）
            if (lock.tryLock(0, 5, TimeUnit.SECONDS)) {
                try {
                    return doCreateOrder(request);
                } catch (Exception e) {
                    log.error("创建订单失败，用户ID: {}", userId, e);
                    throw e;
                }
            } else {
                log.warn("订单创建防重复提交：获取锁失败，用户ID: {}", userId);
                throw new RuntimeException("请勿重复提交订单，请稍后再试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取订单创建锁被中断，用户ID: {}", userId, e);
            throw new RuntimeException("操作失败，请重试");
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 实际执行订单创建的方法
     */
    private OmsOrder doCreateOrder(CreateOrderRequest request) {
        Long userId = UserContext.getUserId();

        // 0. 验证门店营业状态
        if (request.getStoreId() == null) {
            throw new RuntimeException("请选择下单门店");
        }
        OmsStore store = storeService.getById(request.getStoreId());
        if (store == null || store.getOpenStatus() == 0) {
            throw new RuntimeException("门店休息中，暂不接单");
        }
        
        // 1. 验证并获取购物车项
        List<OmsCartItem> cartItems = validateAndGetCartItems(request, userId);
        
        // 2. 验证并获取收货地址
        UmsMemberReceiveAddress address = validateAndGetAddress(request, userId);
        
        // 3. 计算订单金额（包含配送费计算，使用指定的 store）
        OrderAmountInfo amountInfo = calculateOrderAmount(request, cartItems, address, store, userId);
        
        // 4. 验证优惠券
        CouponInfo couponInfo = validateCoupon(request, userId, amountInfo.getCouponAmount());
        
        // 5. 构建并保存订单
        OmsOrder order = buildAndSaveOrder(request, userId, address, amountInfo, couponInfo);
        
        // 6. 保存订单明细并清理购物车
        saveOrderItemsAndClearCart(order, cartItems, request.getCartItemIds());
        
        // 7. 使用优惠券（带分布式锁）
        useCouponWithLock(couponInfo, order, amountInfo.getCouponAmount());
        
        // 8. 发送延迟消息
        sendDelayMessage(order.getId());
        
        return order;
    }

    /**
     * 验证并获取购物车项
     */
    private List<OmsCartItem> validateAndGetCartItems(CreateOrderRequest request, Long userId) {
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

        return cartItems;
    }

    /**
     * 验证并获取收货地址
     * 如果是到店取（门店自提），地址可以为空，返回默认地址或null
     */
    private UmsMemberReceiveAddress validateAndGetAddress(CreateOrderRequest request, Long userId) {
        String deliveryCompany = request.getDeliveryCompany();
        boolean isPickup = deliveryCompany == null || "门店自提".equals(deliveryCompany);
        
        // 到店取时，地址可以为空
        if (isPickup && request.getAddressId() == null) {
            // 返回一个默认地址对象，用于填充订单的收货人信息（可以填门店信息或用户信息）
            return createDefaultPickupAddress(userId);
        }
        
        // 外卖配送时，地址必须提供
        if (request.getAddressId() == null) {
            throw new RuntimeException("外卖配送时，收货地址不能为空");
        }
        
        UmsMemberReceiveAddress address = addressService.getById(request.getAddressId());
        if (address == null) {
            throw new RuntimeException("收货地址不存在");
        }
        
        // 校验地址是否属于当前用户
        if (!address.getMemberId().equals(userId)) {
            throw new RuntimeException("非法操作：收货地址不属于当前用户");
        }

        return address;
    }

    /**
     * 创建默认的到店取地址（使用门店信息或用户信息）
     */
    private UmsMemberReceiveAddress createDefaultPickupAddress(Long userId) {
        UmsMemberReceiveAddress address = new UmsMemberReceiveAddress();
        // 可以设置为门店地址或用户默认信息
        // 这里简单设置为门店信息，实际可以根据业务需求调整
        address.setName("到店自取");
        address.setPhone(""); // 可以设置为门店电话
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setRegion("南山区");
        address.setDetailAddress("智咖·云门店");
        address.setPostCode("");
        return address;
    }

    /**
     * 订单金额信息
     */
    private static class OrderAmountInfo {
        private BigDecimal totalAmount;
        private BigDecimal promotionAmount;
        private BigDecimal couponAmount;
        private BigDecimal coffeeCardDiscountAmount;
        private BigDecimal deliveryFee;
        private BigDecimal payAmount;
        private Long coffeeCardId;
        private Integer distance;

        public OrderAmountInfo(BigDecimal totalAmount, BigDecimal promotionAmount, BigDecimal couponAmount,
                              BigDecimal coffeeCardDiscountAmount, BigDecimal deliveryFee, BigDecimal payAmount, 
                              Long coffeeCardId, Integer distance) {
            this.totalAmount = totalAmount;
            this.promotionAmount = promotionAmount;
            this.couponAmount = couponAmount;
            this.coffeeCardDiscountAmount = coffeeCardDiscountAmount;
            this.deliveryFee = deliveryFee;
            this.payAmount = payAmount;
            this.coffeeCardId = coffeeCardId;
            this.distance = distance;
        }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getPromotionAmount() { return promotionAmount; }
        public BigDecimal getCouponAmount() { return couponAmount; }
        public BigDecimal getCoffeeCardDiscountAmount() { return coffeeCardDiscountAmount; }
        public BigDecimal getDeliveryFee() { return deliveryFee; }
        public BigDecimal getPayAmount() { return payAmount; }
        public Long getCoffeeCardId() { return coffeeCardId; }
        public Integer getDistance() { return distance; }
    }

    /**
     * 计算订单金额
     */
    private OrderAmountInfo calculateOrderAmount(CreateOrderRequest request, List<OmsCartItem> cartItems, 
                                                 UmsMemberReceiveAddress address, OmsStore store, Long userId) {
        // 计算商品总金额
        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal promotionAmount = defaultIfNull(request.getPromotionAmount());
        BigDecimal couponAmount = defaultIfNull(request.getCouponAmount());
        
        // 配送费计算
        BigDecimal deliveryFee = BigDecimal.ZERO;
        Integer distance = 0;
        String deliveryCompany = request.getDeliveryCompany();
        if ("外送配送".equals(deliveryCompany) || "外卖配送".equals(deliveryCompany)) {
            if (store.getDeliveryStatus() == 0) {
                throw new RuntimeException("该门店暂不支持外送服务");
            }

            // 校验经纬度是否存在
            if (address.getLongitude() == null || address.getLatitude() == null) {
                throw new RuntimeException("所选地址缺少坐标信息，请重新编辑地址或在地图上选择");
            }
            
            String destination = address.getLongitude() + "," + address.getLatitude();
            String storeLoc = store.getLongitude() + "," + store.getLatitude();
            distance = aMapUtil.getDistance(storeLoc, destination);
            
            if (distance == -1) {
                log.error("计算配送距离失败，单号：{}，地址坐标：{}", request.getCartItemIds(), destination);
                // 降级处理：给个基础运费
                deliveryFee = store.getBaseDeliveryFee() != null ? store.getBaseDeliveryFee() : new BigDecimal("5.00"); 
            } else {
                int radius = store.getDeliveryRadius() != null ? store.getDeliveryRadius() : DEFAULT_DELIVERY_RADIUS;
                if (distance > radius) {
                    throw new RuntimeException("超出配送范围，该门店仅支持周边 " + (radius / 1000) + "km 配送");
                }
                
                BigDecimal baseFee = store.getBaseDeliveryFee() != null ? store.getBaseDeliveryFee() : new BigDecimal("5.00");
                if (distance <= 3000) {
                    deliveryFee = baseFee;
                } else {
                    int extraKm = (int) Math.ceil((distance - 3000) / 1000.0);
                    deliveryFee = baseFee.add(new BigDecimal(extraKm * 2));
                }
            }
        }

        // 咖啡卡折扣计算（9折，即0.9倍）
        BigDecimal coffeeCardDiscountAmount = BigDecimal.ZERO;
        Long coffeeCardId = request.getCoffeeCardId();
        if (coffeeCardId != null && request.getPayType() != null && request.getPayType() == 3) {
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

        // 最终支付金额 = 商品总价 - 促销 - 优惠券 - 咖啡卡折扣 + 配送费
        BigDecimal payAmount = totalAmount.subtract(promotionAmount).subtract(couponAmount)
                .subtract(coffeeCardDiscountAmount).add(deliveryFee);
        
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }

        return new OrderAmountInfo(totalAmount, promotionAmount, couponAmount, 
                                  coffeeCardDiscountAmount, deliveryFee, payAmount, coffeeCardId, distance);
    }

    /**
     * 优惠券信息
     */
    private static class CouponInfo {
        private Long couponHistoryId;
        private SmsCouponHistory couponHistory;
        private Long couponId;

        public CouponInfo(Long couponHistoryId, SmsCouponHistory couponHistory, Long couponId) {
            this.couponHistoryId = couponHistoryId;
            this.couponHistory = couponHistory;
            this.couponId = couponId;
        }

        public Long getCouponHistoryId() { return couponHistoryId; }
        public SmsCouponHistory getCouponHistory() { return couponHistory; }
        public Long getCouponId() { return couponId; }
    }

    /**
     * 验证优惠券
     */
    private CouponInfo validateCoupon(CreateOrderRequest request, Long userId, BigDecimal couponAmount) {
        Long couponHistoryId = request.getCouponHistoryId();
        if (couponHistoryId == null || couponAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return new CouponInfo(null, null, null);
        }
        
        SmsCouponHistory couponHistory = couponHistoryMapper.selectOne(
                    new LambdaQueryWrapper<SmsCouponHistory>()
                            .eq(SmsCouponHistory::getId, couponHistoryId)
                            .eq(SmsCouponHistory::getMemberId, userId)
                            .eq(SmsCouponHistory::getUseStatus, 0) // 未使用
            );
        
        if (couponHistory == null) {
                throw new RuntimeException("优惠券不存在或已被使用");
            }
        
        Long couponId = couponHistory.getCouponId();
        return new CouponInfo(couponHistoryId, couponHistory, couponId);
        }

    /**
     * 构建并保存订单
     */
    private OmsOrder buildAndSaveOrder(CreateOrderRequest request, Long userId, 
                                       UmsMemberReceiveAddress address, 
                                       OrderAmountInfo amountInfo, CouponInfo couponInfo) {
        OmsOrder order = new OmsOrder();
        LocalDateTime now = LocalDateTime.now();
        
        // 基础信息
        order.setMemberId(userId);
        order.setStoreId(request.getStoreId()); // 保存门店ID
        order.setOrderSn(generateOrderSn(userId));
        order.setPickupCode(generatePickupCode());
        order.setTotalAmount(amountInfo.getTotalAmount());
        order.setPromotionAmount(amountInfo.getPromotionAmount());
        order.setCouponAmount(amountInfo.getCouponAmount());
        order.setCouponId(couponInfo.getCouponId());
        order.setCoffeeCardId(amountInfo.getCoffeeCardId());
        order.setCoffeeCardDiscountAmount(amountInfo.getCoffeeCardDiscountAmount());
        order.setPayAmount(amountInfo.getPayAmount());
        order.setPayType(request.getPayType() == null ? 0 : request.getPayType());
        order.setOrderType(0); // 商品订单
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setDeliveryCompany(request.getDeliveryCompany() != null ? request.getDeliveryCompany() : "门店自提");
        order.setDeliveryFee(amountInfo.getDeliveryFee());
        order.setDeliveryDistance(amountInfo.getDistance());
        
        // 收货人信息（字段名不完全匹配，手动设置）
        copyAddressToOrder(address, order);
        
        // 备注和时间
        order.setNote(request.getRemark());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        
        // 保存订单
        this.save(order);
        
        return order;
    }

    /**
     * 保存订单明细并清理购物车
     */
    private void saveOrderItemsAndClearCart(OmsOrder order, List<OmsCartItem> cartItems, List<Long> cartItemIds) {
        LocalDateTime now = LocalDateTime.now();
        
        // 保存订单明细（使用对象拷贝，然后手动设置不匹配的字段）
        for (OmsCartItem cartItem : cartItems) {
            OmsOrderItem item = new OmsOrderItem();
            // 拷贝匹配的字段（productId, productPic, productName, productSkuId, productSkuCode）
            BeanUtils.copyProperties(cartItem, item);
            // 设置订单相关信息
            item.setOrderId(order.getId());
            item.setOrderSn(order.getOrderSn());
            // 设置不匹配的字段
            item.setProductPrice(cartItem.getPrice());
            item.setProductQuantity(cartItem.getQuantity());
            item.setProductAttr(cartItem.getProductSubTitle());
            item.setCreateTime(now);
            orderItemMapper.insert(item);
        }

        // 删除已下单的购物车项
        cartService.removeByIds(cartItemIds);
    }

    /**
     * 使用优惠券（带分布式锁保护）
     */
    private void useCouponWithLock(CouponInfo couponInfo, OmsOrder order, BigDecimal couponAmount) {
        if (couponInfo.getCouponHistoryId() == null || 
            couponAmount.compareTo(BigDecimal.ZERO) <= 0 || 
            couponInfo.getCouponHistory() == null) {
            return;
        }
        
        String lockKey = "lock:coupon:use:" + couponInfo.getCouponHistoryId();
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
            try {
                    // 在锁内再次确认优惠券状态（双重检查）
                    SmsCouponHistory currentHistory = couponHistoryMapper.selectById(couponInfo.getCouponHistory().getId());
                if (currentHistory != null && currentHistory.getUseStatus() == 0) {
                    // 更新优惠券历史记录为已使用
                        LocalDateTime now = LocalDateTime.now();
                        couponInfo.getCouponHistory().setUseStatus(1);
                        couponInfo.getCouponHistory().setUseTime(now);
                        couponInfo.getCouponHistory().setOrderId(order.getId());
                        couponInfo.getCouponHistory().setOrderSn(order.getOrderSn());
                        couponHistoryMapper.updateById(couponInfo.getCouponHistory());

                        // 更新优惠券的使用数量
                    couponMapper.update(null, new LambdaUpdateWrapper<SmsCoupon>()
                                .eq(SmsCoupon::getId, couponInfo.getCouponId())
                            .setSql("use_count = use_count + 1")
                    );

                    log.info("优惠券使用成功，订单ID: {}, 优惠券ID: {}, 优惠券历史ID: {}",
                                order.getId(), couponInfo.getCouponId(), couponInfo.getCouponHistoryId());
                } else {
                    log.warn("优惠券状态已变更，无法更新。订单ID: {}, 优惠券ID: {}, 当前状态: {}",
                                order.getId(), couponInfo.getCouponId(), 
                                currentHistory != null ? currentHistory.getUseStatus() : "null");
                    throw new RuntimeException("优惠券已被使用，无法重复使用");
                }
            } catch (Exception e) {
                    log.error("优惠券状态更新失败，订单ID: {}, 优惠券ID: {}", order.getId(), couponInfo.getCouponId(), e);
                throw new RuntimeException("优惠券状态更新失败: " + e.getMessage());
            }
            } else {
                log.warn("获取优惠券使用锁失败，订单ID: {}, 优惠券历史ID: {}", order.getId(), couponInfo.getCouponHistoryId());
                throw new RuntimeException("系统繁忙，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取优惠券使用锁被中断，订单ID: {}, 优惠券历史ID: {}", order.getId(), couponInfo.getCouponHistoryId(), e);
            throw new RuntimeException("操作失败，请重试");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
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
                },
                new CorrelationData("timeout-" + orderId)
        );
        log.info("订单创建成功，发送延迟取消消息。OrderId: {}, Delay: {}ms", orderId, delayTime);
    }

    /**
     * 将地址信息拷贝到订单（字段名不完全匹配，需要手动映射）
     */
    private void copyAddressToOrder(UmsMemberReceiveAddress address, OmsOrder order) {
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverDetailAddress(address.getDetailAddress());
        order.setReceiverPostCode(address.getPostCode());
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
                    order.getId(),
                    new CorrelationData("card-pay-" + order.getOrderSn())
            );
            log.info("发送支付成功消息，订单ID: {}", order.getId());
            log.info("咖啡卡支付成功，订单ID: {}, 扣减金额: {}", orderId, order.getPayAmount());
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.ORDER_EVENT_EXCHANGE,
                    RabbitMqConfig.NEW_ORDER_KEY,
                    order.getId(),
                    new CorrelationData("card-new-" + order.getOrderSn())
            );

            log.info("咖啡卡支付成功，已通知管理端和积分服务。订单ID: {}", orderId);
        }

        return updateResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirm(Long id) {
        // 1. 获取当前登录用户ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        // 2. 查询订单，校验所有权和状态
        OmsOrder order = this.getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getMemberId().equals(userId)) {
            throw new RuntimeException("非法操作：无权操作他人订单");
        }
        if (order.getStatus() != OrderStatus.PENDING_PICKUP.getCode()) {
            throw new RuntimeException("当前状态不允许确认收货（仅待取餐订单可确认收货）");
        }
        return this.updateStatus(
                Map.of("id", id, "status", OrderStatus.COMPLETED.getCode()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(Map<String, Object> params) {
        Long orderId = Long.valueOf(params.get("orderId").toString());
        String reason = params.get("reason") != null ? params.get("reason").toString() : "用户取消";

        // 1. 获取当前登录用户ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }

        // 2. 查询订单，校验所有权和状态
        OmsOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getMemberId().equals(userId)) {
            throw new RuntimeException("非法操作：无权操作他人订单");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode()) {
            throw new RuntimeException("当前状态不允许确认收货（仅待取餐订单可确认收货）");
        }

        // 3. 执行取消逻辑
        return this.updateStatus(
                Map.of("id", orderId, "status", OrderStatus.CANCELLED.getCode(), "cancelReason", reason));
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



package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.constant.DateFormatConstants;
import com.coffee.common.context.UserContext;
import com.coffee.common.dict.GiftCardStatus;
import com.coffee.common.dict.OrderStatus;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.GiftCard;
import com.coffee.system.domain.entity.GiftCardTxn;
import com.coffee.system.domain.entity.OmsOrder;
import com.coffee.system.mapper.GiftCardMapper;
import com.coffee.system.mapper.GiftCardTxnMapper;
import com.coffee.system.service.GiftCardService;
import com.coffee.system.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 咖啡卡业务实现
 */
@Service
public class GiftCardServiceImpl extends ServiceImpl<GiftCardMapper, GiftCard> implements GiftCardService {

    @Autowired
    private GiftCardTxnMapper giftCardTxnMapper;

    @Autowired
    @Lazy
    private OrderService orderService;

    @Override
    public Page<GiftCard> listCurrent(PageParam pageParam) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        Page<GiftCard> page = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<GiftCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GiftCard::getMemberId, userId)
                .orderByDesc(GiftCard::getCreateTime);
        return this.baseMapper.selectPage(page, wrapper);
    }

    @Override
    public BigDecimal getTotalBalance() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return BigDecimal.ZERO;
        }
        LambdaQueryWrapper<GiftCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GiftCard::getMemberId, userId)
                .eq(GiftCard::getStatus, GiftCardStatus.ACTIVE.getCode()); // 只统计可用状态的咖啡卡
        return this.list(wrapper).stream()
                .map(GiftCard::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public GiftCard createForCurrent(BigDecimal amount, String name, String greeting, Integer validDays) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("咖啡卡金额必须大于 0");
        }

        // 1. 构建礼品卡
        GiftCard card = new GiftCard();
        card.setCardNo(generateCardNo(userId));
        card.setMemberId(userId);
        card.setName(name != null ? name : "咖啡会员卡");
        card.setOriginalAmount(amount);
        card.setBalance(amount);
        card.setStatus(GiftCardStatus.ACTIVE.getCode());

        int days = validDays != null && validDays > 0 ? validDays : 365;
        card.setExpireTime(LocalDateTime.now().plusDays(days));
        card.setGreeting(greeting);

        // 2. 保存礼品卡
        this.save(card);

        // 3. 写入一条「发卡/充值」流水
        GiftCardTxn txn = new GiftCardTxn();
        txn.setCardId(card.getId());
        txn.setMemberId(userId);
        txn.setType(0); // 0 -> 充值/发卡
        txn.setAmount(amount);
        txn.setOrderId(null);
        txn.setRemark("购卡/发卡");
        giftCardTxnMapper.insert(txn);

        return card;
    }

    @Override
    public Page<GiftCardTxn> getTransactions(Long cardId, PageParam pageParam) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        // 1. 先校验这张卡是否属于当前用户
        GiftCard card = this.getById(cardId);
        if (card == null) {
            throw new RuntimeException("咖啡卡不存在");
        }
        if (!card.getMemberId().equals(userId)) {
            throw new RuntimeException("无权查看他人咖啡卡的交易记录");
        }

        // 2. 查询该卡的交易明细
        Page<GiftCardTxn> page = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<GiftCardTxn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GiftCardTxn::getCardId, cardId)
                .orderByDesc(GiftCardTxn::getCreateTime);
        return giftCardTxnMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OmsOrder createGiftCardOrder(BigDecimal amount, String name, String greeting, Integer validDays) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("咖啡卡金额必须大于 0");
        }

        // 1. 构建咖啡卡订单（使用订单表，标记 delivery_company = "虚拟商品"）
        OmsOrder order = new OmsOrder();
        order.setMemberId(userId);
        order.setOrderSn(generateOrderSn(userId));
        order.setTotalAmount(amount);
        order.setPromotionAmount(BigDecimal.ZERO);
        order.setCouponAmount(BigDecimal.ZERO);
        order.setPayAmount(amount);
        order.setPayType(0); // 未支付
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode()); // 待支付
        order.setOrderType(1); // 咖啡卡订单
        order.setDeliveryCompany("虚拟商品");

        // 2. 填充收货人信息（使用用户默认信息，或使用固定值）
        // 咖啡卡是虚拟商品，不需要真实地址，但订单表要求必填，所以填默认值
        order.setReceiverName("虚拟商品");
        order.setReceiverPhone("00000000000");
        order.setReceiverProvince("虚拟");
        order.setReceiverCity("虚拟");
        order.setReceiverRegion("虚拟");
        order.setReceiverDetailAddress("咖啡卡订单");

        // 3. 在 note 字段中存储咖啡卡信息（JSON格式），用于支付成功后创建咖啡卡
        // 格式：GIFT_CARD:{"name":"xxx","greeting":"xxx","validDays":365}
        String noteJson = String.format("GIFT_CARD:{\"name\":\"%s\",\"greeting\":\"%s\",\"validDays\":%d}",
                name != null ? name.replace("\"", "\\\"") : "咖啡会员卡",
                greeting != null ? greeting.replace("\"", "\\\"") : "",
                validDays != null && validDays > 0 ? validDays : 365);
        order.setNote(noteJson);

        LocalDateTime now = LocalDateTime.now();
        order.setCreateTime(now);
        order.setUpdateTime(now);

        // 4. 保存订单
        orderService.save(order);

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GiftCard activateGiftCardByOrder(Long orderId) {
        // 1. 查询订单
        OmsOrder order = orderService.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 2. 检查是否是咖啡卡订单（虚拟商品）
        if (order.getDeliveryCompany() == null || !"虚拟商品".equals(order.getDeliveryCompany())) {
            throw new RuntimeException("该订单不是咖啡卡订单");
        }
        
        // 3. 解析咖啡卡信息（简单解析，实际可以用 JSON 库）
        String note = order.getNote();
        if (note == null || !note.startsWith("GIFT_CARD:")) {
            throw new RuntimeException("该订单不是咖啡卡订单，缺少咖啡卡信息");
        }
        String jsonStr = note.substring("GIFT_CARD:".length());
        String name = "咖啡会员卡";
        String greeting = "";
        int validDays = 365;

        // 简单解析 JSON（为了简化，这里用字符串处理，实际建议用 JSON 库）
        try {
            if (jsonStr.contains("\"name\"")) {
                int nameStart = jsonStr.indexOf("\"name\":\"") + 8;
                int nameEnd = jsonStr.indexOf("\"", nameStart);
                if (nameEnd > nameStart) {
                    name = jsonStr.substring(nameStart, nameEnd);
                }
            }
            if (jsonStr.contains("\"greeting\"")) {
                int greetingStart = jsonStr.indexOf("\"greeting\":\"") + 12;
                int greetingEnd = jsonStr.indexOf("\"", greetingStart);
                if (greetingEnd > greetingStart) {
                    greeting = jsonStr.substring(greetingStart, greetingEnd);
                }
            }
            if (jsonStr.contains("\"validDays\"")) {
                int daysStart = jsonStr.indexOf("\"validDays\":") + 12;
                int daysEnd = jsonStr.indexOf(",", daysStart);
                if (daysEnd == -1) daysEnd = jsonStr.indexOf("}", daysStart);
                if (daysEnd > daysStart) {
                    validDays = Integer.parseInt(jsonStr.substring(daysStart, daysEnd).trim());
                }
            }
        } catch (Exception e) {
            // 解析失败，使用默认值
        }

        // 4. 创建并激活礼品卡
        GiftCard card = new GiftCard();
        card.setCardNo(generateCardNo(order.getMemberId()));
        card.setMemberId(order.getMemberId());
        card.setName(name);
        card.setOriginalAmount(order.getPayAmount());
        card.setBalance(order.getPayAmount());
        card.setStatus(GiftCardStatus.ACTIVE.getCode());
        card.setExpireTime(LocalDateTime.now().plusDays(validDays));
        card.setGreeting(greeting);

        this.save(card);

        // 5. 写入一条「发卡/充值」流水，关联订单ID
        GiftCardTxn txn = new GiftCardTxn();
        txn.setCardId(card.getId());
        txn.setMemberId(order.getMemberId());
        txn.setType(0); // 0 -> 充值/发卡
        txn.setAmount(order.getPayAmount());
        txn.setOrderId(orderId); // 关联订单ID
        txn.setRemark("购卡/发卡（订单号：" + order.getOrderSn() + "）");
        giftCardTxnMapper.insert(txn);

        return card;
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

    /**
     * 从咖啡卡余额中扣减（用于支付订单）
     *
     * @param cardId 咖啡卡ID
     * @param amount 扣减金额
     * @param orderId 关联订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductBalance(Long cardId, BigDecimal amount, Long orderId) {
        GiftCard card = this.getById(cardId);
        if (card == null) {
            throw new RuntimeException("咖啡卡不存在");
        }
        if (!card.getStatus().equals(GiftCardStatus.ACTIVE.getCode())) {
            throw new RuntimeException("咖啡卡不可用");
        }
        if (card.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("咖啡卡余额不足");
        }

        // 扣减余额
        card.setBalance(card.getBalance().subtract(amount));
        if (card.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            card.setStatus(GiftCardStatus.USED_UP.getCode());
        }
        this.updateById(card);

        // 记录流水
        GiftCardTxn txn = new GiftCardTxn();
        txn.setCardId(cardId);
        txn.setMemberId(card.getMemberId());
        txn.setType(1); // 1 -> 消费
        txn.setAmount(amount.negate()); // 负数表示扣减
        txn.setOrderId(orderId);
        txn.setRemark("订单支付 (订单号: " + orderId + ")");
        giftCardTxnMapper.insert(txn);
    }

    /**
     * 恢复咖啡卡余额（用于订单取消退款）
     *
     * @param cardId 咖啡卡ID
     * @param amount 恢复金额
     * @param orderId 关联订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundBalance(Long cardId, BigDecimal amount, Long orderId) {
        GiftCard card = this.getById(cardId);
        if (card == null) {
            throw new RuntimeException("咖啡卡不存在");
        }

        // 恢复余额
        card.setBalance(card.getBalance().add(amount));
        // 如果咖啡卡之前是已用完状态，现在有余额了，恢复为可用状态
        if (card.getStatus().equals(GiftCardStatus.USED_UP.getCode())) {
            card.setStatus(GiftCardStatus.ACTIVE.getCode());
        }
        this.updateById(card);

        // 记录退款流水
        GiftCardTxn txn = new GiftCardTxn();
        txn.setCardId(cardId);
        txn.setMemberId(card.getMemberId());
        txn.setType(2); // 2 -> 退款
        txn.setAmount(amount); // 正数表示增加
        txn.setOrderId(orderId);
        txn.setRemark("订单取消退款 (订单号: " + orderId + ")");
        giftCardTxnMapper.insert(txn);
    }

    /**
     * 生成咖啡卡卡号：日期前缀 + 用户ID 尾号 + 随机串
     */
    private String generateCardNo(Long userId) {
        String datePart = LocalDateTime.now().format(DateFormatConstants.DATE_COMPACT);
        String userPart = userId == null ? "0000" : String.format("%04d", userId % 10000);
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "CC" + datePart + userPart + randomPart; // CC = Coffee Card
    }
}



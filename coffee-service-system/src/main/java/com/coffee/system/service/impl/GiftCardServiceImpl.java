package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.context.UserContext;
import com.coffee.common.dict.GiftCardStatus;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.GiftCard;
import com.coffee.system.domain.entity.GiftCardTxn;
import com.coffee.system.mapper.GiftCardMapper;
import com.coffee.system.mapper.GiftCardTxnMapper;
import com.coffee.system.service.GiftCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 礼品卡业务实现
 */
@Service
public class GiftCardServiceImpl extends ServiceImpl<GiftCardMapper, GiftCard> implements GiftCardService {

    @Autowired
    private GiftCardTxnMapper giftCardTxnMapper;

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
    public GiftCard createForCurrent(BigDecimal amount, String name, String greeting, Integer validDays) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("礼品卡金额必须大于 0");
        }

        // 1. 构建礼品卡
        GiftCard card = new GiftCard();
        card.setCardNo(generateCardNo(userId));
        card.setMemberId(userId);
        card.setName(name != null ? name : "咖啡礼品卡");
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

    /**
     * 生成礼品卡卡号：日期前缀 + 用户ID 尾号 + 随机串
     */
    private String generateCardNo(Long userId) {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String userPart = userId == null ? "0000" : String.format("%04d", userId % 10000);
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "GC" + datePart + userPart + randomPart;
    }
}



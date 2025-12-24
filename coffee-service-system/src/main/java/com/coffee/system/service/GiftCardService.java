package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.GiftCard;
import com.coffee.system.domain.entity.GiftCardTxn;

import java.math.BigDecimal;

/**
 * 咖啡卡业务接口
 */
public interface GiftCardService extends IService<GiftCard> {

    /**
     * 分页查询当前登录用户的咖啡卡列表
     */
    Page<GiftCard> listCurrent(PageParam pageParam);

    /**
     * 获取当前用户所有咖啡卡的总余额
     * @return 总余额
     */
    BigDecimal getTotalBalance();

    /**
     * 为当前用户创建一张咖啡卡（发卡/购卡）
     *
     * @param amount     面值金额
     * @param name       咖啡卡名称
     * @param greeting   祝福语
     * @param validDays  有效天数（为空时采用默认值）
     */
    GiftCard createForCurrent(BigDecimal amount, String name, String greeting, Integer validDays);

    /**
     * 查询某张咖啡卡的交易明细（流水记录）
     * 用于前端展示"使用记录"，让用户知道余额是如何变化的
     *
     * @param cardId 咖啡卡ID
     * @param pageParam 分页参数
     * @return 交易明细列表
     */
    Page<GiftCardTxn> getTransactions(Long cardId, PageParam pageParam);

    /**
     * 创建咖啡卡订单（用于支付）
     * 先创建订单，支付成功后再创建咖啡卡
     *
     * @param amount 面值金额
     * @param name 咖啡卡名称
     * @param greeting 祝福语
     * @param validDays 有效天数
     * @return 创建的订单
     */
    com.coffee.system.domain.entity.OmsOrder createGiftCardOrder(BigDecimal amount, String name, String greeting, Integer validDays);

    /**
     * 根据订单创建并激活咖啡卡（支付成功后调用）
     *
     * @param orderId 订单ID
     * @return 创建的咖啡卡
     */
    GiftCard activateGiftCardByOrder(Long orderId);

    /**
     * 从咖啡卡余额中扣减（用于支付订单）
     *
     * @param cardId 咖啡卡ID
     * @param amount 扣减金额
     * @param orderId 关联订单ID
     */
    void deductBalance(Long cardId, BigDecimal amount, Long orderId);
}



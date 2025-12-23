package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.GiftCard;

import java.math.BigDecimal;

/**
 * 礼品卡业务接口
 */
public interface GiftCardService extends IService<GiftCard> {

    /**
     * 分页查询当前登录用户的礼品卡列表
     */
    Page<GiftCard> listCurrent(PageParam pageParam);

    /**
     * 为当前用户创建一张礼品卡（发卡/购卡）
     *
     * @param amount     面值金额
     * @param name       礼品卡名称
     * @param greeting   祝福语
     * @param validDays  有效天数（为空时采用默认值）
     */
    GiftCard createForCurrent(BigDecimal amount, String name, String greeting, Integer validDays);
}



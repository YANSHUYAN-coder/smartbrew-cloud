package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.system.domain.UmsMemberReceiveAddress;

import java.util.List;

public interface UmsMemberReceiveAddressService extends IService<UmsMemberReceiveAddress> {
    /**
     * 添加地址
     */
    boolean add(UmsMemberReceiveAddress address);

    /**
     * 修改地址
     */
    boolean updateAddress(UmsMemberReceiveAddress address);

    /**
     * 获取当前用户的地址列表
     */
    List<UmsMemberReceiveAddress> listCurrent(Long memberId);
}
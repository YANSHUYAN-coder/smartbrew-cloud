package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.UmsMemberReceiveAddress;

import java.util.List;

public interface UmsMemberReceiveAddressService extends IService<UmsMemberReceiveAddress> {
    /**
     * 添加地址
     */
    Result<String> addAddress(UmsMemberReceiveAddress address);

    /**
     * 修改地址
     */
    Result<String> updateAddress(UmsMemberReceiveAddress address);

    /**
     * 获取当前用户的地址列表
     */
    List<UmsMemberReceiveAddress> listCurrent();

    /**
     * 删除地址
     */
    Result<String> deleteAddress(Long memberId);
}
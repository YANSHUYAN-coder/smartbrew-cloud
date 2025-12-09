package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.system.domain.UmsMemberReceiveAddress;
import com.coffee.system.mapper.UmsMemberReceiveAddressMapper;
import com.coffee.system.service.UmsMemberReceiveAddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UmsMemberReceiveAddressServiceImpl extends ServiceImpl<UmsMemberReceiveAddressMapper, UmsMemberReceiveAddress> implements UmsMemberReceiveAddressService {

    @Override
    public List<UmsMemberReceiveAddress> listCurrent(Long memberId) {
        return lambdaQuery().eq(UmsMemberReceiveAddress::getMemberId, memberId).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(UmsMemberReceiveAddress address) {
        // 如果新添加的是默认地址，先把其他的置为非默认
        if (address.getDefaultStatus() == 1) {
            cancelDefaultStatus(address.getMemberId());
        }
        return save(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAddress(UmsMemberReceiveAddress address) {
        // 如果修改为默认地址，先把其他的置为非默认
        if (address.getDefaultStatus() != null && address.getDefaultStatus() == 1) {
            cancelDefaultStatus(address.getMemberId());
        }
        return updateById(address);
    }

    // 辅助方法：取消该用户的所有默认地址
    private void cancelDefaultStatus(Long memberId) {
        LambdaUpdateWrapper<UmsMemberReceiveAddress> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UmsMemberReceiveAddress::getMemberId, memberId)
                     .set(UmsMemberReceiveAddress::getDefaultStatus, 0);
        this.update(updateWrapper);
    }
}
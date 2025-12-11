package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.context.UserContext;
import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.UmsMemberReceiveAddress;
import com.coffee.system.mapper.UmsMemberReceiveAddressMapper;
import com.coffee.system.service.UmsMemberReceiveAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UmsMemberReceiveAddressServiceImpl extends ServiceImpl<UmsMemberReceiveAddressMapper, UmsMemberReceiveAddress> implements UmsMemberReceiveAddressService {

    @Autowired
    private UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    /**
     * 获取当前用户的地址列表
     */
    @Override
    public List<UmsMemberReceiveAddress> listCurrent() {
        Long userId = UserContext.getUserId();
        // 建议按 update_time 倒序，把最近修改的排前面
        return lambdaQuery()
                .eq(UmsMemberReceiveAddress::getMemberId, userId)
                .orderByDesc(UmsMemberReceiveAddress::getUpdateTime)
                .list();
    }

    /**
     * 添加地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addAddress(UmsMemberReceiveAddress address) {
        Long userId = UserContext.getUserId();
        address.setMemberId(userId);
        address.setId(null); // 【修复】强制设为 null，防止前端传脏ID

        // 如果新添加的是默认地址，先把其他的置为非默认
        if (address.getDefaultStatus() != null && address.getDefaultStatus() == 1) {
            cancelDefaultStatus(address.getMemberId());
        } else {
            // 如果前端没传，默认是非默认
            address.setDefaultStatus(0);
        }

        boolean flag = save(address);
        return flag ? Result.success("添加成功") : Result.failed("添加失败");
    }

    /**
     * 修改地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateAddress(UmsMemberReceiveAddress address) {
        Long currentUserId = UserContext.getUserId();

        // 安全检查
        UmsMemberReceiveAddress exist = this.getById(address.getId());
        if (exist == null) {
            return Result.failed("地址不存在");
        }
        if (!exist.getMemberId().equals(currentUserId)) {
            return Result.failed("非法操作");
        }

        address.setMemberId(currentUserId);

        // 处理默认地址互斥逻辑
        if (address.getDefaultStatus() != null && address.getDefaultStatus() == 1) {
            cancelDefaultStatus(address.getMemberId());
        }

        boolean flag = updateById(address);
        return flag ? Result.success("修改成功") : Result.failed("修改失败");
    }

    /**
     * 删除地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteAddress(Long addressId) {
        Long userId = UserContext.getUserId();
        UmsMemberReceiveAddress exist = this.getById(addressId);

        // 【修复】如果不存在，直接视为删除成功
        if (exist == null) {
            return Result.success("删除成功");
        }

        // 越权检查
        if (!exist.getMemberId().equals(userId)) {
            return Result.failed("非法操作");
        }

        boolean flag = this.removeById(addressId);
        return flag ? Result.success("删除成功") : Result.failed("删除失败");
    }

    // 辅助方法：取消该用户的所有默认地址
    private void cancelDefaultStatus(Long memberId) {
        LambdaUpdateWrapper<UmsMemberReceiveAddress> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UmsMemberReceiveAddress::getMemberId, memberId)
                .set(UmsMemberReceiveAddress::getDefaultStatus, 0);
        this.update(updateWrapper);
    }
}
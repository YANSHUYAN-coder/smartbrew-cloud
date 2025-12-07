package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.system.domain.UmsMember;

public interface UmsMemberService extends IService<UmsMember> {
    /**
     * 根据手机号获取用户
     * 
     * @param phone 手机号
     * @return 用户信息
     */
    UmsMember getByPhone(String phone);

    // 用户模块业务接口
}
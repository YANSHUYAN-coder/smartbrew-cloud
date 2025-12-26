package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.system.domain.entity.UmsMemberSignIn;
import com.coffee.system.domain.vo.SignInResultVO;

/**
 * 用户签到服务接口
 */
public interface UmsMemberSignInService extends IService<UmsMemberSignIn> {
    
    /**
     * 用户签到
     * @return 签到信息（包含奖励积分和连续签到天数）
     */
    SignInResultVO signIn();
    
    /**
     * 查询用户今日是否已签到
     * @return 签到记录，如果未签到返回null
     */
    UmsMemberSignIn getTodaySignIn();
}


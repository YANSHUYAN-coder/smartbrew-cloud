package com.coffee.app.controller;

import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.UmsMemberSignIn;
import com.coffee.system.domain.vo.SignInResultVO;
import com.coffee.system.service.UmsMemberSignInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户签到控制器
 */
@RestController
@RequestMapping("/app/signin")
public class AppSignInController {

    @Autowired
    private UmsMemberSignInService signInService;

    /**
     * 用户签到
     */
    @PostMapping("/sign")
    public Result<SignInResultVO> signIn() {
        return Result.success(signInService.signIn());
    }

    /**
     * 查询今日签到状态
     */
    @GetMapping("/today")
    public Result<UmsMemberSignIn> getTodaySignIn() {
        UmsMemberSignIn signIn = signInService.getTodaySignIn();
        return Result.success(signIn);
    }
}


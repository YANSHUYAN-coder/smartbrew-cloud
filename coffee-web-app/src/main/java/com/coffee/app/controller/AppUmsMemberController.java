package com.coffee.app.controller;

import com.coffee.common.dto.UpdatePasswordDTO;
import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.UmsMember;
import com.coffee.common.dto.UmsMemberUpdateDTO;
import com.coffee.system.service.UmsMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * C端用户个人中心接口
 */
@RestController
@RequestMapping("/app/member")
@AllArgsConstructor
@Tag(name = "C端-用户个人中心接口", description = "提供“我的”页面所需的个人信息、资料修改、密码修改与注销功能")
public class AppUmsMemberController {

    private final UmsMemberService memberService;

    /**
     * 1. 获取我的个人信息
     * 用于 APP "我的" 页面展示
     */
    @GetMapping("/info")
    @PreAuthorize("hasAuthority('ums:member:info')")
    @Operation(summary = "获取当前登录用户信息", description = "用于“我的”页面展示用户头像、昵称、等级等基础信息")
    public Result<UmsMember> info() {
        UmsMember userInfo = memberService.getUserInfo();
        return userInfo == null ? Result.failed("用户不存在") : Result.success(userInfo);
    }

    /**
     * 2. 修改个人资料
     */
    @PostMapping("/update")
    @Operation(summary = "修改个人资料", description = "支持修改昵称、头像等基础资料")
    public Result<String> updateInfo(@RequestBody UmsMemberUpdateDTO param) {
        boolean flag = memberService.updateUserInfo(param);
        return flag ? Result.success("修改成功") : Result.failed("修改失败");
    }

    /**
     * 3. 修改密码
     */
     @PostMapping("/updatePassword")
     @Operation(summary = "修改登录密码", description = "用户在安全设置中修改登录密码")
     public Result<String> updatePassword(@RequestBody UpdatePasswordDTO param) {
        boolean flag = memberService.updatePassword(param);
        return flag ? Result.success("修改成功") : Result.failed("修改失败");
    }

    /**
     * 4. 注销账号 (逻辑删除)
     * 用户主动点击“注销账号”
     */
    @PostMapping("/logoff")
    @Operation(summary = "注销账号", description = "将当前用户逻辑注销，通常用于“注销账号”功能入口")
    public Result<String> logoff() {
        boolean flag = memberService.logoff();
        return flag ? Result.success("账号已注销，江湖再见") : Result.failed("操作失败");
    }
}
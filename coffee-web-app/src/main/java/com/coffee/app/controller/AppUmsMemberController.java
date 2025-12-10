package com.coffee.app.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.coffee.common.context.UserContext;
import com.coffee.common.dto.UpdatePasswordDTO;
import com.coffee.common.result.Result;
import com.coffee.system.domain.UmsMember;
import com.coffee.system.domain.dto.UmsMemberUpdateDTO;
import com.coffee.system.service.UmsMemberService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * C端用户个人中心接口
 */
@RestController
@RequestMapping("/app/member")
@AllArgsConstructor
public class AppUmsMemberController {

    private final UmsMemberService memberService;

    /**
     * 1. 获取我的个人信息
     * 用于 APP "我的" 页面展示
     */
    @GetMapping("/info")
    @PreAuthorize("hasAuthority('ums:member:info')")
    public Result<UmsMember> info() {
        UmsMember userInfo = memberService.getUserInfo();
        return userInfo == null ? Result.failed("用户不存在") : Result.success(userInfo);
    }

    /**
     * 2. 修改个人资料
     */
    @PostMapping("/update")
    public Result<String> updateInfo(@RequestBody UmsMemberUpdateDTO param) {
        boolean flag = memberService.updateUserInfo(param);
        return flag ? Result.success("修改成功") : Result.failed("修改失败");
    }

    /**
     * 3. 修改密码
     */
     @PostMapping("/updatePassword")
     public Result<String> updatePassword(@RequestBody UpdatePasswordDTO param) {
        boolean flag = memberService.updatePassword(param);
        return flag ? Result.success("修改成功") : Result.failed("修改失败");
    }

    /**
     * 4. 注销账号 (逻辑删除)
     * 用户主动点击“注销账号”
     */
    @PostMapping("/logoff")
    public Result<String> logoff() {
        boolean flag = memberService.logoff();
        return flag ? Result.success("账号已注销，江湖再见") : Result.failed("操作失败");
    }
}
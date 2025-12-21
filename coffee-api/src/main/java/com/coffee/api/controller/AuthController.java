package com.coffee.api.controller;

import com.coffee.common.result.Result;
import com.coffee.common.util.JwtUtil;
import com.coffee.system.domain.entity.UmsMember;
import com.coffee.system.service.UmsMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证接口", description = "提供用户注册、登录、退出登录能力")
public class AuthController {
    @Autowired
    private UmsMemberService umsMemberService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口，返回用户信息及 JWT Token")
    public Result<Map<String, Object>> register(@RequestBody UmsMember member) {
        // 检查手机号是否已存在
        UmsMember existingMember = umsMemberService.getByPhone(member.getPhone());
        if (existingMember != null) {
            return Result.failed("该手机号已被注册");
        }

        // 密码加密
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        
        // 保存用户
        boolean saved = umsMemberService.save(member);
        if (!saved) {
            return Result.failed("注册失败");
        }

        // 生成JWT Token
        String token = jwtUtil.generateToken(member.getId(),member.getPhone());
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", member);
        
        return Result.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口，返回用户信息及 JWT Token")
    public Result<Map<String, Object>> login(@RequestBody UmsMember loginRequest) {
        try {
            // 使用手机号和密码进行认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getPhone(), loginRequest.getPassword())
            );
            
            // 认证成功后生成JWT Token
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UmsMember member = umsMemberService.getByPhone(loginRequest.getPhone());
            String token = jwtUtil.generateToken(member.getId(),member.getPhone());
            
            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", member);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.failed("登录失败，用户名或密码错误");
        }
    }

    /**
     * 退出登录
     * * JWT 退出登录说明：
     * 1. 服务端：清除 Spring Security 上下文。
     * 2. 客户端：接收到成功返回后，务必删除本地存储的 Token。
     * * 进阶实现（可选）：
     * 如果需要防止 Token 在过期前被再次使用（例如被盗用），
     * 可以在此处将 Token 加入 Redis 的黑名单中，并在 JwtAuthenticationTokenFilter 中校验。
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "用户退出登录接口")
    public Result<String> logout() {
        // 清除当前线程的认证信息
        SecurityContextHolder.clearContext();

        // 返回成功提示，前端需据此删除本地 Token
        return Result.success("退出成功");
    }
}
package com.coffee.api.controller;

import com.coffee.common.result.Result;
import com.coffee.common.util.JwtUtil;
import com.coffee.system.domain.entity.UmsMember;
import com.coffee.system.service.UmsMemberService;
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
}
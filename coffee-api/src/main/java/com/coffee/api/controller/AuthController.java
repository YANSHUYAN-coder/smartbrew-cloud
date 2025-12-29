package com.coffee.api.controller;

import com.coffee.common.constant.AuthConstants;
import com.coffee.common.result.Result;
import com.coffee.common.util.JwtUtil;
import com.coffee.system.domain.entity.UmsMember;
import com.coffee.system.service.UmsMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证接口", description = "提供用户注册、登录、退出登录能力")
@Slf4j
public class AuthController {
    @Autowired
    private UmsMemberService umsMemberService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private StringRedisTemplate redisTemplate;

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
        String accessToken = jwtUtil.generateAccessToken(member.getId(),member.getPhone());
        String refreshToken = jwtUtil.generateRefreshToken(member.getId(),member.getPhone());

        String redisKey = AuthConstants.REFRESH_TOKEN_PREFIX + member.getPhone();
        redisTemplate.opsForValue().set(redisKey, refreshToken, AuthConstants.REFRESH_EXPIRATION, TimeUnit.MILLISECONDS);
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", accessToken);
        result.put("refreshToken", refreshToken);
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
            String accessToken = jwtUtil.generateAccessToken(member.getId(),member.getPhone());
            String refreshToken = jwtUtil.generateRefreshToken(member.getId(),member.getPhone());

            String redisKey = AuthConstants.REFRESH_TOKEN_PREFIX + member.getPhone();
            redisTemplate.opsForValue().set(redisKey, refreshToken, AuthConstants.REFRESH_EXPIRATION, TimeUnit.MILLISECONDS);
            
            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("token", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("user", member);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.failed("登录失败，用户名或密码错误");
        }
    }


    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public Result<Map<String, String>> refreshToken(@RequestBody Map<String, String> params) {
        String refreshToken = params.get("refreshToken");

        if (refreshToken == null) {
            return Result.failed("Refresh Token 不能为空");
        }

        try {
            // 1. 校验 Token 格式和签名
            String phone  = jwtUtil.getPhoneFromToken(refreshToken);
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            if (phone == null || !jwtUtil.validateToken(refreshToken) || userId == null) {
                return Result.failed("Refresh Token 无效或已过期");
            }

            // 2. 校验 Redis 中的 Token (防止 Token 被撤销后仍能使用)
            String redisKey = AuthConstants.REFRESH_TOKEN_PREFIX + phone;
            String savedToken = redisTemplate.opsForValue().get(redisKey);

            if (savedToken == null || !savedToken.equals(refreshToken)) {
                return Result.failed("Refresh Token 已失效，请重新登录");
            }

            // 3. 生成新的 Access Token
            String newAccessToken = jwtUtil.generateAccessToken(userId,phone);

            // 4. (可选) 刷新 Redis 中 Refresh Token 的过期时间
            redisTemplate.expire(redisKey, 7, TimeUnit.DAYS);

            Map<String, String> result = new HashMap<>();
            result.put("token", newAccessToken);
            // 这里可以决定是否同时返回新的 refreshToken (轮换机制)，暂时只返回 accessToken

            return Result.success(result);

        } catch (Exception e) {
            log.error("刷新令牌失败", e);
            return Result.failed("刷新令牌失败: " + e.getMessage());
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
        // 1. 获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getName() != null) {
            // Spring Security 中，Principal 的 Name 通常是我们在 UserDetails 中设定的 username (这里是手机号)
            String phone = authentication.getName();

            // 2. 从 Redis 删除 Refresh Token
            // 务必确保这里的 Key 生成规则与 login/register 接口中保持一致！
            // 你的规则是: "auth:refresh_token:" + phone
            String redisKey = AuthConstants.REFRESH_TOKEN_PREFIX + phone;
            Boolean deleted = redisTemplate.delete(redisKey);

            if (Boolean.TRUE.equals(deleted)) {
                log.info("用户退出登录，已清除 Refresh Token: {}", phone);
            } else {
                log.warn("用户退出登录，但 Redis 中未找到 Token 或删除失败: {}", phone);
            }
        }

        // 3. 清除当前线程的认证信息
        SecurityContextHolder.clearContext();

        // 4. 返回成功提示，前端需据此删除本地 Token
        return Result.success("退出成功");
    }
}
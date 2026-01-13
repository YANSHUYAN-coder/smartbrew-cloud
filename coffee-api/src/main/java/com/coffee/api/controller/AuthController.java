package com.coffee.api.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffee.common.constant.AuthConstants;
import com.coffee.common.dto.MobileLoginDTO;
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

        // 设置会员等级
        member.setLevelId(1L);

        // 设置默认用户名和昵称
        member.setUsername(member.getPhone());
        member.setNickname(member.getPhone());
        
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

            // 3. 生成新的 Access Token 和新的 Refresh Token (实现轮换机制)
            String newAccessToken = jwtUtil.generateAccessToken(userId, phone);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId, phone);

            // 4. 更新 Redis 中的 Refresh Token (替换旧的，并重新设置 7 天有效期)
            redisTemplate.opsForValue().set(redisKey, newRefreshToken, 7, TimeUnit.DAYS);

            Map<String, String> result = new HashMap<>();
            result.put("token", newAccessToken);
            result.put("refreshToken", newRefreshToken); // 返回新的刷新令牌

            log.info("用户 {} 成功刷新令牌", phone);
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

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sendCode")
    public Result<String> sendCode(@RequestParam(name = "mobile") String mobile) {
        // 1. 简单校验手机号
        if (StrUtil.isBlank(mobile) || mobile.length() != 11) {
            return Result.failed("请输入正确的11位手机号");
        }

        // 2. 生成6位随机数字
        String code = RandomUtil.randomNumbers(6);

        // 3. 存入 Redis，有效期 5 分钟
        // Key 格式建议统一管理，这里为了演示直接写字符串
        String redisKey = "sms:code:" + mobile;
        redisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);

        // 4. 【模拟发送】实际项目中请对接阿里云/腾讯云短信服务
        log.info("==========> 手机号 [{}] 的验证码是：{}", mobile, code);

        return Result.success("验证码发送成功");
    }

    @Operation(summary = "手机号验证码登录(自动注册)")
    @PostMapping("/loginByMobile")
    public Result<Map<String, Object>> loginByMobile(@RequestBody MobileLoginDTO loginDTO) {
        String mobile = loginDTO.getMobile();
        String code = loginDTO.getCode();

        // 1. 校验参数
        if (StrUtil.hasBlank(mobile, code)) {
            return Result.failed("手机号和验证码不能为空");
        }

        // 2. 校验验证码
        String redisKey = "sms:code:" + mobile;
        String cacheCode = redisTemplate.opsForValue().get(redisKey);

        if (cacheCode == null || !cacheCode.equals(code)) {
            return Result.failed("验证码错误或已失效");
        }

        // 3. 验证通过，删除验证码（防止重复使用）
        redisTemplate.delete(redisKey);

        // 4. 查询用户，不存在则注册
        UmsMember member = umsMemberService.getOne(new LambdaQueryWrapper<UmsMember>()
                .eq(UmsMember::getPhone, mobile));

        if (member == null) {
            member = new UmsMember();
            member.setPhone(mobile);
            member.setUsername(mobile); // 默认账号为手机号
            member.setNickname("用户" + mobile.substring(7)); // 默认昵称
            member.setStatus(1); // 启用状态
            member.setIntegration(0);
            member.setGrowth(0);
            umsMemberService.save(member);
            log.info("手机号 {} 新用户自动注册成功", mobile);
        } else {
            if (member.getStatus() == 0) {
                return Result.failed("账号已被禁用，请联系客服");
            }
        }

        // 5. 生成 Token (使用 JwtUtil)
        String accessToken = jwtUtil.generateAccessToken(member.getId(), member.getPhone());
        String refreshToken = jwtUtil.generateRefreshToken(member.getId(), member.getPhone());

        // 6. 保存 Refresh Token 到 Redis
        String tokenRedisKey = AuthConstants.REFRESH_TOKEN_PREFIX + member.getPhone();
        redisTemplate.opsForValue().set(tokenRedisKey, refreshToken, AuthConstants.REFRESH_EXPIRATION, TimeUnit.MILLISECONDS);

        // 7. 返回登录信息
        Map<String, Object> data = new HashMap<>();
        data.put("token", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("userInfo", member);

        return Result.success(data);
    }
}
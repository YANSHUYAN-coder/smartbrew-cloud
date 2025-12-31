package com.coffee.security.config;

import com.coffee.security.filter.JwtAuthenticationFilter;
import com.coffee.security.handler.AccessDeniedHandlerImpl;
import com.coffee.security.handler.UnauthorizedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UnauthorizedHandler unauthorizedHandler;
    @Autowired
    private AccessDeniedHandlerImpl accessDeniedHandler;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF (对支付宝回调必须禁用，你这里已经禁用了，很好)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. 放行登录注册
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        // 2. 放行错误页面
                        .requestMatchers("/error").permitAll()

                        // ================== 【核心修改】 ==================
                        // 3. 放行支付宝回调接口
                        // 原因：支付宝服务器发送的回调请求不包含 JWT Token，
                        // 如果不放行，会被 Security 拦截并返回 401/403，导致回调失败。
                        // 路径要和你 Controller 里的完全一致：/app/pay + /notify
                        .requestMatchers("/app/pay/notify").permitAll()
                        // ================================================

                        // 3.5. 放行图片代理接口（无需认证，允许直接访问）
                        .requestMatchers("/app/image/**").permitAll()
                        // 3.6. 放行门店公开信息接口
                        .requestMatchers("/app/store/**").permitAll()

                        // 4. 放行 Knife4j / Swagger 静态资源
                        .requestMatchers(
                                "/doc.html",
                                "/webjars/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/favicon.ico"
                        ).permitAll()

                        // 5. 其他接口需要认证
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        // 允许跨域
        http.cors(Customizer.withDefaults());
        return http.build();
    }
}
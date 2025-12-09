package com.coffee.security.filter;

import com.coffee.common.constant.AuthConstants;
import com.coffee.common.context.UserContext;
import com.coffee.common.util.JwtUtil;
import com.coffee.security.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(AuthConstants.AUTH_HEADER);

        try {
            if (authHeader != null && authHeader.startsWith(AuthConstants.TOKEN_PREFIX)) {
                String token = authHeader.substring(7);

                // 1. 验证 Token 是否有效
                if (jwtUtil.validateToken(token)) {
                    // 2. 【新增】将 UserID 放入 UserContext (ThreadLocal)
                    // 这样后续的 Controller/Service 就可以直接用了
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    UserContext.setUserId(userId);

                    // 3. 原有的 Spring Security 认证逻辑 (加载权限等)
                    // 注意：这里我们用 phone 来查 UserDetails，保持逻辑不变
                    String phone = jwtUtil.getPhoneFromToken(token);
                    if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(phone);

                        if (userDetails != null) {
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        }
                    }
                }
            }
            // 放行请求
            filterChain.doFilter(request, response);

        } finally {
            // 【关键】请求结束（无论成功失败），必须清理 ThreadLocal，防止内存泄漏和数据污染
            UserContext.remove();
        }
    }
}
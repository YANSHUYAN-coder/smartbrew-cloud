package com.coffee.security.service;

import com.coffee.system.domain.UmsMember;
import com.coffee.system.domain.UmsRole;
import com.coffee.system.service.UmsMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义用户详情服务
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UmsMemberService umsMemberService;


    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        // 根据手机号查找用户
        UmsMember member = umsMemberService.getByPhone(phone);
        
        if (member == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        
        // 检查用户状态
        if (member.getStatus() != null && member.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用");
        }

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        
        // 返回UserDetails对象
        return new User(
            member.getUsername(),
            member.getPassword(), 
            true,  // 账户是否可用
            true,  // 账户是否过期
            true,  // 凭证是否过期
            true,  // 账户是否锁定
            authorities
        );
    }
}
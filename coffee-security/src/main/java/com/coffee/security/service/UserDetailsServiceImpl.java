package com.coffee.security.service;

import com.coffee.system.domain.entity.UmsMember;
import com.coffee.system.mapper.UmsPermissionMapper;
import com.coffee.system.service.UmsMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义用户详情服务
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UmsMemberService umsMemberService;
    @Autowired
    private UmsPermissionMapper umsPermissionMapper;


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

        List<String> permissions = umsPermissionMapper.selectPermsByUserId(member.getId());
        log.info("用户权限：{}", permissions);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (permissions != null) {
            authorities = permissions.stream()
                    .filter(perm -> perm != null && !perm.isEmpty()) // 过滤掉空字符串
                    .map(SimpleGrantedAuthority::new)                // 封装成 Authority 对象
                    .collect(Collectors.toList());
        }
        
        // 返回UserDetails对象
        return new User(
            member.getPhone(),
            member.getPassword() != null ? member.getPassword() : "", 
            true,  // 账户是否可用
            true,  // 账户是否过期
            true,  // 凭证是否过期
            true,  // 账户是否锁定
            authorities
        );
    }
}
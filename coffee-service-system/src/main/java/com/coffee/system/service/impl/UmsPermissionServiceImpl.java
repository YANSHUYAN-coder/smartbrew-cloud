package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.system.domain.entity.UmsPermission;
import com.coffee.system.mapper.UmsPermissionMapper;
import com.coffee.system.service.UmsPermissionService;
import org.springframework.stereotype.Service;

@Service
public class UmsPermissionServiceImpl extends ServiceImpl<UmsPermissionMapper, UmsPermission> implements UmsPermissionService {
}


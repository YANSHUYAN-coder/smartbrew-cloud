package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.system.domain.entity.OmsOrderItem;
import com.coffee.system.mapper.OrderItemMapper;
import com.coffee.system.service.OrderItemService;
import org.springframework.stereotype.Service;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OmsOrderItem> implements OrderItemService {
}

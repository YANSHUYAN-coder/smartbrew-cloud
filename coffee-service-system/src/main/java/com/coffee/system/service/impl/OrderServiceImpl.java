package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.context.UserContext;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.OmsOrder;
import com.coffee.system.domain.entity.OmsOrderItem;
import com.coffee.system.domain.vo.OrderVO;
import com.coffee.system.mapper.OmsOrderMapper;
import com.coffee.system.mapper.OrderItemMapper;
import com.coffee.system.service.OrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements OrderService {
    @Autowired
    private OmsOrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public Page<OmsOrder> getAllList(PageParam pageParam, Integer status) {
        Page<OmsOrder> orderPage = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(OmsOrder::getStatus, status);
        }
        wrapper.orderByDesc(OmsOrder::getCreateTime);
        return orderMapper.selectPage(orderPage, wrapper);
    }


    @Override
    public OrderVO getDetail(Long id) {
        OmsOrder omsOrder = this.getById(id);
        // TODO: 查询 OmsOrderItem
        List<OmsOrderItem> omsOrderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>()
                        .eq(OmsOrderItem::getOrderId, id));
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(omsOrder, orderVO);
        orderVO.setOrderItemList(omsOrderItems);
        return orderVO;
    }

    @Override
    public boolean updateStatus(Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        OmsOrder order = new OmsOrder();
        order.setId(id);
        order.setStatus(status);
        return this.update(new LambdaUpdateWrapper<OmsOrder>()
                .eq(OmsOrder::getId, id)
                .set(OmsOrder::getStatus, status));
    }

    @Override
    public Page<OmsOrder> listCurrent(PageParam pageParam, Integer status) {
        Long userId = UserContext.getUserId();
        Page<OmsOrder> orderPage = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<OmsOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsOrder::getMemberId, userId);
        if (status != null) {
            wrapper.eq(OmsOrder::getStatus, status);
        }
        wrapper.orderByDesc(OmsOrder::getCreateTime);
        return orderMapper.selectPage(orderPage, wrapper);
    }
}


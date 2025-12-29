package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.common.dto.CreateOrderRequest;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.OmsOrder;
import com.coffee.system.domain.vo.OrderVO;

import java.util.Map;

public interface OrderService extends IService<OmsOrder> {
    /*管理端业务*/

    /**
     * 管理端获取订单列表（包含商品明细）
     */
    Page<OrderVO> getAllList(PageParam pageParam, Integer status);


    /**
     * 获取订单详情（包含商品明细）
     * (简化版，暂时只返回订单信息，明细需关联查询)
     */
    OrderVO getDetail(Long id);

    /**
     * 修改订单状态
     */
    boolean updateStatus(Map<String, Object> params);

    /*用户端业务*/

    /**
     * 用户端获取用户自己的订单列表（包含商品明细）
     */
    Page<OrderVO> listCurrent(PageParam pageParam, Integer status);

    /**
     * C 端创建订单
     * 根据前端传入的收货地址、订单金额和商品明细生成订单及订单项
     */
    OmsOrder createOrder(CreateOrderRequest request);
    
    /**
     * 咖啡卡支付
     * @param orderId 订单ID
     * @return 支付是否成功
     */
    boolean payByCoffeeCard(Long orderId);

    boolean confirm(Long id);

    boolean cancel(Map<String, Object> params);
}


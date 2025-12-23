package com.coffee.system.domain.vo;


import com.coffee.system.domain.entity.OmsOrder;
import com.coffee.system.domain.entity.OmsOrderItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * APP端订单展示对象 (聚合视图)
 * 继承 OmsOrder -> 包含订单基础信息 (订单号、状态、实付金额)
 * 聚合 List<OmsOrderItem> -> 包含商品信息 (图片、名字、规格、数量)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderVO extends OmsOrder {
    private String orderSn;

    // 新增：前端展示用的取餐码
    private String pickupCode;

    /**
     * 订单内的商品明细列表
     * 前端遍历这个列表来展示：
     * - 商品图片 (productPic)
     * - 商品名称 (productName)
     * - 规格属性 (productAttr) -> 例如: [{"key":"规格","value":"大杯"},{"key":"糖度","value":"半糖"}]
     * - 购买数量 (productQuantity)
     * - 当时价格 (productPrice)
     */
    private List<OmsOrderItem> orderItemList;
    
    // 您还可以扩展其他前端专用的展示字段，比如：
    // private String statusText; // "待制作" (后端把 int status 转成中文)
    // private Long remainingTime; // 剩余支付时间 (秒)
}
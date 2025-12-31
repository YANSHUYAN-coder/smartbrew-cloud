package com.coffee.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 门店配置实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("oms_store")
public class OmsStore extends BaseEntity {


    private String name;
    private String address;
    private String phone;
    private Double longitude;
    private Double latitude;

    /**
     * 营业状态：0->休息；1->营业
     */
    private Integer openStatus;

    /**
     * 外送状态：0->关闭；1->开启
     */
    private Integer deliveryStatus;

    private String businessHours;
    private Integer deliveryRadius;
    private BigDecimal baseDeliveryFee;
}


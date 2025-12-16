package com.coffee.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_product") // 对应数据库表名
public class Product extends BaseEntity {

    /**
     * 商品名称
     */
    private String name;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 分类ID (关联 pms_category 表)
     */
    private Long categoryId;

    /**
     * 描述
     */
    private String description;

    /**
     * 图片地址 (对应数据库 pic_url)
     */
    private String picUrl;

    /**
     * 销量 (修复：新增字段)
     */
    private Integer sales;

    /**
     * 状态: 1-上架, 0-下架
     */
    private Integer status;
}
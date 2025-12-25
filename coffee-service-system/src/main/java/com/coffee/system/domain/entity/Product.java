package com.coffee.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
     * 分类名称 (不映射到数据库，用于返回给前端)
     */
    @TableField(exist = false)
    private String category;

    /**
     * 描述
     */
    private String description;

    /**
     * 图片地址 (对应数据库 pic_url)
     */
    private String picUrl;

    /**
     * 销量
     */
    private Integer sales;

    /**
     * 是否新品: 0->否；1->是
     */
    private Integer newStatus;

    /**
     * 是否推荐: 0->否；1->是
     */
    private Integer recommendStatus;

    /**
     * 状态: 1-上架, 0-下架
     */
    private Integer status;
}
package com.coffee.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_category")
public class Category extends BaseEntity {

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 排序权重, 值越大越靠前
     */
    private Integer sort;

    /**
     * 状态: 1-启用, 0-禁用
     */
    private Integer status;
}


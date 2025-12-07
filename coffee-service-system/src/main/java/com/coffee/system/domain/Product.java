package com.coffee.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pms_product") // 对应数据库表名
public class Product extends BaseEntity{
    private String name;
    private BigDecimal price;
    private String category;
    private String description;
    private String picUrl;
    private Integer status; // 1-上架, 0-下架
}
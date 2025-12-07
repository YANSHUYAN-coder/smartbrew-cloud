package com.coffee.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ums_role")
public class UmsRole extends BaseEntity {
    private String name;
    private String description;
    private Integer status;
}
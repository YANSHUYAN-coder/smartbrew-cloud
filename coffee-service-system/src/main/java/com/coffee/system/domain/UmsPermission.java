package com.coffee.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ums_permission")
public class UmsPermission extends BaseEntity {
    private String name;
    private String description;
    private String url;
    private String method;
}
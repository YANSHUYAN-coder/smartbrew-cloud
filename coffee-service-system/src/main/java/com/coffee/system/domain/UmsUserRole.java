package com.coffee.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ums_user_role")
public class UmsUserRole extends BaseEntity {
    private Long userId;
    private Long roleId;
}
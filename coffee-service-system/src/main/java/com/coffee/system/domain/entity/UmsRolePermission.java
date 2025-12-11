package com.coffee.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ums_role_permission")
public class UmsRolePermission extends BaseEntity {
    private Long roleId;
    private Long permissionId;
}
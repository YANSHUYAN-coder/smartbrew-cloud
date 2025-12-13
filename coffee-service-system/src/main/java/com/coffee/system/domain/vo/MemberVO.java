package com.coffee.system.domain.vo;

import com.coffee.system.domain.entity.UmsMember;
import com.coffee.system.domain.entity.UmsMemberLevel;
import com.coffee.system.domain.entity.UmsRole;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 会员信息VO，包含角色列表和等级信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberVO extends UmsMember {
    /**
     * 用户角色列表
     */
    private List<UmsRole> roles;
    
    /**
     * 会员等级信息
     */
    private UmsMemberLevel level;
}


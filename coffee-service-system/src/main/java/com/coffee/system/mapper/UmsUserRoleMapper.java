package com.coffee.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coffee.system.domain.entity.UmsRole;
import com.coffee.system.domain.entity.UmsUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UmsUserRoleMapper extends BaseMapper<UmsUserRole> {
    
    /**
     * 根据用户ID查询用户的所有角色
     */
    @Select("SELECT r.* FROM ums_role r " +
            "INNER JOIN ums_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<UmsRole> selectRolesByUserId(Long userId);
}
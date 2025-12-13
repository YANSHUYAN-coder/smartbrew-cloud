package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.common.dto.MemberStatusDTO;
import com.coffee.common.dto.PageParam;
import com.coffee.common.dto.UpdatePasswordDTO;
import com.coffee.system.domain.entity.UmsMember;
import com.coffee.system.domain.vo.MemberVO;
import com.coffee.common.dto.UmsMemberUpdateDTO;

import java.util.List;

public interface UmsMemberService extends IService<UmsMember> {
    /**
     * 分页查询会员列表
     */
    Page<UmsMember> getList(PageParam pageParam, String phone);
    
    /**
     * 分页查询会员列表（包含角色信息）
     */
    Page<MemberVO> getListWithRoles(PageParam pageParam, String phone);
    
    /**
     * 根据手机号获取用户
     * 
     * @param phone 手机号
     * @return 用户信息
     */
    UmsMember getByPhone(String phone);

    /**
     * 修改用户状态
     * @param dto
     * @return
     */
    boolean updateStatus(MemberStatusDTO dto);

    // 用户模块业务接口

    /**
     * 获取用户信息
     * @return
     */
    UmsMember getUserInfo();

    /**
     * 修改用户信息
     * @param param
     * @return
     */
    boolean updateUserInfo(UmsMemberUpdateDTO param);

    /**
     * 修改密码
     * @param param 包含旧密码和新密码
     * @return 是否成功
     */
    boolean updatePassword(UpdatePasswordDTO param);

    /**
     * 注销账号
     * @return
     */
    boolean logoff();

    /**
     * 分配角色给用户
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    boolean allocRoles(Long userId, List<Long> roleIds);
}
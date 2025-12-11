package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.common.dto.MemberStatusDTO;
import com.coffee.common.dto.UpdatePasswordDTO;
import com.coffee.system.domain.entity.UmsMember;
import com.coffee.common.dto.UmsMemberUpdateDTO;

public interface UmsMemberService extends IService<UmsMember> {
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
}
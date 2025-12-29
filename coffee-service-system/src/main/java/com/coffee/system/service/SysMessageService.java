package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.system.domain.entity.SysMessage;

public interface SysMessageService extends IService<SysMessage> {
    /**
     * 发送系统消息
     * @param userId 用户ID
     * @param title 标题
     * @param content 内容
     * @param type 类型
     * @param bizId 业务ID
     */
    void sendMessage(Long userId, String title, String content, Integer type, Long bizId);
}
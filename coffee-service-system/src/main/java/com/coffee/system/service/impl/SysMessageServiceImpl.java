package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.system.domain.entity.SysMessage;
import com.coffee.system.mapper.SysMessageMapper;
import com.coffee.system.service.SysMessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {

    @Override
    public void sendMessage(Long userId, String title, String content, Integer type, Long bizId) {
        SysMessage message = new SysMessage();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setBizId(bizId);
        message.setIsRead(0); // 默认未读
        message.setCreateTime(LocalDateTime.now());
        
        this.save(message);
    }
}
package com.coffee.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.common.context.UserContext;
import com.coffee.common.dto.PageParam;
import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.SysMessage;
import com.coffee.system.service.SysMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "App-消息中心")
@RestController
@RequestMapping("/app/message")
@RequiredArgsConstructor
public class AppMessageController {

    private final SysMessageService sysMessageService;

    @Operation(summary = "获取消息列表")
    @GetMapping("/list")
    public Result<Page<SysMessage>> list(PageParam pageParam) {
        Long userId = UserContext.getUserId();
        Page<SysMessage> page = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessage::getUserId, userId);
        wrapper.orderByDesc(SysMessage::getCreateTime); // 最新消息在前
        
        return Result.success(sysMessageService.page(page, wrapper));
    }

    @Operation(summary = "获取未读消息数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        Long userId = UserContext.getUserId();
        long count = sysMessageService.count(new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getUserId, userId)
                .eq(SysMessage::getIsRead, 0));
        return Result.success(count);
    }

    @Operation(summary = "标记消息为已读")
    @PostMapping("/read")
    public Result<Boolean> markAsRead(@RequestBody SysMessage message) {
        // 如果传了ID，标记单条；没传ID，标记当前用户所有
        Long userId = UserContext.getUserId();
        LambdaUpdateWrapper<SysMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SysMessage::getIsRead, 1)
                     .eq(SysMessage::getUserId, userId);
        
        if (message.getId() != null) {
            updateWrapper.eq(SysMessage::getId, message.getId());
        } else {
            // 标记全部已读时，只更新未读的
            updateWrapper.eq(SysMessage::getIsRead, 0);
        }
        
        return Result.success(sysMessageService.update(updateWrapper));
    }
}
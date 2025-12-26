package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.context.UserContext;
import com.coffee.system.domain.entity.UmsMember;
import com.coffee.system.domain.entity.UmsMemberSignIn;
import com.coffee.system.domain.vo.SignInResultVO;
import com.coffee.system.mapper.UmsMemberMapper;
import com.coffee.system.mapper.UmsMemberSignInMapper;
import com.coffee.system.service.UmsMemberSignInService;
import com.coffee.system.service.UmsMemberIntegrationHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户签到服务实现
 */
@Slf4j
@Service
public class UmsMemberSignInServiceImpl extends ServiceImpl<UmsMemberSignInMapper, UmsMemberSignIn> 
        implements UmsMemberSignInService {

    @Autowired
    private UmsMemberMapper memberMapper;
    
    @Autowired
    private UmsMemberIntegrationHistoryService integrationHistoryService;
    
    // 每日签到奖励积分（可根据连续签到天数调整）
    private static final int BASE_POINTS = 10; // 基础积分
    private static final int CONTINUOUS_BONUS = 5; // 连续签到额外奖励（每连续7天额外5积分）

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignInResultVO signIn() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        LocalDate today = LocalDate.now();
        
        // 检查今日是否已签到（使用MyBatis-Plus）
        UmsMemberSignIn todaySignIn = this.getOne(
            new LambdaQueryWrapper<UmsMemberSignIn>()
                .eq(UmsMemberSignIn::getMemberId, userId)
                .eq(UmsMemberSignIn::getSignDate, today)
        );
        if (todaySignIn != null) {
            throw new RuntimeException("今日已签到，请明天再来");
        }

        // 查询最近一次签到日期（使用MyBatis-Plus）
        UmsMemberSignIn lastSignIn = this.getOne(
            new LambdaQueryWrapper<UmsMemberSignIn>()
                .eq(UmsMemberSignIn::getMemberId, userId)
                .orderByDesc(UmsMemberSignIn::getSignDate)
                .last("LIMIT 1")
        );
        LocalDate lastSignDate = lastSignIn != null ? lastSignIn.getSignDate() : null;
        
        // 计算连续签到天数
        int continuousDays = 1;
        boolean isFirstTime = (lastSignDate == null);
        
        if (!isFirstTime) {
            LocalDate yesterday = today.minusDays(1);
            if (lastSignDate.equals(yesterday)) {
                // 昨天签到了，连续签到（使用已查询的lastSignIn）
                continuousDays = (lastSignIn != null ? lastSignIn.getContinuousDays() : 0) + 1;
            } else {
                // 昨天没签到，重新开始
                continuousDays = 1;
            }
        }

        // 计算奖励积分（基础积分 + 连续签到奖励）
        int points = BASE_POINTS;
        if (continuousDays >= 7) {
            // 连续7天及以上，额外奖励
            int bonusDays = (continuousDays / 7) * 7; // 每7天一个周期
            points += (bonusDays / 7) * CONTINUOUS_BONUS;
        }

        // 创建签到记录
        UmsMemberSignIn signIn = new UmsMemberSignIn();
        signIn.setMemberId(userId);
        signIn.setSignDate(today);
        signIn.setPoints(points);
        signIn.setContinuousDays(continuousDays);
        signIn.setCreateTime(LocalDateTime.now());
        this.save(signIn);

        // 更新用户积分
        UmsMember member = memberMapper.selectById(userId);
        if (member != null) {
            int currentIntegration = member.getIntegration() != null ? member.getIntegration() : 0;
            memberMapper.update(null, new LambdaUpdateWrapper<UmsMember>()
                    .eq(UmsMember::getId, userId)
                    .set(UmsMember::getIntegration, currentIntegration + points));
            log.info("用户签到成功，用户ID: {}, 奖励积分: {}, 连续签到: {}天", userId, points, continuousDays);
            
            // 记录积分明细
            integrationHistoryService.recordIntegrationChange(
                userId, 
                1, // 1->签到获得
                points, 
                "sign_in", 
                signIn.getId(), 
                String.format("签到获得，连续%d天", continuousDays)
            );
        }

        return new SignInResultVO(points, continuousDays, isFirstTime);
    }

    @Override
    public UmsMemberSignIn getTodaySignIn() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return null;
        }
        
        LocalDate today = LocalDate.now();
        // 使用MyBatis-Plus查询
        return this.getOne(
            new LambdaQueryWrapper<UmsMemberSignIn>()
                .eq(UmsMemberSignIn::getMemberId, userId)
                .eq(UmsMemberSignIn::getSignDate, today)
        );
    }
}


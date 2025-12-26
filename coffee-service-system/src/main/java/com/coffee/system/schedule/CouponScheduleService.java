package com.coffee.system.schedule;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.coffee.system.domain.entity.SmsCoupon;
import com.coffee.system.domain.entity.SmsCouponHistory;
import com.coffee.system.mapper.SmsCouponMapper;
import com.coffee.system.mapper.SmsCouponHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 优惠券定时任务服务
 * 用于每日凌晨刷新优惠券库存和更新过期优惠券状态
 */
@Slf4j
@Service
public class CouponScheduleService {

    @Autowired
    private SmsCouponMapper couponMapper;
    
    @Autowired
    private SmsCouponHistoryMapper couponHistoryMapper;

    /**
     * 每日凌晨0点重置优惠券库存
     * cron表达式：秒 分 时 日 月 周
     * 0 0 0 * * ? 表示每天0点0分0秒执行
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void resetDailyCouponCount() {
        log.info("开始执行每日优惠券库存重置任务...");
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 将 count 重置为 publish_count（发行数量）
            // 只重置在有效期内的优惠券（start_time <= now <= end_time）
            // 并且 publish_count > 0 的优惠券
            // 注意：使用 setSql 方式更新，publish_count 如果为 NULL，count 也会被设为 NULL
            int updatedCount = couponMapper.update(null, new LambdaUpdateWrapper<SmsCoupon>()
                    .le(SmsCoupon::getStartTime, now)
                    .ge(SmsCoupon::getEndTime, now)
                    .isNotNull(SmsCoupon::getPublishCount)
                    .gt(SmsCoupon::getPublishCount, 0)
                    .setSql("count = publish_count"));
            
            log.info("每日优惠券库存重置完成，共重置 {} 张优惠券的库存", updatedCount);
        } catch (Exception e) {
            log.error("每日优惠券库存重置失败", e);
            throw e; // 抛出异常以触发事务回滚
        }
    }
    
    /**
     * 定时更新过期优惠券状态
     * 每小时执行一次，将过期但状态仍为"未使用"的优惠券更新为"已过期"
     * cron表达式：0 0 * * * ? 表示每小时0分0秒执行
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void updateExpiredCoupons() {
        log.info("开始执行优惠券过期状态更新任务...");
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 更新过期但状态仍为"未使用"(0)的优惠券为"已过期"(2)
            // 条件：use_status = 0 且 expire_time < now 且 expire_time 不为空
            int updatedCount = couponHistoryMapper.update(null, 
                new LambdaUpdateWrapper<SmsCouponHistory>()
                    .eq(SmsCouponHistory::getUseStatus, 0) // 未使用状态
                    .isNotNull(SmsCouponHistory::getExpireTime) // 过期时间不为空
                    .lt(SmsCouponHistory::getExpireTime, now) // 已过期
                    .set(SmsCouponHistory::getUseStatus, 2) // 设置为已过期
            );
            
            log.info("优惠券过期状态更新完成，共更新 {} 张优惠券为已过期状态", updatedCount);
        } catch (Exception e) {
            log.error("优惠券过期状态更新失败", e);
            throw e; // 抛出异常以触发事务回滚
        }
    }
}


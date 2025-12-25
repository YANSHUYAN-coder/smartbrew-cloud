-- 为优惠券表添加有效期天数字段
ALTER TABLE `sms_coupon` 
ADD COLUMN `valid_days` int NULL DEFAULT NULL COMMENT '有效期天数（从领取时开始计算，如果为NULL则使用end_time）' AFTER `points`;

-- 为优惠券历史表添加个人过期时间字段
ALTER TABLE `sms_coupon_history` 
ADD COLUMN `expire_time` datetime NULL DEFAULT NULL COMMENT '个人过期时间（从领取时间+有效期天数计算）' AFTER `create_time`;

-- 更新已有数据：如果有 valid_days，计算个人过期时间
-- 注意：这里需要根据实际情况调整，如果优惠券模板有 valid_days，则计算；否则使用模板的 end_time
UPDATE `sms_coupon_history` h
INNER JOIN `sms_coupon` c ON h.coupon_id = c.id
SET h.expire_time = CASE 
    WHEN c.valid_days IS NOT NULL AND c.valid_days > 0 THEN DATE_ADD(h.create_time, INTERVAL c.valid_days DAY)
    ELSE c.end_time
END
WHERE h.expire_time IS NULL;


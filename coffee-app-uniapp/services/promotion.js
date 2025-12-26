/**
 * 营销活动/优惠券相关 API
 */
import { get, post } from '@/utils/request.js'

// 获取积分商城可兑换的优惠券列表
export const getRedeemableCoupons = (page = 1, pageSize = 10) => {
  return get('/app/coupon/redeem/list', { page, pageSize })
}

// 积分兑换优惠券
export const redeemCoupon = (id) => {
  return post(`/app/coupon/redeem/${id}`)
}

// 获取我的优惠券
export const getMyCoupons = (useStatus) => {
  return get('/app/coupon/my/list', { useStatus })
}

// 每日签到
export const signIn = () => {
  return post('/app/signin/sign')
}

// 查询今日签到状态
export const getTodaySignIn = () => {
  return get('/app/signin/today')
}

// 获取积分明细列表
export const getIntegrationHistory = (page = 1, pageSize = 10) => {
  return get('/app/integration/history', { page, pageSize })
}


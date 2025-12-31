/**
 * 支付相关 API
 */
import { get, post } from '@/utils/request.js'


export const alipay = (orderId) => {
  return post(`/app/pay/alipay?orderId=${orderId}`)
}

// 咖啡卡支付
export const payByCoffeeCard = (orderId) => {
  return post(`/app/pay/coffee-card?orderId=${orderId}`)
}

// 同步支付状态
export const syncPaymentStatus = (orderId) => {
  return post(`/app/pay/sync?orderId=${orderId}`)
}
/**
 * 支付相关 API
 */
import { get, post } from '@/utils/request.js'


export const alipay = (orderId) => {
  return post(`/app/pay/alipay?orderId=${orderId}`)
}
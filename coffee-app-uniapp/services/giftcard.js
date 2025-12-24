/**
 * 礼品卡相关 API
 */
import { get, post } from '@/utils/request.js'

// 获取礼品卡列表
export const getGiftCardList = (params = {}) => {
  return get('/app/giftCard/list', params)
}

// 创建礼品卡订单（用于支付）
export const createGiftCardOrder = (data) => {
  return post('/app/giftCard/createOrder', data)
}

// 获取礼品卡交易明细
export const getGiftCardTransactions = (cardId, params = {}) => {
  return get(`/app/giftCard/${cardId}/transactions`, params)
}

// 获取咖啡卡总余额
export const getGiftCardTotalBalance = () => {
  return get('/app/giftCard/totalBalance')
}


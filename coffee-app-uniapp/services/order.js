/**
 * 订单相关 API
 */
import {get, post, request} from '@/utils/request.js'

// 创建订单
export const createOrder = (data) => {
  return post('/app/order/create', data)
}

// 获取订单列表
export const getOrderList = (params = {}) => {
  return get('/app/order/list', params)
}

// 获取订单列表（滚动分页）
// params: { lastId: number | null, pageSize: number, status?: number }
export const getOrderListCursor = (params = {}) => {
  return get('/app/order/list/cursor', params)
}

// 获取订单详情
export const getOrderDetail = (id) => {
  return get(`/app/order/${id}`)
}

// 取消订单
export function cancelOrder(orderId, reason) {
  return request({
    url: '/app/order/cancel',
    method: 'POST',
    data: {
      orderId,
      reason
    }
  })
}

// 确认收货
export const confirmReceiveOrder = (id) => {
  return post(`/app/order/confirm/${id}`)
}


/**
 * 订单相关 API
 */
import { get, post } from '@/utils/request.js'

// 创建订单
export const createOrder = (data) => {
  return post('/app/order/create', data)
}

// 获取订单列表
export const getOrderList = (params = {}) => {
  return get('/app/order/list', params)
}

// 获取订单详情
export const getOrderDetail = (id) => {
  return get(`/app/order/${id}`)
}

// 取消订单
export const cancelOrder = (id) => {
  return post(`/app/order/cancel/${id}`)
}


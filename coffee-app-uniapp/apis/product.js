/**
 * 商品相关 API
 */
import { get, post } from '@/utils/request.js'

// 获取商品列表
export const getProducts = (params = {}) => {
  return get('/api/product/list', params)
}

// 获取商品详情
export const getProductDetail = (id) => {
  return get(`/api/product/${id}`)
}

// 搜索商品
export const searchProducts = (keyword) => {
  return get('/api/product/search', { keyword })
}


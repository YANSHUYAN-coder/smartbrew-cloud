/**
 * 商品相关 API
 */
import { get, post } from '@/utils/request.js'

// 获取商品列表（旧接口，返回按分类分组的对象）
export const getProducts = () => {
  return get('/app/product/menu')
}

// 获取菜单数据（新接口，返回 categories 和 products 数组）
export const getMenuVO = () => {
  return get('/app/product/menu/vo')
}

// 获取商品详情（包含SKU规格）
export const getProductDetail = (id) => {
  return get(`/app/product/detail/${id}`)
}

// 搜索商品（公共接口，C端和管理端共用）
export const searchProducts = (keyword, page = 1, pageSize = 20) => {
  return get('/api/product/search', { keyword, page, pageSize })
}


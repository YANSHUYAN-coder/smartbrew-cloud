/**
 * 商品分类 API
 */
import { get, post } from '@/utils/request.js'

// 获取商品分类
export const getCategories = () => {
  return get('/app/categories/list')
}
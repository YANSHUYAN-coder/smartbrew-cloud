import { get } from '@/utils/request.js'

/**
 * 获取门店信息
 * @param {Number} storeId 可选，不传则获取默认门店
 */
export const getStoreInfo = (storeId) => {
  return get('/app/store/info', { storeId })
}

/**
 * 获取所有门店列表
 */
export const getStoreList = () => {
  return get('/app/store/list')
}


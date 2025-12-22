import { get } from '@/utils/request.js'

/**
 * 获取行政区划列表 (省市区)
 */
export const getRegions = () => {
  return get('/common/dict/regions')
}

/**
 * 获取枚举字典
 * @param {String} type orderStatus | gender
 */
export const getEnumDict = (type) => {
  return get(`/common/dict/enum/${type}`)
}


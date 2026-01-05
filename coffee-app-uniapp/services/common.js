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

/**
 * 逆地理编码 (坐标转地址)
 * @param {String} location "lng,lat"
 */
export const regeo = (location) => {
  return get('/common/dict/regeo', { location })
}

/**
 * 获取与门店的距离
 * @param {String} userLocation "lng,lat"
 * @param {Number} storeId 门店ID
 */
export const getDistance = (userLocation, storeId) => {
  return get('/common/dict/distance', { userLocation, storeId })
}

/**
 * 地理编码 (地址转坐标)
 * @param {String} address 完整地址
 * @param {String} city 城市（可选）
 */
export const geocode = (address, city) => {
  const params = { address }
  if (city) {
    params.city = city
  }
  return get('/common/dict/geocode', params)
}


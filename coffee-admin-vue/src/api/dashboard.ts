import request from '@/utils/request'

/**
 * 获取统计数据
 */
export function getStatistics() {
  return request({
    url: '/admin/dashboard/statistics',
    method: 'get'
  })
}

/**
 * 获取销售趋势
 * @param days 天数，默认7天
 */
export function getSalesTrend(days: number = 7) {
  return request({
    url: '/admin/dashboard/sales-trend',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取热销商品Top N
 * @param topN Top数量，默认5
 */
export function getTopProducts(topN: number = 5) {
  return request({
    url: '/admin/dashboard/top-products',
    method: 'get',
    params: { topN }
  })
}


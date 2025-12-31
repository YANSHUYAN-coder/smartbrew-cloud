import request from '@/utils/request'

/**
 * 获取所有门店列表
 */
export function getStoreList() {
  return request({
    url: '/admin/store/list',
    method: 'get'
  })
}

/**
 * 获取指定门店详情
 */
export function getStoreInfo() {
  return request({
    url: '/admin/store/info',
    method: 'get'
  })
}

/**
 * 更新门店信息
 */
export function updateStore(data: any) {
  return request({
    url: '/admin/store/update',
    method: 'post',
    data
  })
}

/**
 * 新增门店
 */
export function addStore(data: any) {
  return request({
    url: '/admin/store/add',
    method: 'post',
    data
  })
}

/**
 * 切换营业状态
 */
export function updateOpenStatus(id: number, status: number) {
  return request({
    url: `/admin/store/status/open/${id}/${status}`,
    method: 'post'
  })
}

/**
 * 删除门店
 */
export function deleteStore(id: number) {
  return request({
    url: `/admin/store/delete/${id}`,
    method: 'post'
  })
}


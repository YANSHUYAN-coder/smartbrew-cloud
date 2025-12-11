import request from '@/utils/request'

export function getOrderList(params: any) {
  return request({
    url: '/admin/order/list',
    method: 'get',
    params
  })
}

export function getOrderDetail(id: number) {
  return request({
    url: `/admin/order/detail/${id}`,
    method: 'get'
  })
}

export function updateOrderStatus(data: any) {
  return request({
    url: '/admin/order/updateStatus',
    method: 'post',
    data
  })
}

export function deleteOrder(id: number) {
  return request({
    url: `/admin/order/delete/${id}`,
    method: 'delete'
  })
}


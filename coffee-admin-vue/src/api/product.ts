import request from '@/utils/request'

export function getProductList(params: any) {
  return request({
    url: '/admin/product/list',
    method: 'get',
    params
  })
}

export function createProduct(data: any) {
  return request({
    url: '/admin/product/add',
    method: 'post',
    data
  })
}

export function updateProduct(data: any) {
  return request({
    url: '/admin/product/update',
    method: 'post',
    data
  })
}

export function updateProductStatus(data: any) {
  return request({
    url: '/admin/product/updateStatus',
    method: 'post',
    data
  })
}

export function deleteProduct(id: number) {
  return request({
    url: `/admin/product/delete/${id}`,
    method: 'delete'
  })
}


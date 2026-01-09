import request from '@/utils/request'

// 获取商品列表（使用公共搜索接口，支持筛选）
export function getProductList(params: any) {
  return request({
    url: '/api/product/search',
    method: 'get',
    params
  })
}

export function getProductDetail(id: number) {
  return request({
    url: `/admin/product/detail/${id}`,
    method: 'get'
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

export function uploadProductImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/product/upload/image',
    method: 'post',
    data: formData
    // 注意：不要手动设置 Content-Type，让浏览器自动设置，这样会包含 boundary
  })
}


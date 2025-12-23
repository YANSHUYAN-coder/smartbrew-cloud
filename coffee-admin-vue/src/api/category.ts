import request from '@/utils/request'

export function getCategoryList(params: any) {
  return request({
    url: '/admin/category/list',
    method: 'get',
    params
  })
}

export function getCategoryDetail(id: number) {
  return request({
    url: `/admin/category/detail/${id}`,
    method: 'get'
  })
}

export function createCategory(data: any) {
  return request({
    url: '/admin/category/add',
    method: 'post',
    data
  })
}

export function updateCategory(data: any) {
  return request({
    url: '/admin/category/update',
    method: 'post',
    data
  })
}

export function updateCategoryStatus(data: any) {
  return request({
    url: '/admin/category/updateStatus',
    method: 'post',
    data
  })
}

export function deleteCategory(id: number) {
  return request({
    url: `/admin/category/delete/${id}`,
    method: 'delete'
  })
}


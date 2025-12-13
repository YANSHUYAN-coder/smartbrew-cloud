import request from '@/utils/request'

export function getMenuList(params: any) {
  return request({
    url: '/admin/permission/list', // 对应 ums_permission
    method: 'get',
    params
  })
}

export function createMenu(data: any) {
  return request({
    url: '/admin/permission/create',
    method: 'post',
    data
  })
}

export function updateMenu(data: any) {
  return request({
    url: '/admin/permission/update',
    method: 'post',
    data
  })
}

export function deleteMenu(id: number) {
  return request({
    url: `/admin/permission/delete/${id}`,
    method: 'delete'
  })
}


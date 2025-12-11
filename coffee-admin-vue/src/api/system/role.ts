import request from '@/utils/request'

export function getRoleList(params: any) {
  return request({
    url: '/admin/role/list',
    method: 'get',
    params
  })
}

export function createRole(data: any) {
  return request({
    url: '/admin/role/create',
    method: 'post',
    data
  })
}

export function updateRole(data: any) {
  return request({
    url: '/admin/role/update',
    method: 'post',
    data
  })
}

export function deleteRole(id: number) {
  return request({
    url: `/admin/role/delete/${id}`,
    method: 'delete'
  })
}

export function getRoleMenuIds(roleId: number) {
  return request({
    url: `/admin/role/menus/${roleId}`,
    method: 'get'
  })
}

export function updateRoleMenus(roleId: number, menuIds: number[]) {
  return request({
    url: '/admin/role/allocMenu',
    method: 'post',
    data: { roleId, menuIds }
  })
}


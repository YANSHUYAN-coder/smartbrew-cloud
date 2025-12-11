import request from '@/utils/request'

// 这里的 User 其实是 UmsMember，只是在系统管理模块中侧重于管理管理员角色
export function getUserList(params: any) {
  return request({
    url: '/admin/member/list', // 复用 member list，或者新增专门的 admin list
    method: 'get',
    params
  })
}

export function createUser(data: any) {
    return request({
      url: '/auth/register', // 复用注册接口
      method: 'post',
      data
    })
}

export function updateUser(data: any) {
  return request({
    url: '/admin/member/updateInfo', // 需新增
    method: 'post',
    data
  })
}

export function deleteUser(id: number) {
  return request({
    url: `/admin/member/delete/${id}`,
    method: 'delete'
  })
}


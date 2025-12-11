import request from '@/utils/request'

export function login(data: any) {
  return request({
    url: '/admin/auth/login', // 根据实际后端接口修改
    method: 'post',
    data
  })
}

export function logout() {
  return request({
    url: '/admin/auth/logout',
    method: 'post'
  })
}

export function getInfo() {
  return request({
    url: '/admin/auth/info',
    method: 'get'
  })
}


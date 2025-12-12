import request from '@/utils/request'

export function login(data: any) {
  return request({
    url: '/auth/login', // Removed /admin prefix to match backend /api/auth/login
    method: 'post',
    data
  })
}

export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

export function getInfo() {
  return request({
    url: '/auth/info', // Check if this exists or needs adjustment
    method: 'get'
  })
}


import request from '@/utils/request'

export function getMemberList(params: any) {
  return request({
    url: '/admin/member/list',
    method: 'get',
    params
  })
}

export function getMemberDetail(id: number) {
  return request({
    url: `/admin/member/detail/${id}`,
    method: 'get'
  })
}

export function updateMemberStatus(data: any) {
  return request({
    url: '/admin/member/update',
    method: 'post',
    data
  })
}


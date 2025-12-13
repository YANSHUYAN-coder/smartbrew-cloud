import request from '@/utils/request'

export function getMemberLevelList(params: any) {
  return request({
    url: '/admin/member-level/list',
    method: 'get',
    params
  })
}

export function getMemberLevelDetail(id: number) {
  return request({
    url: `/admin/member-level/detail/${id}`,
    method: 'get'
  })
}

export function createMemberLevel(data: any) {
  return request({
    url: '/admin/member-level/add',
    method: 'post',
    data
  })
}

export function updateMemberLevel(data: any) {
  return request({
    url: '/admin/member-level/update',
    method: 'post',
    data
  })
}

export function deleteMemberLevel(id: number) {
  return request({
    url: `/admin/member-level/delete/${id}`,
    method: 'delete'
  })
}

export function updateMemberLevelStatus(data: any) {
  return request({
    url: '/admin/member-level/updateStatus',
    method: 'post',
    data
  })
}


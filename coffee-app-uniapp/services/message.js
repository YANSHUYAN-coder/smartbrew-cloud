/**
 * 消息中心相关 API
 */
import { get, post } from '@/utils/request.js'

// 获取消息列表
export const getMessageList = (params = {}) => {
  return get('/app/message/list', params)
}

// 获取未读消息数量
export const getUnreadCount = () => {
  return get('/app/message/unread-count')
}

// 标记消息为已读
// data: { id: Long }，如果不传 id 则标记全部已读
export const markAsRead = (data = {}) => {
  return post('/app/message/read', data)
}
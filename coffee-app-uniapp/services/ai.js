import { get } from '@/utils/request.js'

/**
 * 发送消息给 AI
 * @param {String} message 用户的问题
 */
export const chatWithAi = (message) => {
  return get('/app/ai/chat', { message })
}
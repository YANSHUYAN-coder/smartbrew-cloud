import { get, post } from '@/utils/request.js'

/**
 * 发送消息给 AI (RAG 增强)
 * @param {String} question 用户的问题
 */
export const chatWithAi = (question) => {
  return get(`/coffee/chat/rag?question=${question}`)
}

/**
 * 获取聊天历史记录
 */
export const getChatHistory = () => {
  return get('/coffee/chat/history')
}

/**
 * 清空聊天历史
 */
export const clearChatHistory = () => {
  return post('/coffee/chat/clear')
}
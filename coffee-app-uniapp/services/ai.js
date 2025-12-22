import { get } from '@/utils/request.js'

/**
 * 发送消息给 AI (RAG 增强)
 * @param {String} question 用户的问题
 */
export const chatWithAi = (question) => {
  return get(`/coffee/rag?question=${question}`)
}
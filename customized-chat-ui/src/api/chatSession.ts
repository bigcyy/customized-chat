import type { Ref } from 'vue'
import { Result, type AiResponseVO } from '@/request/Result'
import { get, post, del, postSSEStream } from '@/request'
import type { ChatMessage, ChatSession } from '@/api/type/application'

/**
 * 获取应用的聊天会话列表
 */
const getChatSessions = (applicationId: number, loading?: Ref<boolean>): Promise<Result<any>> => {
  return get(`/application/${applicationId}/chat/sessions`, undefined, loading)
}

/**
 * 获取单个聊天会话详情
 */
const getChatSession = (sessionId: string): Promise<Result<any>> => {
  return get(`/chat/session/${sessionId}`)
}

/**
 * 获取会话的聊天消息
 */
const getSessionMessages = (sessionId: number, loading?: Ref<boolean>): Promise<Result<any>> => {
  return get(`/chat/session/${sessionId}/messages`, undefined, loading)
}

/**
 * 创建新的聊天会话
 */
const createChatSession = (applicationId: number, data: Partial<ChatSession>): Promise<Result<any>> => {
  return post(`/application/${applicationId}/chat/sessions`, data)
}

/**
 * 更新聊天会话
 */
const updateChatSession = (sessionId: string, data: Partial<ChatSession>): Promise<Result<any>> => {
  return post(`/chat/session/${sessionId}`, data)
}

/**
 * 删除聊天会话
 */
const deleteSession = (sessionId: string): Promise<Result<any>> => {
  return del(`/chat/session/${sessionId}`)
}

/**
 * 打开指定应用的聊天会话
 */
const openChatSession = (applicationId: number): Promise<Result<any>> => {
  return post(`/application/${applicationId}/chat/session`)
}

/**
 * 应用聊天 - SSE流式响应
 * @param applicationId 应用ID
 * @param sessionId 会话ID
 * @param userMessage 用户消息
 * @param onMessage 接收到消息时的回调
 * @param onError 错误回调
 * @param onComplete 完成回调
 * @returns 控制对象，包含abort方法用于取消请求
 */
const postChatMessageStream = (
  applicationId: number,
  sessionId: number,
  userMessage: ChatMessage,
  onMessage?: (data: AiResponseVO) => void,
  onError?: (error: any) => void,
  onComplete?: () => void
) : {
  abort: () => void,
  promise: Promise<any>
} => {
  return postSSEStream(`/application/${applicationId}/chat/session/${sessionId}`, userMessage, onMessage, onError, onComplete)
}

export default {
  getChatSessions,
  getChatSession,
  getSessionMessages,
  createChatSession,
  updateChatSession,
  deleteSession,
  openChatSession,
  postChatMessageStream
}
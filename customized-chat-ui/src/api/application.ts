import Result from '@/request/Result'
import type { Ref } from 'vue'
import { get, post, put, del, postSSEStream } from '@/request'
import type { ApplicationForm, TempChatDto } from '@/api/type/application'
/**
 * 创建Agent
 */
const createApplication: (data: ApplicationForm, loading?: Ref<boolean>) => Promise<Result<any>> = (
  data,
  loading
) => {
  return post('/application', data, {}, loading)
}

// const postChatMessage: (chat_id: string, data: any) => Promise<any> = (chat_id, data) => {
//   return postStream(`/api/chat_message/${chat_id}`, data)
// }

/**
 * 根据Agent id 打开会话
 * @param 参数

 }
 */
const openChat: (application_id: String) => Promise<Result<any>> = (application_id) => {
  return get(`/api/chat_open/${application_id}`)
}

/**
 * 打开临时会话
 * @param 参数

 }
 */
const openTempChat: () => Promise<Result<any>> = () => {
  return post(`/application/temp/chat/session`)
}

/**
 * 临时会话聊天 - SSE流式响应
 * @param sessionId 会话ID
 * @param data TempChatDto对象，包含application、chatMessage、chatHistories
 * @param onMessage 接收到消息时的回调
 * @param onError 错误回调
 * @param onComplete 完成回调
 * @returns 控制对象，包含abort方法用于取消请求
 */
const postTempChatMessageStream = (
  sessionId: number | undefined, 
  data: TempChatDto,
  onMessage?: (data: string) => void,
  onError?: (error: any) => void,
  onComplete?: () => void
) : {
  abort: () => void,
  promise: Promise<any>
} => {
  return postSSEStream(`/application/temp/chat/session/${sessionId}`, data, onMessage, onError, onComplete)
}

export default { createApplication, openChat, openTempChat, postTempChatMessageStream }

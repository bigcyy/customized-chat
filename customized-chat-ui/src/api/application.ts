import Result from '@/request/Result'
import type { Ref } from 'vue'
import { get, post, put, del, postStream } from '@/request'
import type { ApplicationForm } from '@/api/type/application'
/**
 * 创建应用
 */
const createApplication: (data: ApplicationForm, loading?: Ref<boolean>) => Promise<Result<any>> = (
  data,
  loading
) => {
  return post('/application', data, {}, loading)
}

const postChatMessage: (chat_id: string, data: any) => Promise<any> = (chat_id, data) => {
  return postStream(`/api/chat_message/${chat_id}`, data)
}

/**
 * 根据应用 id 打开会话
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
const openTempChat: (data: ApplicationFormType) => Promise<Result<any>> = (data) => {
  return post(`temp/chat/open`, data)
}

export default { createApplication, postChatMessage, openChat, openTempChat }

import { Result, type AiResponseVO } from '@/request/Result'
import type { Ref } from 'vue'
import { get, post, put, del, postSSEStream } from '@/request'
import type { ApplicationForm, TempChatDto } from '@/api/type/application'
import type { MCPServer } from '@/api/type/mcpServer'
/**
 * 创建Agent
 */
const createApplication: (data: ApplicationForm, loading?: Ref<boolean>) => Promise<Result<any>> = (
  data,
  loading
) => {
  return post('/application', data, {}, loading)
}

/**
 * 根据ID获取Agent
 */
const getApplicationById: (id: number, loading?: Ref<boolean>) => Promise<Result<any>> = (
  id,
  loading
) => {
  return get(`/application/${id}`, {}, loading)
}

/**
 * 更新Agent
 */
const updateApplication: (data: ApplicationForm, loading?: Ref<boolean>) => Promise<Result<any>> = (
  data,
  loading
) => {
  console.log(data)
  return put('/application', data, {}, loading)
}

/**
 * 打开临时会话
 * @param 参数
 */
const openTempChat: () => Promise<Result<any>> = () => {
  return post(`/temp/chat/session`)
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
  onMessage?: (data: AiResponseVO) => void,
  onError?: (error: any) => void,
  onComplete?: () => void
) : {
  abort: () => void,
  promise: Promise<any>
} => {
  return postSSEStream(`/temp/chat/session/${sessionId}`, data, onMessage, onError, onComplete)
}

const listApplications: (page: number, size: number) => Promise<Result<any>> = (page, size) => {
  return get('/application', { pageIndex : page, pageSize : size })
}

const deleteApplication: (id: number) => Promise<Result<any>> = (id) => {
  return del(`/application/${id}`)
}

/**
 * 获取应用关联的MCP服务器
 */
const getApplicationMcpServers: (applicationId: number, loading?: Ref<boolean>) => Promise<Result<{ mcpServers: MCPServer[] }>> = (
  applicationId,
  loading
) => {
  return get(`/application/${applicationId}/mcp-servers`, {}, loading)
}

/**
 * 更新应用关联的MCP服务器
 */
const updateApplicationMcpServers: (applicationId: number, mcpServerIds: number[], loading?: Ref<boolean>) => Promise<Result<any>> = (
  applicationId,
  mcpServerIds,
  loading
) => {
  return post(`/application/${applicationId}/mcp-servers`, mcpServerIds, {}, loading)
}

/**
 * 移除应用的MCP服务器关联
 */
const removeApplicationMcpServer: (applicationId: number, mcpServerId: number, loading?: Ref<boolean>) => Promise<Result<any>> = (
  applicationId,
  mcpServerId,
  loading
) => {
  return del(`/application/${applicationId}/mcp-servers/${mcpServerId}`, {}, loading)
}

export default {
  createApplication,
  getApplicationById,
  updateApplication,
  openTempChat,
  postTempChatMessageStream,
  listApplications,
  deleteApplication,
  getApplicationMcpServers,
  updateApplicationMcpServers,
  removeApplicationMcpServer
}

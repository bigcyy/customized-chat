import type { ModelConfig } from "./model"
import type { toolExecutionDetail } from "@/request/Result"

interface ApplicationForm {
  id?: number
  name: string
  description?: string
  prologue?: string
  workflow?: string
  icon?: string
  applicationType?: string
  modelId?: number
  modelSetting?: ModelSetting
  datasetSetting?: string
  mcpSetting?: McpSetting
  mcpServerIds?: number[]
}

/**
 * 聊天会话接口 - 对应后端ChatSession实体
 */
interface ChatSession {
  id?: number
  applicationId: number
  chatAbstract: string
  updateTime: string
  createTime: string
  isDeleted: boolean
  clientId?: string
}

interface chatType {
  id?: string
  problem_text: string
  answer_text: string
  buffer: Array<String>
  answer_text_list: Array<{
    content: string
    chat_record_id?: string
    runtime_node_id?: string
    child_node?: any
  }>
  /**
   * 是否写入结束
   */
  write_ed?: boolean
  /**
   * 是否暂停
   */
  is_stop?: boolean
  record_id: string
  chat_id: string
  vote_status: string
  status?: number
  execution_details?: any[]
}

interface ChatMessage {
  id?: number,
  sessionId: number | undefined,
  messageIndex: number,
  messageText: string,
  messageToken?: number,
  updateTime?: string,
  createTime?: string,
  loading?: boolean,
  isError?: boolean,
  role: 'user' | 'assistant'
  toolExecutionDetail?: toolExecutionDetail[]
}

/**
 * 临时聊天DTO
 */
interface TempChatDto {
  application: ApplicationForm
  chatMessage: ChatMessage
  chatHistories?: ChatMessage[]
}

/**
 * 模型设置
 */
interface ModelSetting {
  prompt: string
  chatMemory: number
  system: string
  noReferencesPrompt: string
  referencesPrompt: string
  modelConfig: ModelConfig
}

/**
 * MCP设置 - 配置MCP的相关内容
 */
interface McpSetting {
  /** sse 传输形式的 mcp server */
  sseServers?: SseTransport[]
  /** stdio 传输形式的 mcp server */
  stdioServers?: StdioTransport[]
}

/**
 * SSE传输格式
 */
interface SseTransport {
  /** 传输类型, sse 或 stdio */
  type: string
  /** sse url */
  sseUrl: string
  /** 发送请求时附带的请求头，可用于鉴权 */
  header?: Record<string, string>
}

/**
 * STDIO传输格式
 */
interface StdioTransport {
  /** 传输类型, sse 或 stdio */
  type: string
  /** 命令, uvx 或 npx */
  command: string
  /** 命令的附带参数 */
  args?: string[]
}

export type { ApplicationForm, chatType, ChatMessage, TempChatDto, ModelSetting, ChatSession, McpSetting, SseTransport, StdioTransport }

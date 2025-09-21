interface MCPServer {
  /** id */
  id?: number
  /** MCP 服务名称 */
  serverName: string
  /** MCP 服务描述 */
  serverDescription?: string
  /** 传输类型, sse 或 stdio */
  type: string
  /** sse url */
  sseUrl?: string
  /** 发送请求时附带的请求头，可用于鉴权 */
  header?: Record<string, string>
  /** 命令, uvx 或 npx */
  command?: string
  /** 命令的附带参数 */
  args?: string[],
  /** 是否启用 */
  isEnabled?: boolean,
}

export type { MCPServer }
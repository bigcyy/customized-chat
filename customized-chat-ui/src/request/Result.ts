export class Result<T> {
  success: boolean
  message: string
  code: number
  data: T
  constructor(success: boolean, message: string, code: number, data: T) {
    this.success = success
    this.message = message
    this.code = code
    this.data = data
  }

  static success(data: any) {
    return new Result(true, '请求成功', 200, data)
  }
  static error(message: string, code: number) {
    return new Result(false, message, code, null)
  }
}

export interface Page<T> {
  records: T[],
  total: number,
  size: number,
  current: number
}

/**
 * 封装 ai 的流式响应
 */
export interface AiResponseVO {
  /** 这一段响应的 id */
  chunkId: number;
  /** 这一段响应所属的会话 id */
  sessionId?: number;
  /** 这一段响应所属的消息 id */
  messageId?: number;
  /** 响应的内容 */
  message?: string;
  /** 是否为最后一段 */
  isEnd?: boolean;
  /** 响应的工具调用内容 */
  toolExecution?: ToolExecution;
}

export interface ToolExecution {
  request: ToolExecutionRequest;
  result: string;
}

export interface ToolExecutionRequest {
  id: string;
  name: string;
  arguments: string;
}

export default Result

interface ApplicationForm {
  id?: number
  name: string
  description?: string
  prologue?: string
  workflow?: string
  icon?: string
  applicationType: string
  modelId?: number
  modelSetting?: string
  dataset_setting?: string
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

export type { ApplicationForm, chatType }

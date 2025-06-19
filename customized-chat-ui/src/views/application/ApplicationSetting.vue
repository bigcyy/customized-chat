<template>
  <div>
    <div class="flex-between mb-16">
      <h3>Agent设置</h3>
      <div><el-button type="primary" @click="handleSaveAndPublish" :disabled="isSaving || isLoading" :loading="isSaving" >保存并发布</el-button></div>
    </div>
    <div class="content-body" v-loading="isLoading">
      <el-row class="full-height">
        <el-col :span="10" class="left-column custom-scrollbar">
          <div class="section-header">
            <h4 class="section-title">Agent信息</h4>
          </div>
          <div class="form-wrapper">
            <el-form
              :model="applicationInfo"
              label-position="top"
              class="form-container"
            >
              <el-form-item label="Agent名称" required>
                <el-input
                  v-model="applicationInfo.name"
                  placeholder="请输入Agent名称"
                  maxlength="64"
                  :input-style="{ backgroundColor: '#fff' }"
                >
                  <template #append>
                    <span class="word-limit">{{ applicationInfo.name.length }}/64</span>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="Agent描述">
                <el-input
                  v-model="applicationInfo.description"
                  type="textarea"
                  placeholder="请输入Agent描述"
                  maxlength="256"
                  :rows="4"
                  resize="none"
                  :input-style="{ backgroundColor: '#fff' }"
                >
                  <template #append>
                    <span class="word-limit">{{ applicationInfo.description?.length }}/256</span>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item class="ai-model-form-item">
                <template #label>
                  <div class="model-label">
                    <span>AI 模型</span>
                    <el-button link class="param-setting">参数设置</el-button>
                  </div>
                </template>
                <div class="model-select">
                  <el-select
                    v-model="applicationInfo.modelId"
                    placeholder="请选择 AI 模型"
                    class="full-width"
                    :loading="isLoadingModels"
                  >
                    <el-option 
                      v-for="model in modelOptions" 
                      :key="model.value" 
                      :label="model.label" 
                      :value="model.value" 
                    />
                  </el-select>
                </div>
              </el-form-item>
              <el-form-item label="角色设定">
                <el-input
                  v-model="modelPrompt"
                  type="textarea"
                  placeholder="请输入角色设定"
                  :rows="4"
                  resize="none"
                  :input-style="{ backgroundColor: '#fff' }"
                />
              </el-form-item>
              <el-form-item label="历史聊天记录">
                <el-input-number
                  v-model="modelChatMemory"
                  placeholder="请输入历史聊天记录"
                  controls-position="right"
                  class="full-width"
                />
              </el-form-item>
              <el-form-item class="mb-0" label="开场白">
                <el-input
                  v-model="applicationInfo.prologue"
                  type="textarea"
                  placeholder="您好，我是 xxx 小助手，您可以向我提出 xxx 使用问题。"
                  resize="none"
                  :input-style="{ backgroundColor: '#fff' }"
                  :rows="4"
                />
              </el-form-item>
              
              <!-- MCP设置部分 -->
              <el-form-item label="工具" class="mcp-setting-form-item">
                <div class="mcp-config-container">
                  <div class="mcp-header">
                    <span class="mcp-count">{{ totalMcpServers }}/{{ totalMcpServers }} 启用</span>
                    <el-popover
                      placement="bottom-end"
                      :width="200"
                      trigger="click"
                      popper-class="mcp-add-popover"
                    >
                      <template #reference>
                        <el-button type="primary" size="small" :icon="Plus">
                          添加
                        </el-button>
                      </template>
                      <div class="mcp-type-options">
                        <div class="option-item" @click="addSseServer">
                          <div class="option-content">
                            <h6>SSE传输服务器</h6>
                            <p>通过SSE协议连接的MCP服务器</p>
                          </div>
                        </div>
                        <div class="option-item" @click="addStdioServer">
                          <div class="option-content">
                            <h6>STDIO传输服务器</h6>
                            <p>通过STDIO协议连接的MCP服务器</p>
                          </div>
                        </div>
                      </div>
                    </el-popover>
                  </div>
                  
                  <!-- 服务器列表 -->
                  <div v-if="allMcpServers.length === 0" class="empty-state">
                    <div class="empty-icon">🔧</div>
                    <div class="empty-text">暂无MCP服务器配置</div>
                    <div class="empty-description">点击右上角添加按钮配置MCP服务器</div>
                  </div>
                  
                  <div v-else class="mcp-server-list">
                    <!-- SSE服务器 -->
                    <el-card 
                      v-for="(server, index) in sseServers" 
                      :key="`sse-${index}`"
                      class="mcp-server-card"
                      shadow="never"
                    >
                      <template #header>
                        <div class="server-header">
                          <div class="server-info">
                            <span class="server-type">SSE</span>
                            <span class="server-title">{{ server.sseUrl || 'SSE服务器' }}</span>
                          </div>
                          <div class="server-actions">
                            <el-button 
                              size="small" 
                              text 
                              @click="editSseServer(index)"
                            >
                              编辑
                            </el-button>
                            <el-button 
                              type="danger" 
                              size="small" 
                              text
                              @click="removeSseServer(index)"
                            >
                              删除
                            </el-button>
                          </div>
                        </div>
                      </template>
                      <div class="server-summary">
                        <div class="summary-item">
                          <span class="label">URL:</span>
                          <span class="value">{{ server.sseUrl || '未配置' }}</span>
                        </div>
                        <div class="summary-item" v-if="server.header && Object.keys(server.header).length > 0">
                          <span class="label">请求头:</span>
                          <span class="value">{{ Object.keys(server.header).length }} 个</span>
                        </div>
                      </div>
                    </el-card>

                    <!-- STDIO服务器 -->
                    <el-card 
                      v-for="(server, index) in stdioServers" 
                      :key="`stdio-${index}`"
                      class="mcp-server-card"
                      shadow="never"
                    >
                      <template #header>
                        <div class="server-header">
                          <div class="server-info">
                            <span class="server-type">STDIO</span>
                            <span class="server-title">{{ server.command || 'STDIO服务器' }}</span>
                          </div>
                          <div class="server-actions">
                            <el-button 
                              size="small" 
                              text 
                              @click="editStdioServer(index)"
                            >
                              编辑
                            </el-button>
                            <el-button 
                              type="danger" 
                              size="small" 
                              text
                              @click="removeStdioServer(index)"
                            >
                              删除
                            </el-button>
                          </div>
                        </div>
                      </template>
                      <div class="server-summary">
                        <div class="summary-item">
                          <span class="label">命令:</span>
                          <span class="value">{{ server.command || '未配置' }}</span>
                        </div>
                        <div class="summary-item" v-if="server.args && server.args.length > 0">
                          <span class="label">参数:</span>
                          <span class="value">{{ server.args.length }} 个</span>
                        </div>
                      </div>
                    </el-card>
                  </div>
                </div>
              </el-form-item>
            </el-form>
          </div>
        </el-col>
        <el-col :span="14" class="right-column">
          <div class="section-header">
            <h4 class="section-title">调试预览</h4>
          </div>
          <div class="chat-area py-20">
            <div class="flex align-center px-24">
              <div
                @mouseenter="showIconEdit = true"
                @mouseleave="showIconEdit = false"
                class="app-icon-container"
              >
                <ColorAvater v-if="showIconEdit" class="edit-icon" shape="square">
                  <el-icon><Edit /></el-icon>
                </ColorAvater>
                <ColorAvater name="Agent" pinyinColor shape="square" />
              </div>
              <h4 class="agent-title">
                {{ applicationInfo.name || 'Agent名称' }}
              </h4>
            </div>
            <div class="chat-container custom-scrollbar px-24">
              <AiChat
              ref="aiChatRef"
              :application="applicationInfo"
              :chat-messages="chatMessages"
              :isMessageSending="isMessageSending"
              @send-message="handleSendMessage"
            />
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- MCP配置弹窗 -->
    <el-dialog
      v-model="mcpDialogVisible"
      :title="mcpDialogTitle"
      width="600px"
      :before-close="handleMcpDialogClose"
    >
      <!-- SSE服务器配置 -->
      <div v-if="currentMcpType === 'sse'">
        <el-form :model="currentSseServer" label-position="top">
          <el-form-item label="SSE URL" required>
            <el-input 
              v-model="currentSseServer.sseUrl" 
              placeholder="请输入SSE服务器URL"
            />
          </el-form-item>
          <el-form-item label="请求头">
            <div class="header-config">
              <div 
                v-for="(headerValue, headerKey) in currentSseServer.header" 
                :key="headerKey"
                class="header-item"
              >
                <el-input 
                  v-model="currentHeaderKeys[headerKey]"
                  placeholder="Header名称"
                  class="header-key"
                  @blur="updateCurrentHeaderKey(headerKey, currentHeaderKeys[headerKey])"
                />
                <el-input 
                  v-model="currentSseServer.header![headerKey]" 
                  placeholder="Header值"
                  class="header-value"
                />
                <el-button 
                  type="danger" 
                  size="small" 
                  text
                  @click="removeCurrentHeader(headerKey)"
                  :icon="Delete"
                />
              </div>
              <el-button 
                type="primary" 
                size="small" 
                text
                @click="addCurrentHeader"
                :icon="Plus"
              >
                添加Header
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <!-- STDIO服务器配置 -->
      <div v-if="currentMcpType === 'stdio'">
        <el-form :model="currentStdioServer" label-position="top">
          <el-form-item label="命令" required>
            <el-input 
              v-model="currentStdioServer.command" 
              placeholder="请输入命令 (如: uvx, npx)"
            />
          </el-form-item>
          <el-form-item label="参数">
            <div class="args-config">
              <div 
                v-for="(arg, argIndex) in currentStdioServer.args" 
                :key="argIndex"
                class="arg-item"
              >
                <el-input 
                  v-model="currentStdioServer.args![argIndex]" 
                  placeholder="请输入参数"
                  class="arg-input"
                />
                <el-button 
                  type="danger" 
                  size="small" 
                  text
                  @click="removeCurrentArg(argIndex)"
                  :icon="Delete"
                />
              </div>
              <el-button 
                type="primary" 
                size="small" 
                text
                @click="addCurrentArg"
                :icon="Plus"
              >
                添加参数
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleMcpDialogClose">取消</el-button>
          <el-button type="primary" @click="saveMcpServer">
            {{ mcpEditIndex >= 0 ? '更新' : '添加' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Edit, Plus, Delete } from '@element-plus/icons-vue'
import type { ApplicationForm, ChatMessage, McpSetting, SseTransport, StdioTransport } from '@/api/type/application'
import modelApi from '@/api/model'
import applicationApi from '@/api/application'
import AiChat from '@/components/ai-chat/index.vue'
import ColorAvater from '@/components/avaters/coloer-avater.vue'
import { MsgSuccess, MsgError } from '@/utils/message'

const route = useRoute()
const router = useRouter()
const isSaving = ref(false)
const isLoading = ref(false)
const showIconEdit = ref(false)
const tempSessionId = ref<number | undefined>(undefined)
const aiChatRef = ref()
const chatMessages = ref<ChatMessage[]>([])
const isMessageSending = ref(false)

// 判断是新增还是编辑模式
const isEditMode = computed(() => route.params.id !== 'new')
const applicationId = computed(() => {
  const id = route.params.id as string
  return id === 'new' ? null : parseInt(id)
})


// 定义模型选项类型
interface ModelOption {
  label: string
  value: number
}

const modelOptions = ref<ModelOption[]>([]) // 存储模型选项
const isLoadingModels = ref(false) // 加载状态

const applicationInfo = ref<ApplicationForm>({
  name: '',
  description: '',
  prologue: `您好，我是 XXX 小助手，您可以向我提出 XXX 使用问题。\n- XXX 主要功能有什么？\n- XXX 如何收费？\n- 需要转人工服务`,
  workflow: undefined,
  icon: '',
  applicationType: '',
  modelId: undefined,
  modelSetting: {
    prompt: '你是 xxx 小助手',
    chatMemory: 1,
    system: '',
    noReferencesPrompt: '',
    referencesPrompt: '',
    modelConfig: {
      temperature: 0.7,
      maxTokens: 10240,
      topP: 0.95
    }
  },
  datasetSetting: undefined
})

// 保存或更新操作
const handleSaveAndPublish = async() => {
  isSaving.value = true
  try {
    if (isEditMode.value) {
      await updateApplication()
    } else {
      await createApplication()
    }
  } finally {
    isSaving.value = false
  }
}
// 创建安全的计算属性来处理 modelSetting 的双向绑定
const modelPrompt = computed({
  get: () => applicationInfo.value.modelSetting?.prompt || '',
  set: (value: string) => {
    if (!applicationInfo.value.modelSetting) {
      applicationInfo.value.modelSetting = {
        prompt: '',
        chatMemory: 1,
        system: '',
        noReferencesPrompt: '',
        referencesPrompt: '',
        modelConfig: {
          temperature: 0.7,
          maxTokens: 10240,
          topP: 0.95
        }
      }
    }
    applicationInfo.value.modelSetting.prompt = value
  }
})

const modelChatMemory = computed({
  get: () => applicationInfo.value.modelSetting?.chatMemory || 1,
  set: (value: number) => {
    if (!applicationInfo.value.modelSetting) {
      applicationInfo.value.modelSetting = {
        prompt: '',
        chatMemory: 1,
        system: '',
        noReferencesPrompt: '',
        referencesPrompt: '',
        modelConfig: {
          temperature: 0.7,
          maxTokens: 10240,
          topP: 0.95
        }
      }
    }
    applicationInfo.value.modelSetting.chatMemory = value
  }
})

// 获取已保存的模型列表
const fetchModelOptions = async () => {
  try {
    isLoadingModels.value = true
    const res = await modelApi.getSavedModels()
    modelOptions.value = res.data.models.map((model: any) => ({
      label: model.displayName,
      value: model.id
    }))
  } catch (error) {
    console.error('获取模型列表失败:', error)
  } finally {
    isLoadingModels.value = false
  }
}

// 加载应用数据
const loadApplicationData = async () => {
  if (!applicationId.value) return
  
  try {
    isLoading.value = true
    const res = await applicationApi.getApplicationById(applicationId.value)
    if (res.data) {
      // console.log(res.data.application)
      applicationInfo.value = { ...res.data.application }
    }
  } catch (error) {
    MsgError('加载应用数据失败')
  } finally {
    isLoading.value = false
  }
}

const createApplication = async() => {
  try {
    const res = await applicationApi.createApplication(applicationInfo.value)
    MsgSuccess('创建应用成功')
    router.push(`/application/${res.data.id}/setting`)
  } catch (error) {
    MsgError('创建应用失败')
  }
}

// 更新应用
const updateApplication = async() => {
  try {
    console.log(applicationInfo.value)
    await applicationApi.updateApplication(applicationInfo.value)
    MsgSuccess('更新应用成功')
  } catch (error) {
    MsgError('更新应用失败')
  }
}

/**
 * 打开会话，如果错误会以 ai 身份发送错误信息，并返回 undefined
 */
const openTempSession = async () => {
  try{
    const res = await applicationApi.openTempChat()
    tempSessionId.value = res.data.sessionId
  }catch(err){
    console.log(err)
    writeErrMessage(err)
  }
}

const writeErrMessage = (err: any, updateMessage?: ChatMessage ) => {
  if(updateMessage && updateMessage.role === 'assistant' && updateMessage.loading){
    // 更新最后一条消息为错误消息
    updateMessage.loading = false
    updateMessage.messageText = err.message
    updateMessage.isError = true
    aiChatRef.value.scrollToBottom()
    return
  }
  const errMessage: ChatMessage = {
    sessionId: undefined,
    messageIndex: chatMessages.value.length,
    role: 'assistant',
    messageText: err.message,
    loading: false,
    isError: true
  }
  chatMessages.value.push(errMessage)
  aiChatRef.value.scrollToBottom()
}

// 处理消息发送
const handleSendMessage = async (message: string) => {
  if (isMessageSending.value || !message.trim()) return
  isMessageSending.value = true
  // 判断是否打开会话
  if (!tempSessionId.value) {
    await openTempSession()
  }
  if(!tempSessionId.value){
    return
  }

  // 获取历史消息
  const chatHistories = chatMessages.value.filter(item => !item.isError && !item.loading)

  // 添加用户消息
  const userMessage: ChatMessage = {
    sessionId: tempSessionId.value,
    messageIndex: chatMessages.value.length,
    role: 'user',
    messageText: message.trim(),
  }
  chatMessages.value.push(userMessage)
  
  // 添加AI加载状态
  const aiMessage: ChatMessage = reactive({
    sessionId: tempSessionId.value,
    messageIndex: chatMessages.value.length,
    role: 'assistant',
    messageText: '',
    loading: true,
    toolExecutions: []  // 初始化工具调用数组
  })
  chatMessages.value.push(aiMessage)
  aiChatRef.value.scrollToBottom()

  
  // 发送消息
  applicationApi.postTempChatMessageStream(tempSessionId.value, {
    application: applicationInfo.value,
    chatMessage: userMessage,
    chatHistories: chatHistories
    },
     (data) => { 
      aiMessage.loading = false
      aiMessage.messageText = aiMessage.messageText + (data.message || '')
      
      // 处理工具调用
      if(data.toolExecution){
        if (!aiMessage.toolExecutions) {
          aiMessage.toolExecutions = []
        }
        // 检查是否已存在相同ID的工具调用，如果存在则更新，否则添加
        const existingIndex = aiMessage.toolExecutions.findIndex(te => te.request.id === data.toolExecution!.request.id)
        if (existingIndex >= 0) {
          aiMessage.toolExecutions[existingIndex] = data.toolExecution
        } else {
          aiMessage.toolExecutions.push(data.toolExecution)
        }
        console.log('工具调用:', data.toolExecution)
      }
      
      if(data.isEnd){
        // 记录 token
      }
      aiChatRef.value.scrollToBottom()
    },
     (err) => { 
      console.log(err)
      writeErrMessage(err, chatMessages.value[chatMessages.value.length - 1])
     }, () => { 
      isMessageSending.value = false
    })
}

// MCP设置相关状态
const sseServers = ref<SseTransport[]>([])
const stdioServers = ref<StdioTransport[]>([])
const mcpDialogVisible = ref(false)
const currentMcpType = ref<'sse' | 'stdio'>('sse')
const mcpEditIndex = ref(-1) // -1表示新增，>=0表示编辑
const currentSseServer = ref<SseTransport>({
  type: 'sse',
  sseUrl: '',
  header: {}
})
const currentStdioServer = ref<StdioTransport>({
  type: 'stdio',
  command: '',
  args: []
})
const currentHeaderKeys = ref<Record<string, string>>({})

// 计算属性
const totalMcpServers = computed(() => sseServers.value.length + stdioServers.value.length)
const allMcpServers = computed(() => [...sseServers.value, ...stdioServers.value])
const mcpDialogTitle = computed(() => {
  const typeText = currentMcpType.value === 'sse' ? 'SSE传输服务器' : 'STDIO传输服务器'
  return mcpEditIndex.value >= 0 ? `编辑${typeText}` : `添加${typeText}`
})

// 初始化MCP设置数据
const initMcpSetting = () => {
  if (applicationInfo.value.mcpSetting) {
    sseServers.value = applicationInfo.value.mcpSetting.sseServers || []
    stdioServers.value = applicationInfo.value.mcpSetting.stdioServers || []
  }
}

// 同步MCP设置到applicationInfo
const syncMcpSetting = () => {
  applicationInfo.value.mcpSetting = {
    sseServers: sseServers.value.length > 0 ? sseServers.value : undefined,
    stdioServers: stdioServers.value.length > 0 ? stdioServers.value : undefined
  }
}

// 重置当前编辑的服务器
const resetCurrentServer = () => {
  currentSseServer.value = {
    type: 'sse',
    sseUrl: '',
    header: {}
  }
  currentStdioServer.value = {
    type: 'stdio',
    command: '',
    args: []
  }
  currentHeaderKeys.value = {}
  mcpEditIndex.value = -1
}

// SSE服务器操作
const addSseServer = () => {
  resetCurrentServer()
  currentMcpType.value = 'sse'
  mcpDialogVisible.value = true
}

const editSseServer = (index: number) => {
  mcpEditIndex.value = index
  currentMcpType.value = 'sse'
  const server = sseServers.value[index]
  currentSseServer.value = JSON.parse(JSON.stringify(server))
  
  // 初始化header keys
  currentHeaderKeys.value = {}
  if (server.header) {
    Object.keys(server.header).forEach(key => {
      currentHeaderKeys.value[key] = key
    })
  }
  
  mcpDialogVisible.value = true
}

const removeSseServer = (index: number) => {
  sseServers.value.splice(index, 1)
  syncMcpSetting()
}

// STDIO服务器操作
const addStdioServer = () => {
  resetCurrentServer()
  currentMcpType.value = 'stdio'
  mcpDialogVisible.value = true
}

const editStdioServer = (index: number) => {
  mcpEditIndex.value = index
  currentMcpType.value = 'stdio'
  const server = stdioServers.value[index]
  currentStdioServer.value = JSON.parse(JSON.stringify(server))
  mcpDialogVisible.value = true
}

const removeStdioServer = (index: number) => {
  stdioServers.value.splice(index, 1)
  syncMcpSetting()
}

// 当前编辑的Header操作
const addCurrentHeader = () => {
  const newKey = `header-${Date.now()}`
  if (!currentSseServer.value.header) {
    currentSseServer.value.header = {}
  }
  currentSseServer.value.header[newKey] = ''
  currentHeaderKeys.value[newKey] = newKey
}

const removeCurrentHeader = (headerKey: string) => {
  if (currentSseServer.value.header) {
    delete currentSseServer.value.header[headerKey]
  }
  delete currentHeaderKeys.value[headerKey]
}

const updateCurrentHeaderKey = (oldKey: string, newKey: string) => {
  if (!newKey || oldKey === newKey) return
  
  if (currentSseServer.value.header && currentSseServer.value.header[oldKey] !== undefined) {
    const value = currentSseServer.value.header[oldKey]
    delete currentSseServer.value.header[oldKey]
    currentSseServer.value.header[newKey] = value
    
    delete currentHeaderKeys.value[oldKey]
    currentHeaderKeys.value[newKey] = newKey
  }
}

// 当前编辑的Args操作
const addCurrentArg = () => {
  if (!currentStdioServer.value.args) {
    currentStdioServer.value.args = []
  }
  currentStdioServer.value.args.push('')
}

const removeCurrentArg = (argIndex: number) => {
  if (currentStdioServer.value.args) {
    currentStdioServer.value.args.splice(argIndex, 1)
  }
}

// 保存MCP服务器
const saveMcpServer = () => {
  if (currentMcpType.value === 'sse') {
    if (!currentSseServer.value.sseUrl) {
      MsgError('请输入SSE URL')
      return
    }
    
    if (mcpEditIndex.value >= 0) {
      sseServers.value[mcpEditIndex.value] = { ...currentSseServer.value }
    } else {
      sseServers.value.push({ ...currentSseServer.value })
    }
  } else {
    if (!currentStdioServer.value.command) {
      MsgError('请输入命令')
      return
    }
    
    if (mcpEditIndex.value >= 0) {
      stdioServers.value[mcpEditIndex.value] = { ...currentStdioServer.value }
    } else {
      stdioServers.value.push({ ...currentStdioServer.value })
    }
  }
  
  syncMcpSetting()
  mcpDialogVisible.value = false
}

// 关闭弹窗
const handleMcpDialogClose = () => {
  mcpDialogVisible.value = false
  resetCurrentServer()
}

// 监听路由变化，重新加载数据
watch(() => route.params.id, (newId) => {
  if (newId && newId !== 'new') {
    loadApplicationData().then(() => {
      initMcpSetting()
    })
  }
}, { immediate: true })

// 组件挂载时获取数据
onMounted(() => {
  fetchModelOptions()
})

</script>

<style scoped lang="scss">
// 基础布局样式
.content-body {
  height: calc(100vh - 190px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.full-height {
  height: 100%;
}

.full-width {
  width: 100%;
}

// 左侧列样式
.left-column {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow-y: scroll;
  box-sizing: border-box;
}

// 右侧列样式
.right-column {
  border-left: 1px solid #e5e7eb;
}

// 区域标题样式
.section-header {
  margin-bottom: 16px;
  padding-left: 16px;
}

.section-title {
  margin: 0;
}

// 表单相关样式
.form-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.form-container {
  padding: 16px;
  padding-top: 0;
}

// Agent标题样式
.agent-title {
  margin: 0;
  padding-left: 12px;
}

// AI模型表单项样式
.ai-model-form-item {
  :deep(.el-form-item__label) {
    width: 100% !important;
    max-width: 100% !important;
    padding-right: 0 !important;
  }
}

// 模型标签样式
.model-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-bottom: -8px;
  
  .param-setting {
    color: var(--app-color-primary);
    &:hover {
      color: var(--app-color-primary-hover);
    }
  }
}

.model-select {
  width: 100%;
}

// 字数限制提示样式
.word-limit {
  color: #9ca3af;
  font-size: 12px;
  margin-left: 8px;
}

// 应用图标容器样式
.app-icon-container {
  user-select: none;
  position: relative;
  width: 30px;
  height: 30px;
  cursor: pointer;
  
  .edit-icon {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.4);
  }
}

// 聊天区域样式
.chat-area {
  height: calc(100vh - 230px);
  background-color: #eef0f4;
  border-radius: 8px;
  box-sizing: border-box;
  margin-left: 20px;
  display: flex;
  flex-direction: column;
}

.chat-container {
  margin-top: 24px;
  flex: 1;
  overflow-y: auto;
}

// 其他预览相关样式
.hint-label {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
  font-size: 14px;
}

.preview-container {
  background: #f3f4f6;
  border-radius: 8px;
  padding: 20px;
  height: 600px;
}

.chat-preview {
  margin-top: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.bot-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;

  .avatar-container {
    width: 40px;
    height: 40px;
    
    .avatar {
      width: 100%;
      height: 100%;
      background: #ec4899;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 20px;
    }
  }
}

// 开关相关样式
.switch-row {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  margin-top: -10px;
}

.switch-item {
  width: 100%;
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-top: 16px;
}

.switch-label {
  font-size: 14px;
}

// 深度选择器样式
:deep(.el-input__wrapper) {
  background-color: #fff;
}

// MCP设置样式
.mcp-setting-form-item {
  :deep(.el-form-item__label) {
    width: 100% !important;
    max-width: 100% !important;
    padding-right: 0 !important;
  }
}

.mcp-config-container {
  width: 100%;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  overflow: hidden;
}

.mcp-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e4e7ed;
  
  .mcp-count {
    font-size: 14px;
    color: #606266;
  }
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
  color: #909399;
  
  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }
  
  .empty-text {
    font-size: 16px;
    margin-bottom: 8px;
    color: #606266;
  }
  
  .empty-description {
    font-size: 14px;
  }
}

.mcp-server-list {
  padding: 16px;
}

.mcp-server-card {
  margin-bottom: 12px;
  border: 1px solid #e4e7ed;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .server-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .server-info {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .server-type {
        padding: 2px 8px;
        font-size: 12px;
        background: #f0f9ff;
        color: #0369a1;
        border-radius: 4px;
        font-weight: 500;
      }
      
      .server-title {
        font-weight: 500;
        color: #303133;
      }
    }
    
    .server-actions {
      display: flex;
      gap: 8px;
    }
  }
  
  .server-summary {
    padding: 8px 0;
    
    .summary-item {
      display: flex;
      margin-bottom: 4px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .label {
        width: 60px;
        font-size: 14px;
        color: #909399;
      }
      
      .value {
        flex: 1;
        font-size: 14px;
        color: #606266;
        word-break: break-all;
      }
    }
  }
}

// Popover样式
:deep(.mcp-add-popover) {
  padding: 8px 0 !important;
}

.mcp-type-options {
  .option-item {
    padding: 12px 16px;
    cursor: pointer;
    border-radius: 4px;
    margin: 4px 8px;
    transition: all 0.2s;
    
    &:hover {
      background: #f5f7fa;
    }
    
    .option-content {
      h6 {
        margin: 0 0 4px 0;
        font-size: 14px;
        font-weight: 500;
        color: #303133;
      }
      
      p {
        margin: 0;
        font-size: 12px;
        color: #909399;
        line-height: 1.4;
      }
    }
  }
}

// 弹窗内的表单样式
.header-config, .args-config {
  .header-item, .arg-item {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    
    .header-key, .header-value, .arg-input {
      flex: 1;
    }
    
    .header-key {
      max-width: 150px;
    }
  }
}
</style>

<template>
  <div class="chat-layout">
    <el-container class="chat-container">
      <!-- 侧边栏 -->
      <el-aside width="280px" class="chat-aside">
        <ChatSideBar 
          ref="sidebarRef"
          :chat-sessions="chatSessions"
          :active-session="currentChatSession"
          :loading="loadingSessions"
          @on-session-selected="handelSessionSelected"
          @on-new-session="handleNewSession"
          @on-delete-session="handleDeleteSession"
        />
      </el-aside>
      
      <!-- 主聊天区域 -->
      <el-main class="chat-main">
        <!-- 加载状态 -->
        <div v-if="loadingApplication" class="loading-area">
          <div class="loading-content">
            <el-icon size="40" class="is-loading"><Loading /></el-icon>
            <p>正在加载应用信息...</p>
          </div>
        </div>

        <!-- 聊天区域 -->
        <div v-else class="chat-area">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <h3>{{ currentApp.name || 'AI 助手' }}</h3>
            <el-button text @click="clearChat" :disabled="!isAiChatAvailable">
              <el-icon><Delete /></el-icon>
              清空对话
            </el-button>
          </div>

          <!-- AI聊天组件 -->
          <div class="chat-content">
            <AiChat
              ref="aiChatRef"
              :application="currentApp"
              :chat-messages="chatMessages"
              :disabled="!isAiChatAvailable"
              :loadingMessages="loadingMessages"
              :isMessageSending="isMessageSending"
              @send-message="handleSendMessage"
            />
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Delete, Loading} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import ChatSideBar from './components/ChatSideBar.vue'
import AiChat from '@/components/ai-chat/index.vue'
import chatSessionApi from '@/api/chatSession'
import type { ApplicationForm, ChatSession, ChatMessage } from '@/api/type/application'
import applicationApi from '@/api/application'
import { MsgError } from '@/utils/message'

const route = useRoute()
const router = useRouter()

// 响应式状态
const currentChatSession = ref<ChatSession | undefined>(undefined)
const chatSessions = ref<ChatSession[]>([])
const loadingSessions = ref(false)
const loadingApplication = ref(false)
const loadingMessages = ref(false)
const isMessageSending = ref(false)
const sidebarRef = ref()
const aiChatRef = ref()
const chatMessages = ref<ChatMessage[]>([])

// 获取applicationId
const applicationId = computed(() => {
  const id = parseInt(route.params.id as string)
  return isNaN(id) ? null : id
})

// 检查AiChat组件是否可用
const isAiChatAvailable = computed(() => {
  return !!(
    applicationId.value &&
    !loadingApplication.value &&
    currentApp.value.id
  )
})

const sessionIsOpen = computed(() => {
  return currentChatSession ? currentChatSession.value?.id !== undefined : false
})

// AI应用配置
const currentApp = ref<ApplicationForm>({
  id: undefined,
  name: '',
  description: '',
  prologue: '',
  modelId: undefined,
  icon: 'robot',
  applicationType: 'chat',
})

// 加载应用信息
const loadApplicationInfo = async () => {
  if (!applicationId.value) return
  try {
    const response = await applicationApi.getApplicationById(applicationId.value, loadingApplication)
    currentApp.value = response.data.application
  } catch (error) {
    MsgError('加载应用信息失败')
    console.error('加载应用信息失败:', error)
  }
}

// 加载历史会话
const loadChatSessions = async () => {
  if (!applicationId.value) return

  try {
    const response = await chatSessionApi.getChatSessions(applicationId.value, loadingSessions)
    chatSessions.value = response.data.sessions || []
  } catch (error) {
    console.error('加载聊天会话失败:', error)
    chatSessions.value = []
  }
}

// 根据路由参数加载对应会话
const loadSessionFromRoute = async () => {
  const sessionId = route.query.sessionId as string
  if (sessionId) {
    // 先在本地会话列表中查找
    const session = chatSessions.value.find(s => s.id!.toString() === sessionId)
    if (session) {
      currentChatSession.value = session
      loadSessionMessages()
    } else {
      // 如果本地没有找到
      currentChatSession.value = undefined
      console.error('加载会话失败')
      MsgError('加载会话失败')
      // 重定向到chat首页，清除无效的sessionId
      router.replace(`/application/chat/${applicationId.value}`)
    }
  }else{
    currentChatSession.value = undefined
  }
}

// 处理聊天选择
const handelSessionSelected = (session: ChatSession | null) => {
  if (session) {
    currentChatSession.value = session
    // 更新路由
    router.push({
      path: `/application/chat/${applicationId.value}`,
      query: { sessionId: session.id!.toString() }
    })
  } else {
    currentChatSession.value = undefined
    router.push(`/application/chat/${applicationId.value}`)
  }
}

// 处理新建聊天
const handleNewSession = async () => {
  try{
    const res = await chatSessionApi.openChatSession(applicationId.value!)
    chatSessions.value.unshift(res.data.session)
    router.replace({
      path: `/application/chat/${applicationId.value}`,
      query: { sessionId: res.data.session.id!.toString() }
    })
  }catch(err){
    console.log(err)
    MsgError("新建会话失败")
  }
  
}

// 处理删除聊天
const handleDeleteSession = async (sessionId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个会话吗？', '确认删除')
    await chatSessionApi.deleteSession(sessionId.toString())
    chatSessions.value = chatSessions.value.filter(s => s.id !== sessionId)

    if (currentChatSession.value?.id === sessionId) {
      currentChatSession.value = undefined
      router.push(`/application/chat/${applicationId.value}`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除会话失败:', error)
    }
  }
}

// 处理消息发送
const handleSendMessage = async (message: string) => {
  if (isMessageSending.value || !message.trim() || !isAiChatAvailable.value) return
  isMessageSending.value = true
  // 判断是否打开会话
  if (!sessionIsOpen.value) {
    await openSession()
  }
  if(!sessionIsOpen.value){
    return
  }

  // 添加用户消息
  const userMessage: ChatMessage = {
    sessionId: currentChatSession.value?.id,
    messageIndex: chatMessages.value.length,
    role: 'user',
    messageText: message.trim(),
  }
  chatMessages.value.push(userMessage)
  
  // 添加AI加载状态
  const aiMessage: ChatMessage = reactive({
    sessionId: currentChatSession.value?.id,
    messageIndex: chatMessages.value.length,
    role: 'assistant',
    messageText: '',
    loading: true,
    toolExecutions: [] 
  })
  chatMessages.value.push(aiMessage)
  aiChatRef.value.scrollToBottom()

  
  // 发送聊天请求
  chatSessionApi.postChatMessageStream(
    currentApp.value.id!,
    currentChatSession.value?.id!,
    userMessage,
    (data) => {
      aiMessage.loading = false
      aiMessage.messageText = aiMessage.messageText + (data.message || '')
      
      // 处理工具调用
      if(data.toolExecutionDetail){
        if (!aiMessage.toolExecutionDetail) {
          aiMessage.toolExecutionDetail = []
        }
        // 检查是否已存在相同ID的工具调用，如果存在则更新，否则添加
        const existingIndex = aiMessage.toolExecutionDetail.findIndex(te => te.request.id === data.toolExecutionDetail!.request.id)
        if (existingIndex >= 0) {
          aiMessage.toolExecutionDetail[existingIndex] = data.toolExecutionDetail
        } else {
          aiMessage.toolExecutionDetail.push(data.toolExecutionDetail)
        }
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
      console.log('完成')
      isMessageSending.value = false
    })
}

/**
 * 打开会话，如果错误会以 ai 身份发送错误信息，并返回 undefined
 */
const openSession = async () => {
  if(loadingApplication.value || !currentApp.value.id) return
  try{
    const res = await chatSessionApi.openChatSession(currentApp.value.id!)
    currentChatSession.value = res.data.session
  }catch(err){
    console.log(err)
    writeErrMessage(err)
  }
}

// 清空对话
const clearChat = async () => {
  console.log('清空对话')
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

// 加载会话消息
const loadSessionMessages = async () => {
  if (!currentChatSession.value) return

  try {
    // 调用API加载历史消息
    const response = await chatSessionApi.getSessionMessages(currentChatSession.value.id!, loadingMessages)
    chatMessages.value = response.data.messages || []
    console.log(chatMessages.value)
  } catch (error) {
    console.error('加载会话消息失败:', error)
    // 如果加载失败
    chatMessages.value = []
  }
}
// 监听路由变化 - 优化：监听整个 query 对象
watch(() => route.query, () => {
  loadSessionFromRoute()
}, { deep: true })

// 监听applicationId变化
watch(() => applicationId.value, async (newId, oldId) => {
  if (newId && newId !== oldId) {
    await loadApplicationInfo()
    await loadChatSessions()
    await loadSessionFromRoute()
  }
})

// 组件挂载时初始化
onMounted(async () => {
  if (applicationId.value) {
    await loadApplicationInfo()
    await loadChatSessions()
    await loadSessionFromRoute()
  }
})
</script>

<style scoped lang="scss">
.chat-layout {
  height: 89vh;
  background: #f0f2f5;
}

.chat-container {
  height: 100%;
}

.chat-main {
  padding: 0;
  display: flex;
  flex-direction: column;
}

.loading-area, .error-area, .welcome-area {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;

  .loading-content, .error-content, .welcome-content {
    text-align: center;

    h2 {
      margin: 16px 0 8px 0;
      color: #303133;
    }

    p {
      margin: 0 0 24px 0;
      color: #606266;
    }
  }
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid #e4e7ed;
  
  h3 {
    margin: 0;
    color: #303133;
  }
}

.chat-content {
  flex: 1;
  overflow: hidden;
  position: relative;

  .chat-disabled-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10;

    .disabled-content {
      text-align: center;

      p {
        margin: 12px 0 0 0;
        color: #606266;
        font-size: 14px;
      }
    }
  }
}
</style>

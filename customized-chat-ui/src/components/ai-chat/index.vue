<template>
  <div class="ai-chat-container">
    <!-- 聊天消息区域 -->
    <div class="chat-messages" ref="messagesContainer">
      
      <!-- 开场白 -->
      <div v-if="application.prologue && chatMessages.length === 0" class="greeting-message">
        <UserAvater name="icon-robot" class="ai-avatar" />
        <div class="message-content">
          <MdRenderer :source="application.prologue" />
        </div>
      </div>

      <!-- 聊天消息列表 -->
      <div v-for="(message, index) in chatMessages" :key="index" class="message-item">

        <!-- 用户消息 -->
        <div v-if="message.role === 'user'" class="user-message">
          <div class="message-content">
            <p>{{ message.messageText }}</p>
          </div>
          <UserAvater class="user-avatar" />
        </div>
        
        <!-- AI回复 -->
        <div v-else class="ai-message">
          <UserAvater name="icon-robot" class="ai-avatar" />
          <div class="message-content">
            <MdRenderer 
              v-if="message.messageText"
              :source="message.messageText"
            />
            <div v-if="message.loading" class="loading-indicator">
              <el-icon class="is-loading">
                <Loading />
              </el-icon>
              <span>AI正在思考中...</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 输入区域 -->
    <div class="chat-input-area">
      <div class="input-container">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="1"
          :autosize="{ minRows: 1, maxRows: 4 }"
          placeholder="请输入您的问题..."
          @keydown.enter.prevent="handleSend"
          :disabled="isLoading"
          class="message-input"
        />
        <el-button 
          type="primary" 
          @click="handleSend"
          :disabled="!inputMessage.trim() || isLoading"
          class="send-button"
        >
          <el-icon>
            <Right />
          </el-icon>
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, ref, nextTick, defineEmits, reactive } from 'vue'
import { Loading, Right } from '@element-plus/icons-vue'
import UserAvater from '@/components/avaters/user-avater.vue'
import MdRenderer from '@/components/markdown/MdRenderer.vue'
import applicationApi from '@/api/application'
import type { ApplicationForm, ChatMessage } from '@/api/type/application'

const props = withDefaults(defineProps<{
  application: ApplicationForm,
  type: 'debug' | 'normal'
}>(), {
  type: 'normal'
})

const emit = defineEmits<{
  sendMessage: [message: string]
}>()

// 响应式数据
const inputMessage = ref('')
const chatMessages = ref<ChatMessage[]>([])
const isLoading = ref(false)
const messagesContainer = ref<HTMLElement>()
const sessionId = ref<number | undefined>(undefined)

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 生成消息ID
const generateMessageId = () => {
  return Date.now().toString() + Math.random().toString(36).substr(2, 9)
}

// 发送消息
const sendMessage = async (message: string) => {
  if (!message.trim() || isLoading.value) return
  // 判断是否打开会话
  if (!sessionId.value) {
    await openChat()
  }
  if(!sessionId.value){
    return
  }
  // 添加用户消息
  const userMessage: ChatMessage = {
    sessionId: sessionId.value,
    messageIndex: chatMessages.value.length,
    role: 'user',
    messageText: message.trim(),
  }
  chatMessages.value.push(userMessage)
  
  // 添加AI加载状态
  const aiMessage: ChatMessage = reactive({
    sessionId: sessionId.value,
    messageIndex: chatMessages.value.length,
    role: 'assistant',
    messageText: '',
    loading: true
  })
  chatMessages.value.push(aiMessage)
  
  isLoading.value = true
  scrollToBottom()
  
  // 触发父组件事件
  emit('sendMessage', message.trim())

  // 发送消息
  applicationApi.postTempChatMessageStream(sessionId.value, {
    application: props.application,
    chatMessage: userMessage,
    chatHistories: chatMessages.value.filter(item => !item.isError)
    },
     (data) => { 
      aiMessage.loading = false
      console.log(data)
      aiMessage.messageText = aiMessage.messageText + data
      scrollToBottom()
    },
     (err) => { 
      console.log(err)
      writeErrMessage(err, chatMessages.value[chatMessages.value.length - 1])
     }, () => { 
      console.log('完成')
      isLoading.value = false
    })
}

// 处理发送按钮点击
const handleSend = () => {
  if (inputMessage.value.trim()) {
    sendMessage(inputMessage.value)
    inputMessage.value = ''
  }
}

// 暴露方法给父组件
const addAIResponse = (content: string) => {
  const lastMessage = chatMessages.value[chatMessages.value.length - 1]
  if (lastMessage && lastMessage.role === 'assistant' && lastMessage.loading) {
    lastMessage.loading = false
    lastMessage.messageText = content
    isLoading.value = false
    scrollToBottom()
  }
}

const setLoading = (loading: boolean) => {
  isLoading.value = loading
}

/**
 * 打开会话，如果错误会以 ai 身份发送错误信息，并返回 undefined
 */
const openChat = async () : Promise<number|undefined> => {
  try{
    if(props.type === 'debug'){
      const res = await applicationApi.openTempChat()
      sessionId.value = res.data.sessionId
      return res.data.sessionId
    }else{
      const res = await applicationApi.openTempChat()
      sessionId.value = res.data.sessionId
      return res.data.sessionId
    }
  }catch(err){
    console.log(err)
    writeErrMessage(err)
    return undefined
  }
}

const writeErrMessage = (err: any, updateMessage?: ChatMessage ) => {
  if(updateMessage && updateMessage.role === 'assistant' && updateMessage.loading){
    updateMessage.loading = false
    updateMessage.messageText = err.message
    updateMessage.isError = true
    isLoading.value = false
    scrollToBottom()
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
  isLoading.value = false
  scrollToBottom()
}

// 暴露给父组件使用
defineExpose({
  addAIResponse,
  setLoading
})
</script>

<style scoped lang="scss">
.ai-chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f8f9fa;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  
  .message-item {
    margin-bottom: 16px;
    
    .user-message {
      display: flex;
      justify-content: flex-end;
      align-items: flex-start;
      gap: 12px;
      
      .message-content {
        background-color: #3370ff;
        color: white;
        padding: 12px 16px;
        border-radius: 18px 18px 4px 18px;
        max-width: 70%;
        word-wrap: break-word;
        
        p {
          margin: 0;
          line-height: 1.5;
        }
      }
      
      .user-avatar {
        flex-shrink: 0;
      }
    }
    
    .ai-message {
      display: flex;
      justify-content: flex-start;
      align-items: flex-start;
      gap: 12px;
      
      .ai-avatar {
        flex-shrink: 0;
      }
      
      .message-content {
        background-color: white;
        padding: 12px 16px;
        border-radius: 18px 18px 18px 4px;
        max-width: 70%;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
        
        .loading-indicator {
          display: flex;
          align-items: center;
          gap: 8px;
          color: #666;
          font-size: 14px;
          
          .el-icon {
            color: #3370ff;
          }
        }
      }
    }
  }

  .greeting-message {
    display: flex;
    justify-content: flex-start;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 16px;

    .ai-avatar {
      flex-shrink: 0;
    }

    .message-content {
      background-color: white;
      padding: 12px 16px;
      border-radius: 18px 18px 18px 4px;
      max-width: 70%;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    }
  }
}

.chat-input-area {
  border-top: 1px solid #e0e6ed;
  background-color: white;
  padding: 16px;
  
  .input-container {
    display: flex;
    gap: 12px;
    align-items: flex-end;
    
    .message-input {
      flex: 1;
      
      :deep(.el-textarea__inner) {
        border-radius: 20px;
        padding: 12px 16px;
        border: 1px solid #dcdfe6;
        resize: none;
        
        &:focus {
          border-color: #3370ff;
        }
      }
    }
    
    .send-button {
      border-radius: 20px;
      padding: 12px 20px;
      height: auto;
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }
}

// 自定义滚动条样式
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>

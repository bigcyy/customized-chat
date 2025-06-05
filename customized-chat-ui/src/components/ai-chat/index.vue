<template>
  <div class="ai-chat-container">
    <!-- 聊天消息区域 -->
    <div class="chat-messages" ref="messagesContainer">
      
      <!-- 开场白 -->
      <div v-if="application.greeting && chatMessages.length === 0" class="greeting-message">
        <UserAvater name="icon-robot" class="ai-avatar" />
        <div class="message-content">
          <MdRenderer :source="application.greeting" />
        </div>
      </div>

      <!-- 聊天消息列表 -->
      <div v-for="message in chatMessages" :key="message.id" class="message-item">

        <!-- 用户消息 -->
        <div v-if="message.type === 'user'" class="user-message">
          <div class="message-content">
            <p>{{ message.content }}</p>
          </div>
          <UserAvater class="user-avatar" />
        </div>
        
        <!-- AI回复 -->
        <div v-else class="ai-message">
          <UserAvater name="icon-robot" class="ai-avatar" />
          <div class="message-content">
            <MdRenderer 
              v-if="message.content"
              :source="message.content"
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
import { defineProps, ref, nextTick, defineEmits } from 'vue'
import { Loading, Right } from '@element-plus/icons-vue'
import UserAvater from '@/components/avaters/user-avater.vue'
import MdRenderer from '@/components/markdown/MdRenderer.vue'

interface ChatMessage {
  id: string
  type: 'user' | 'ai'
  content: string
  timestamp: number
  loading?: boolean
}

const props = defineProps<{
  application: any
}>()

const emit = defineEmits<{
  sendMessage: [message: string]
}>()

// 响应式数据
const inputMessage = ref('')
const chatMessages = ref<ChatMessage[]>([])
const isLoading = ref(false)
const messagesContainer = ref<HTMLElement>()

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

// 处理提示问题选择
const handleTipSelect = (tip: string) => {
  sendMessage(tip)
}

// 发送消息
const sendMessage = (message: string) => {
  if (!message.trim() || isLoading.value) return
  
  // 添加用户消息
  const userMessage: ChatMessage = {
    id: generateMessageId(),
    type: 'user',
    content: message.trim(),
    timestamp: Date.now()
  }
  chatMessages.value.push(userMessage)
  
  // 添加AI加载状态
  const aiMessage: ChatMessage = {
    id: generateMessageId(),
    type: 'ai',
    content: '',
    timestamp: Date.now(),
    loading: true
  }
  chatMessages.value.push(aiMessage)
  
  isLoading.value = true
  scrollToBottom()
  
  // 触发父组件事件
  emit('sendMessage', message.trim())
  
  // 模拟AI回复 (实际项目中这里应该调用API)
  setTimeout(() => {
    aiMessage.loading = false
    aiMessage.content = generateMockResponse(message)
    isLoading.value = false
    scrollToBottom()
  }, 2000)
}

// 处理发送按钮点击
const handleSend = () => {
  if (inputMessage.value.trim()) {
    sendMessage(inputMessage.value)
    inputMessage.value = ''
  }
}

// 模拟AI回复 (仅用于演示)
const generateMockResponse = (userMessage: string): string => {
  const responses = [
    `您好！关于"**${userMessage}**"这个问题，我来为您详细解答。

这是一个很好的问题。根据我的理解，我可以从以下几个方面来回答：

1. **基本概念**：首先需要了解相关的基础知识
2. **具体操作**：然后是具体的操作步骤  
3. **注意事项**：最后是一些需要注意的地方

希望这个回答对您有帮助！如果您还有其他问题，请随时告诉我。`,
    
    `感谢您的提问！针对"**${userMessage}**"，我建议您可以：

- 查看相关文档
- 参考最佳实践
- 进行实际测试

如果您需要更多帮助，请告诉我具体的使用场景。`,
    
    `关于"**${userMessage}**"的问题，这确实是一个常见的需求。

\`\`\`javascript
// 示例代码
function example() {
  console.log('这是一个示例');
}
\`\`\`

如果您需要更多帮助，请告诉我具体的使用场景。`
  ]
  
  return responses[Math.floor(Math.random() * responses.length)]
}

// 暴露方法给父组件
const addAIResponse = (content: string) => {
  const lastMessage = chatMessages.value[chatMessages.value.length - 1]
  if (lastMessage && lastMessage.type === 'ai' && lastMessage.loading) {
    lastMessage.loading = false
    lastMessage.content = content
    isLoading.value = false
    scrollToBottom()
  }
}

const setLoading = (loading: boolean) => {
  isLoading.value = loading
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

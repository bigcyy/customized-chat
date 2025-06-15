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
          :disabled="props.disabled"
          class="message-input"
        />
        <el-button
          type="primary"
          @click="handleSend"
          :disabled="!inputMessage.trim() || isMessageSending || disabled"
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
import { defineProps, ref, nextTick, defineEmits, reactive, watch, computed } from 'vue'
import { Loading, Right } from '@element-plus/icons-vue'
import UserAvater from '@/components/avaters/user-avater.vue'
import MdRenderer from '@/components/markdown/MdRenderer.vue'
import type { ApplicationForm, ChatMessage } from '@/api/type/application'

const props = withDefaults(defineProps<{
  application: ApplicationForm,
  chatMessages: ChatMessage[],
  loadingMessages?: boolean,
  disabled?: boolean,
  isMessageSending: boolean
}>(), {
  type: 'normal',
  loadingMessages: false,
  disabled: false,
})

const emit = defineEmits<{
  sendMessage: [message: string]
}>()

// 响应式数据
const inputMessage = ref('')
const messagesContainer = ref<HTMLElement>()

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 处理发送按钮点击
const handleSend = () => {
  if (!inputMessage.value.trim() || props.isMessageSending || props.disabled) return
  emit('sendMessage', inputMessage.value.trim())
  inputMessage.value = ''
}

// 暴露给父组件使用
defineExpose({
  scrollToBottom
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

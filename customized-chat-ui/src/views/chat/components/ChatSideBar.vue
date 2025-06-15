<template>
  <div class="chat-sidebar">
    <!-- 头部 -->
    <div class="sidebar-header">
      <h3>聊天记录</h3>
      <el-button 
        type="primary" 
        size="small" 
        @click="emit('onNewSession')"
        class="new-chat-btn"
        :loading="loading"
      >
        <el-icon><Plus /></el-icon>
        新对话
      </el-button>
    </div>

    <!-- 聊天列表 -->
    <div class="chat-list" v-loading="loading">
      <div 
        v-for="session in chatSessions" 
        :key="session.id "
        :class="['chat-item', { active: session.id === activeSession?.id }]"
        @click="emit('onSessionSelected', session)"
      >
        <div class="chat-info">
          <h4>{{ session.chatAbstract || '新对话' }}</h4>
          <p>{{ formatTime(session.updateTime) }}</p>
        </div>
        <el-button
          text
          size="small"
          @click.stop="emit('onDeleteSession', session.id!)"
          class="delete-btn"
        >
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
      
      <!-- 空状态 -->
      <div v-if="!loading && chatSessions.length === 0" class="empty-state">
        <el-empty description="暂无聊天记录"/>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Plus, Delete } from '@element-plus/icons-vue'
import type { ChatSession } from '@/api/type/application'

const props = defineProps<{
  chatSessions: ChatSession[]
  activeSession: ChatSession | undefined
  loading: boolean
}>()

const emit = defineEmits<{
  onSessionSelected: [session: ChatSession | null]
  onNewSession: []
  onDeleteSession: [sessionId: number]
}>()

// 格式化时间显示
const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }
  // 小于1小时
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }
  // 小于1天
  if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  }
  // 大于1天
  return date.toLocaleDateString()
}
</script>

<style scoped lang="scss">
.chat-sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  border-right: 1px solid #e4e7ed;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  background: white;
  
  h3 {
    margin: 0 0 12px 0;
    font-size: 16px;
    color: #303133;
  }
  
  .new-chat-btn {
    width: 100%;
  }
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  
  .chat-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px;
    margin-bottom: 4px;
    border-radius: 8px;
    cursor: pointer;
    background: white;
    transition: all 0.2s;
    
    &:hover {
      background: #f0f9ff;
      
      .delete-btn {
        opacity: 1;
      }
    }
    
    &.active {
      background: #e3f2fd;
      border-left: 3px solid #409eff;
    }
    
    .chat-info {
      flex: 1;
      min-width: 0;
      
      h4 {
        margin: 0 0 4px 0;
        font-size: 14px;
        color: #303133;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      p {
        margin: 0;
        font-size: 12px;
        color: #909399;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    
    .delete-btn {
      opacity: 0;
      transition: opacity 0.2s;
      color: #f56c6c;
    }
  }
  
  .empty-state {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 300px;
  }
}
</style>

<template>
  <div>
    <div class="flex-between mb-16">
      <h3>设置</h3>
      <div><el-button type="primary">保存并发布</el-button></div>
    </div>
    <div class="content-body">
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
                    <span class="word-limit">{{ applicationInfo.description.length }}/256</span>
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
                    v-model="applicationInfo.aiModel"
                    placeholder="请选择 AI 模型"
                    class="full-width"
                  >
                    <el-option label="模型1" value="model1" />
                    <el-option label="模型2" value="model2" />
                  </el-select>
                </div>
              </el-form-item>
              <el-form-item label="角色设定">
                <el-input
                  v-model="applicationInfo.rolePrompt"
                  type="textarea"
                  placeholder="你是 xxx 小助手"
                  :rows="4"
                  resize="none"
                  :input-style="{ backgroundColor: '#fff' }"
                />
              </el-form-item>
              <el-form-item label="历史聊天记录">
                <el-input-number
                  v-model="applicationInfo.historyCount"
                  placeholder="请输入历史聊天记录"
                  controls-position="right"
                  class="full-width"
                />
              </el-form-item>
              <el-form-item class="mb-0" label="开场白">
                <el-input
                  v-model="applicationInfo.greeting"
                  type="textarea"
                  placeholder="您好，我是 xxx 小助手，您可以向我提出 xxx 使用问题。"
                  resize="none"
                  :input-style="{ backgroundColor: '#fff' }"
                  :rows="4"
                />
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
              <AiChat :application="applicationInfo" />
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const showIconEdit = ref(false)

const applicationInfo = ref({
  name: '',
  description: '',
  aiModel: '',
  rolePrompt: '',
  prompt: '{question}',
  historyCount: 1,
  promptWithKnowledge: `已知信息：{data}\n用户问题：{question}\n回答要求：\n - 请使用中文回答用户问题`,
  greeting: `您好，我是 XXX 小助手，您可以向我提出 XXX 使用问题。\n- XXX 主要功能有什么？\n- XXX 如何收费？\n- 需要转人工服务`,
  outputThinking: false,
  voiceInput: false,
  voiceBroadcast: false
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
</style>

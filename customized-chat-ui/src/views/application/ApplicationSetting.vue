<template>
  <div>
    <div class="flex-between mb-16">
      <h3>设置</h3>
      <div><el-button type="primary">保存并发布</el-button></div>
    </div>
    <div class="content-body">
      <el-row style="height: 100%">
        <el-col
          :span="10"
          style="
            height: 100%;
            display: flex;
            flex-direction: column;
            overflow-y: scroll;
            /* margin-right: 2px; */
            box-sizing: border-box;
          "
          class="custom-scrollbar"
        >
          <div class="mb-16" style="padding-left: 16px">
            <h4 class="title-decoration-1" style="margin: 0">应用信息</h4>
          </div>
          <div style="flex: 1; display: flex; flex-direction: column">
            <el-form
              :model="applicationInfo"
              label-position="top"
              class="p-16"
              style="padding-top: 0"
            >
              <el-form-item label="应用名称" required>
                <el-input
                  v-model="applicationInfo.name"
                  placeholder="请输入应用名称"
                  maxlength="64"
                  :input-style="{ backgroundColor: '#fff' }"
                >
                  <template #append>
                    <span class="word-limit">{{ applicationInfo.name.length }}/64</span>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="应用描述">
                <el-input
                  v-model="applicationInfo.description"
                  type="textarea"
                  placeholder="请输入应用描述"
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
                    style="width: 100%"
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
              <el-form-item class="mb-0">
                <template #label>
                  <div class="hint-label">
                    <span>提示词（无引用知识库）</span>
                    <el-tooltip content="这是一个提示" placement="top">
                      <el-icon><QuestionFilled /></el-icon>
                    </el-tooltip>
                  </div>
                </template>
                <el-input
                  v-model="applicationInfo.prompt"
                  type="textarea"
                  placeholder="{question}"
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
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item class="mb-0">
                <template #label>
                  <div class="hint-label">
                    <span>提示词（引用知识库）</span>
                    <el-tooltip content="这是一个提示" placement="top">
                      <el-icon><QuestionFilled /></el-icon>
                    </el-tooltip>
                  </div>
                </template>
                <el-input
                  v-model="applicationInfo.promptWithKnowledge"
                  type="textarea"
                  placeholder="{question}"
                  :rows="4"
                  resize="none"
                  :input-style="{ backgroundColor: '#fff' }"
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

              <div class="switch-row">
                <div class="switch-item">
                  <span class="switch-label">输出思考</span>
                  <el-switch v-model="applicationInfo.outputThinking" />
                </div>

                <div class="switch-item">
                  <span class="switch-label">语音输入</span>
                  <el-switch v-model="applicationInfo.voiceInput" />
                </div>

                <div class="switch-item">
                  <span class="switch-label">语音播放</span>
                  <el-switch v-model="applicationInfo.voiceBroadcast" />
                </div>
              </div>
            </el-form>
          </div>
        </el-col>
        <el-col :span="14" style="border-left: 1px solid #e5e7eb">
          <div class="mb-16" style="padding-left: 16px">
            <h4 class="title-decoration-1" style="margin: 0">调试预览</h4>
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
                <ColorAvater name="应用" pinyinColor shape="square" />
              </div>
              <h4 style="margin: 0; padding-left: 12px">
                {{ applicationInfo.name || '应用名称' }}
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
import { QuestionFilled } from '@element-plus/icons-vue'

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
.content-body {
  height: calc(100vh - 190px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ai-model-form-item {
  :deep(.el-form-item__label) {
    width: 100% !important;
    max-width: 100% !important;
    padding-right: 0 !important;
  }
}

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

.chat-container {
  margin-top: 24px;
  height: 100%;
  overflow: scroll;
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

.word-limit {
  color: #9ca3af;
  font-size: 12px;
  margin-left: 8px;
}

:deep(.el-input__wrapper) {
  background-color: #fff;
}

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

.chat-area {
  height: calc(100vh - 230px);
  background-color: #eef0f4;
  border-radius: 8px;
  box-sizing: border-box;
  margin-left: 20px;
}
</style>

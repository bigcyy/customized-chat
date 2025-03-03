<template>
  <div>
    <div class="flex-between">
      <h3>设置</h3>
      <div><el-button type="primary">保存并发布</el-button></div>
    </div>
    <div class="content-body">
      <el-row>
        <el-col :span="10">
          <div class="mb-16" style="padding-left: 16px">
            <h4 class="title-decoration-1" style="margin: 0">应用信息</h4>
          </div>
          <div>
            <el-scrollbar height="450px">
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
                    rows="4"
                    resize="none"
                    :input-style="{ backgroundColor: '#fff' }"
                  >
                    <template #append>
                      <span class="word-limit">{{ applicationInfo.description.length }}/256</span>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item label="AI 模型">
                  <div class="model-select">
                    <el-select
                      v-model="applicationInfo.aiModel"
                      placeholder="请选择 AI 模型"
                      class="w-full"
                    >
                      <el-option label="模型1" value="model1" />
                      <el-option label="模型2" value="model2" />
                    </el-select>
                    <a class="param-setting">参数设置</a>
                  </div>
                </el-form-item>
                <el-form-item label="角色设定">
                  <el-input
                    v-model="applicationInfo.rolePrompt"
                    type="textarea"
                    placeholder="你是 xxx 小助手"
                    rows="4"
                    resize="none"
                    :input-style="{ backgroundColor: '#fff' }"
                  />
                </el-form-item>
                <el-form-item label="提示词" class="mb-0">
                  <div class="hint-label">
                    <span>（无引用知识库）</span>
                    <el-tooltip content="这是一个提示" placement="top">
                      <el-icon><QuestionFilled /></el-icon>
                    </el-tooltip>
                  </div>
                  <el-input
                    v-model="applicationInfo.prompt"
                    type="textarea"
                    placeholder="{question}"
                    rows="4"
                    resize="none"
                    :input-style="{ backgroundColor: '#fff' }"
                  />
                </el-form-item>
              </el-form>
            </el-scrollbar>
          </div>
        </el-col>
        <el-col :span="14">
          <div class="mb-16" style="padding-left: 16px">
            <h4 class="title-decoration-1" style="margin: 0">调试预览</h4>
          </div>
          <div class="p-16">
            <div class="flex align-center">
              <ColorAvater name="advance" pinyinColor shape="square" />
              <h4 style="margin: 0; padding-left: 12px">
                {{ applicationInfo.name || '应用名称' }}
              </h4>
            </div>
            <el-scrollbar height="400px">
              <div class="chat-preview">
                <div class="chat-content">
                  <div class="message-box">
                    <div class="bot-message">
                      您好，我是 {{ applicationInfo.name || 'XXX' }} 小助手，您可以向我提出
                      {{ applicationInfo.name || 'XXX' }} 使用问题。
                    </div>
                    <div class="suggested-questions">
                      <div class="question-item">
                        <i class="question-icon">📝</i>
                        <span>{{ applicationInfo.name || 'XXX' }} 主要功能有什么？</span>
                      </div>
                      <div class="question-item">
                        <i class="question-icon">📝</i>
                        <span>{{ applicationInfo.name || 'XXX' }} 如何收费？</span>
                      </div>
                      <div class="question-item">
                        <i class="question-icon">📝</i>
                        <span>需要转人工服务</span>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="chat-input">
                  <div class="input-hint">请输入问题，Ctrl+Enter 换行，Enter发送</div>
                  <div class="send-icon">
                    <el-icon><Position /></el-icon>
                  </div>
                </div>
              </div>
            </el-scrollbar>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { QuestionFilled, Position } from '@element-plus/icons-vue'

const applicationInfo = ref({
  name: '',
  description: '',
  aiModel: '',
  rolePrompt: '',
  prompt: '{question}'
})
</script>

<style scoped lang="scss">
.model-select {
  display: flex;
  align-items: center;
  gap: 12px;

  .param-setting {
    color: #3b82f6;
    text-decoration: none;
    font-size: 14px;
  }
}

.hint-label {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
  color: #6b7280;
  font-size: 14px;
}

.preview-container {
  background: #f3f4f6;
  border-radius: 8px;
  padding: 20px;
  height: 600px;
}

.chat-preview {
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

.chat-content {
  flex: 1;
  overflow-y: auto;
}

.message-box {
  background: white;
  border-radius: 8px;
  padding: 16px;

  .bot-message {
    color: #374151;
    margin-bottom: 16px;
  }
}

.suggested-questions {
  .question-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px;
    margin: 4px 0;
    color: #6b7280;
    cursor: pointer;
    border-radius: 4px;
    background: #f3f4f6;

    &:hover {
      background: #e5e7eb;
    }

    .question-icon {
      font-style: normal;
    }
  }
}

.chat-input {
  margin-top: 16px;
  background: white;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .input-hint {
    color: #9ca3af;
    font-size: 14px;
  }

  .send-icon {
    color: #9ca3af;
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
</style>

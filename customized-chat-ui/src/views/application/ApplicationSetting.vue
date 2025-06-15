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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Edit } from '@element-plus/icons-vue'
import type { ApplicationForm, ChatMessage } from '@/api/type/application'
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
    loading: true
  })
  chatMessages.value.push(aiMessage)
  aiChatRef.value.scrollToBottom()

  
  // 发送消息
  applicationApi.postTempChatMessageStream(tempSessionId.value, {
    application: applicationInfo.value,
    chatMessage: userMessage,
    chatHistories: chatMessages.value.filter(item => !item.isError)
    },
     (data) => { 
      aiMessage.loading = false
      console.log(data)
      aiMessage.messageText = aiMessage.messageText + data
      aiChatRef.value.scrollToBottom()
    },
     (err) => { 
      console.log(err)
      writeErrMessage(err, chatMessages.value[chatMessages.value.length - 1])
     }, () => { 
      console.log('完成')
      isLoading.value = false
    })
}



// 监听路由变化，重新加载数据
watch(() => route.params.id, (newId) => {
  if (newId && newId !== 'new') {
    loadApplicationData()
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
</style>

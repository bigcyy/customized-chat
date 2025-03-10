<template>
  <div class="chat-input-operate">
    <div class="text-area">
      <div class="flex">
        <el-input
          ref="quickInputRef"
          v-model="inputValue"
          placeholder="请输入问题，Ctrl+Enter 换行，Enter发送"
          :autosize="{ minRows: 1, maxRows: 10 }"
          type="textarea"
          :maxlength="100000"
          @keydown.enter="sendChatHandle($event)"
        />

        <div class="flex align-center">
          <el-button
            text
            class="sent-button"
            :disabled="isDisabledChat || loading"
            @click="sendChatHandle"
          >
            <el-icon><Promotion /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { chatType } from '@/api/type/application'

const props = defineProps<{
  applicationDetails: any
  loading: boolean
  sendMessage: (question: string, other_params_data?: any, chat?: chatType) => void
}>()

const isDisabledChat = computed(
  () =>
    !(
      inputValue.value.trim() &&
      (props.applicationDetails?.appId || props.applicationDetails?.name)
    )
)

const inputValue = ref('')
const quickInputRef = ref()

const sendMessage = () => {
  props.sendMessage(inputValue.value)
  inputValue.value = ''
  quickInputRef.value.textareaStyle.height = '45px'
}

const sendChatHandle = (event?: any) => {
  if (!event?.ctrlKey) {
    // 如果没有按下组合键ctrl，则会阻止默认事件
    event?.preventDefault()
    if (!isDisabledChat.value && !props.loading && !event?.isComposing) {
      if (inputValue.value.trim()) {
        sendMessage()
      }
    }
  } else {
    // 如果同时按下ctrl+回车键，则会换行
    inputValue.value += '\n'
  }
}
</script>

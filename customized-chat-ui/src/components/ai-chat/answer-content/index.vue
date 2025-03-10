<template>
  <div class="item-content mb-16 lighter">
    <template v-for="(answer_text, index) in answer_text_list" :key="index">
      <div class="avatar">
        <el-image
          v-if="application.avatar"
          :src="application.avatar"
          alt=""
          fit="cover"
          style="width: 32px; height: 32px; display: block"
        />
        <ColorAvater height="32px" width="32px" pingying v-else> </ColorAvater>
      </div>
      <div class="content">
        <el-card shadow="always" class="mb-8 border-r-8">
          <MdRenderer
            v-if="
              (chatRecord.write_ed === undefined || chatRecord.write_ed === true) &&
              answer_text.length == 0
            "
            source="抱歉，没有查找到相关内容，请重新描述您的问题或提供更多信息。"
          ></MdRenderer>
          <template v-else-if="answer_text.length > 0">
            <MdRenderer
              v-for="(answer, index) in answer_text"
              :key="index"
              :chat_record_id="answer.chat_record_id"
              :child_node="answer.child_node"
              :runtime_node_id="answer.runtime_node_id"
              :reasoning_content="answer.reasoning_content"
              :disabled="loading || type == 'log'"
              :source="answer.content"
            ></MdRenderer>
          </template>
          <span v-else-if="chatRecord.is_stop" shadow="always"> 已停止回答 </span>
          <span v-else shadow="always"> 回答中 <span class="dotting"></span> </span>
        </el-card>
      </div>
    </template>
  </div>
</template>
<script setup lang="ts">
import MdRenderer from '@/components/markdown/MdRenderer.vue'
import { type chatType } from '@/api/type/application'
import { computed } from 'vue'
import ColorAvater from '@/components/color-avater/index.vue'
const props = defineProps<{
  chatRecord: chatType
  application: any
  loading: boolean
  type: 'log' | 'ai-chat' | 'debug-ai-chat'
}>()

const answer_text_list = computed(() => {
  return props.chatRecord.answer_text_list.map((item) => {
    if (typeof item == 'string') {
      return [
        {
          content: item,
          chat_record_id: undefined,
          child_node: undefined,
          runtime_node_id: undefined,
          reasoning_content: undefined
        }
      ]
    } else if (item instanceof Array) {
      return item
    } else {
      return [item]
    }
  })
})
</script>
<style lang="scss" scoped></style>

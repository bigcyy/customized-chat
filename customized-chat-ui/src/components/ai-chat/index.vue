<template>
  <div>
    <div class="chat-content">
      <el-scrollbar>
        <template v-for="(item, index) in chatList" :key="index">
          <!-- 问题 -->
          <QuestionContent
            :type="type"
            :application="applicationDetails"
            :chat-record="item"
          ></QuestionContent>
          <!-- 回答 -->
          <AnswerContent
            :application="applicationDetails"
            :loading="loading"
            v-model:chat-record="chatList[index]"
            :type="type"
          ></AnswerContent>
        </template>
      </el-scrollbar>
    </div>
    <div class="chat-input">
      <ChatInputOperate
        :applicationDetails="applicationDetails"
        :sendMessage="sendMessage"
        v-model:loading="loading"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import ChatInputOperate from './chat-input-operate/index.vue'
import type { chatType } from '@/api/type/application'
import { ref, reactive } from 'vue'
import applicationApi from '@/api/application'
import { debounce } from 'lodash'
import { randomId } from '@/utils/utils'

const loading = ref(false)
const chatList = ref<any[]>([])
const inputValue = ref('')
const chatId = ref('')

const props = withDefaults(
  defineProps<{
    applicationDetails: any
    type?: 'log' | 'ai-chat' | 'debug-ai-chat'
    chatId?: string
  }>(),
  {
    applicationDetails: () => ({}),
    type: 'ai-chat',
    chatId: 'new'
  }
)

const emit = defineEmits(['refresh'])

function sendMessage(val: string, other_params_data?: any, chat?: chatType) {
  if (!loading.value && props.applicationDetails?.name) {
    handleDebounceClick(val, other_params_data, chat)
  }
}

/**
 * 获取一个递归函数,处理流式数据
 * @param chat    每一条对话记录
 * @param reader  流数据
 * @param stream  是否是流式数据
 */
const getWrite = (chat: any, reader: any, stream: boolean) => {
  let tempResult = ''
  /**
   *
   * @param done  是否结束
   * @param value 值
   */
  const write_stream = ({ done, value }: { done: boolean; value: any }) => {
    try {
      if (done) {
        ChatManagement.close(chat.id)
        return
      }
      const decoder = new TextDecoder('utf-8')
      let str = decoder.decode(value, { stream: true })
      // 这里解释一下 start 因为数据流返回流并不是按照后端chunk返回 我们希望得到的chunk是data:{xxx}\n\n 但是它获取到的可能是 data:{ -> xxx}\n\n 总而言之就是 fetch不能保证每个chunk都说以data:开始 \n\n结束
      tempResult += str
      const split = tempResult.match(/data:.*}\n\n/g)
      if (split) {
        str = split.join('')
        tempResult = tempResult.replace(str, '')
      } else {
        return reader.read().then(write_stream)
      }
      // 这里解释一下 end
      if (str && str.startsWith('data:')) {
        if (split) {
          for (const index in split) {
            const chunk = JSON?.parse(split[index].replace('data:', ''))
            chat.chat_id = chunk.chat_id
            chat.record_id = chunk.chat_record_id
            if (!chunk.is_end) {
              ChatManagement.appendChunk(chat.id, chunk)
            }
            if (chunk.is_end) {
              // 流处理成功 返回成功回调
              return Promise.resolve()
            }
          }
        }
      }
    } catch (e) {
      return Promise.reject(e)
    }
    return reader.read().then(write_stream)
  }
  /**
   * 处理 json 响应
   * @param param0
   */
  const write_json = ({ done, value }: { done: boolean; value: any }) => {
    if (done) {
      const result_block = JSON.parse(tempResult)
      if (result_block.code === 500) {
        return Promise.reject(result_block.message)
      } else {
        if (result_block.content) {
          ChatManagement.append(chat.id, result_block.content)
        }
      }
      ChatManagement.close(chat.id)
      return
    }
    if (value) {
      const decoder = new TextDecoder('utf-8')
      tempResult += decoder.decode(value)
    }
    return reader.read().then(write_json)
  }
  return stream ? write_stream : write_json
}

function chatMessage(chat?: any, problem?: string, re_chat?: boolean, other_params_data?: any) {
  loading.value = true
  if (!chat) {
    chat = reactive({
      id: randomId(),
      problem_text: problem ? problem : inputValue.value.trim(),
      answer_text: '',
      answer_text_list: [[]],
      buffer: [],
      reasoning_content: '',
      reasoning_content_buffer: [],
      write_ed: false,
      is_stop: false,
      record_id: '',
      chat_id: '',
      vote_status: '-1',
      status: undefined
    })
    // chat list 和 Manager的区别?
    chatList.value.push(chat)
    inputValue.value = ''
  }
  if (!chatId.value) {
    openChatAndSendMessage(chat, problem, re_chat, other_params_data).catch(() => {
      errorWrite(chat)
    })
  } else {
    const obj = {
      id: randomId(),
      message: chat.problem_text,
      re_chat: re_chat || false,
      ...other_params_data,
      form_data: {
        ...form_data.value,
        ...api_form_data.value
      }
    }
    // 对话
    applicationApi
      .postChatMessage(chatId.value, obj)
      .then((response) => {
        const reader = response.body.getReader()
        // 处理流数据
        const write = getWrite(
          chat,
          reader,
          response.headers.get('Content-Type') !== 'application/json'
        )
        return reader.read().then(write)
      })
      .then(() => {
        if (props.chatId === 'new') {
          emit('refresh', chatId.value)
        }
      })
      .finally(() => {
        ChatManagement.close(chat.id)
      })
      .catch((e: any) => {
        errorWrite(chat, e + '')
      })
  }
}

const openChatAndSendMessage = (
  chat?: any,
  problem?: string,
  re_chat?: boolean,
  other_params_data?: any
) => {
  return openChat().then(() => {
    chatMessage(chat, problem, re_chat, other_params_data)
  })
}

/**
 * 打开对话id
 */
const openChat: () => Promise<string> = () => {
  if (props.applicationDetails?.id) {
    return applicationApi.openChat(props.applicationDetails?.id).then((res) => {
      chatId.value = res.data.chatId
      return res.data.chatId
    })
  } else {
    const obj = props.applicationDetails
    return applicationApi.openTempChat(obj).then((res) => {
      chatId.value = res.data
      return res.data
    })
  }
}

const errorWrite = (chat: any, message?: string) => {
  ChatManagement.addChatRecord(chat, 50, loading)
  ChatManagement.write(chat.id)
  ChatManagement.append(chat.id, message || '抱歉，当前正在维护，无法提供服务，请稍后再试！')
  ChatManagement.updateStatus(chat.id, 500)
  ChatManagement.close(chat.id)
}

const handleDebounceClick = debounce((val, other_params_data?: any, chat?: chatType) => {
  chatMessage(chat, val, false, other_params_data)
}, 200)
</script>

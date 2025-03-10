<template>
  <!-- 问题内容 -->
  <div class="question-content item-content mb-16 lighter">
    <div class="content mr-12 p-12-16 border-r-8">
      <div class="text break-all pre-wrap">
        <span> {{ chatRecord.problem_text }}</span>
      </div>
    </div>
    <div class="avatar">
      <el-image
        v-if="application.user_avatar"
        :src="application.user_avatar"
        alt=""
        fit="cover"
        style="width: 32px; height: 32px; display: block"
      />
      <ColorAvater v-else>
        <img src="@/assets/user-icon.svg" style="width: 50%" alt="" />
      </ColorAvater>
    </div>
  </div>
</template>
<script setup lang="ts">
import { type chatType } from '@/api/type/application'

defineProps<{
  application: any
  chatRecord: chatType
  type: 'log' | 'ai-chat' | 'debug-ai-chat'
}>()
</script>

<style lang="scss" scoped>
.question-content {
  display: flex;
  justify-content: flex-end;
  padding-left: var(--padding-left);
  width: 100%;
  box-sizing: border-box;

  .content {
    background: #d6e2ff;
    padding-left: 16px;
    padding-right: 16px;
  }

  .download-file {
    height: 43px;

    &:hover {
      color: var(--el-color-primary);
      border: 1px solid var(--el-color-primary);

      .download-button {
        display: block;
        text-align: center;
        line-height: 26px;
      }

      .show {
        display: none;
      }
    }

    .download-button {
      display: none;
    }
  }
  .media-file-width {
    :deep(.el-space__item) {
      min-width: 40% !important;
      flex-grow: 1;
    }
  }
  .media_2 {
    flex: 1;
  }
  .media_0 {
    flex: inherit;
  }
  .media_1 {
    width: 50%;
  }
}
@media only screen and (max-width: 768px) {
  .question-content {
    .media-file-width {
      :deep(.el-space__item) {
        min-width: 100% !important;
      }
    }
    .media_1 {
      width: 100%;
    }
  }
}
.debug-ai-chat {
  .question-content {
    .media-file-width {
      :deep(.el-space__item) {
        min-width: 100% !important;
      }
    }
    .media_1 {
      width: 100%;
    }
  }
}
</style>

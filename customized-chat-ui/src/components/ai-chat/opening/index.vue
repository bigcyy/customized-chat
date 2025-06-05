<template>
  <div v-if="visible" class="opening-container">
    <UserAvater name="icon-robot" class="robot-avatar" />
    <el-card class="opening-card">
      <p class="text-dark">您好，我是 XXX 小助手，您可以向我提出 XXX 使用问题。</p>
      <div v-for="tip in tips" :key="tip" class="tip-item">
        <IconFont name="icon-edit" color="#3370ff" />
        <p>{{ tip }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { defineProps, ref } from 'vue'

defineProps<{
  application: any
  visible: boolean
}>()

const tips = ref(['XXX 主要功能有什么？', 'XXX 怎么使用？', 'XXX 怎么收费？', 'XXX 怎么联系？'])
</script>

<style scoped lang="scss">
.opening-container {
  padding: 0 40px;
  position: relative;

  .robot-avatar {
    position: absolute;
    top: 0;
    left: 0;
  }

  .opening-card {
    font-size: 14px;
    border-radius: 8px;

    :deep(.el-card__body) {
      padding: 16px;
    }

    .tip-item {
      margin-top: 8px;
      padding: 12px;
      border-radius: 8px;
      background-color: #f5f6f7;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: start;
      gap: 6px;

      &:hover {
        background-color: #ecf5ff;
      }
    }
  }
}
</style>

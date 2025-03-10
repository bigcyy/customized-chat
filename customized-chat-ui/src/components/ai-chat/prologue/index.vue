<template>
  <div>
    <div class="avator">
      <ColorAvater :name="applicationName" pinyinColor shape="square" />
    </div>
    <div class="content">
      <el-card shadow="always">
        <MdRenderer :source="prologue" :send-message="sendMessage"></MdRenderer>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import MdRenderer from '@/components/markdown/MdRenderer.vue'
import { computed } from 'vue'

const sendMessage = (message: string) => {
  console.log(message)
}

const props = defineProps<{
  prologue: string
  applicationName: string
}>()

const prologue = computed(() => {
  return props.prologue
})
</script>

<style scoped lang="scss">
.avator {
  width: 32px;
  height: 32px;
  float: left;
}
.content {
  padding-left: var(--padding-left);

  :deep(ol) {
    margin-left: 16px !important;
  }
}
</style>

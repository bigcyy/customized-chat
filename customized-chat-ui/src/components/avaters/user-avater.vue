<template>
  <el-avatar v-if="!name" :size="size" style="background-color: #3370ff; padding: 3px">
    <img src="@/assets/user-icon.svg" alt="avatar" />
  </el-avatar>
  <el-avatar v-else :size="size" style="background-color: transparent">
    <IconFont :name="name" :size="size"></IconFont>
  </el-avatar>
</template>

<script setup lang="ts">

const props = defineProps({
  size: {
    type: Number,
    default: 30
  },
  name: {
    type: String,
    default: ''
  }
})
</script>

<style lang="scss" scoped>

</style>

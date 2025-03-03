<template>
  <div class="app-layout">
    <el-container>
      <el-header>
        <AppHeader />
      </el-header>
      <el-container>
        <el-main><AppMain /></el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import AppHeader from './components/app-header/index.vue'
import AppMain from './components/app-main/index.vue'
</script>

<style lang="scss" scoped></style>

<template>
  <div class="system-layout w-full">
    <el-container>
      <el-aside>
        <AppSideBar />
      </el-aside>
      <el-main>
        <AppMain />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import AppMain from './components/app-main/index.vue'
import AppSideBar from './components/side-bar/index.vue'
</script>

<style lang="scss" scoped></style>

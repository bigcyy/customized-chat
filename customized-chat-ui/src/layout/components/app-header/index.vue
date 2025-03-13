<template>
  <div class="app-header w-full">
    <TopBar />
  </div>
</template>

<script setup lang="ts">
import TopBar from '../top-bar/index.vue'
</script>

<style lang="scss" scoped>
.app-header {
  background: radial-gradient(circle, rgba(235,241,255,1) 0%, rgba(229,250,249,1) 15%, rgba(242,235,254,1) 100%);
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
}
</style>

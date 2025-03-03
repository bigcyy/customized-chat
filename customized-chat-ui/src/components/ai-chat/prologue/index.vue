<template>
  <div>
    <div class="avator">
      <ColorAvater name="advance" pinyinColor shape="square" />
    </div>
    <div class="content">
      <el-card shadow="always"> </el-card>
    </div>
  </div>
</template>

<script setup lang="ts"></script>

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

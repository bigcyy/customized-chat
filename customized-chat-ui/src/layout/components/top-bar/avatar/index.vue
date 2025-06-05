<template>
  <el-dropdown trigger="click" type="primary">
    <div class="avatar-container">
      <UserAvater />
      <div>名字</div>
      <IconFont name="icon-down"></IconFont>
    </div>

    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item><div class="dropdown-item">名字</div></el-dropdown-item>
        <el-dropdown-item><div class="dropdown-item">关于</div></el-dropdown-item>
        <el-dropdown-item><div class="dropdown-item">退出</div></el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts"></script>

<style lang="scss" scoped>
.avatar-container {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
}

.dropdown-item {
  width: 100px;
}
</style>

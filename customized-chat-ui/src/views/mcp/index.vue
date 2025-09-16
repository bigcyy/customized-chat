<template>
  <div>
    <div class="content-header"><h4>MCP</h4></div>
    <div class="content-body">
      <div class="mcp-tool">
        <el-button type="primary">添加 MCP</el-button>
        <el-input placeholder="搜索" v-model="search" clearable style="width: 240px;"></el-input>
      </div>
      <div class="mcp-list">
        <el-row :gutter="20" v-loading>
          <el-col :span="8">
            hi
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>
<script lang = 'ts' setup>
  import {ref} from 'vue'

  let search = ref<string>('');

</script>
<style scoped lang="scss">
  .content-body {
    padding: 20px;
    background-color: var(--app-view-bg-color);
  }
  .mcp-tool {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
</style>
<template>
  <div class="side-bar">
    <el-scrollbar>
      <el-menu :default-active="activeMenu" router class="side-bar-menu">
        <SideBarItem
          v-for="(menu, index) in menuList"
          :key="index"
          :menu="menu"
          :activeMenu="activeMenu"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { getChildRouteListByPathAndName } from '@/router/index'
import SideBarItem from './SideBarItem.vue'

const route = useRoute()

const menuList = computed(() => {
  const { meta } = route
  return getChildRouteListByPathAndName(meta.parentPath, meta.parentName)
})

const activeMenu = computed(() => {
  const { path, meta } = route
  return meta.active || path
})

</script>

<style lang="scss" scoped>
.side-bar {
  height: 100%;
}
.side-bar-menu {
  height: calc(100vh - 130px);
  padding-right: 10px;
  margin-right: 10px;
}
</style>

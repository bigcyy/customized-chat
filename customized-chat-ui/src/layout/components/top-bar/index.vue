<template>
  <div class="top-bar-container flex-between">
    <div class="top-bar-left app-title-container flex-center h-full">
      <div class="app-title flex-center cursor h-full" @click="router.push('/')">
        <img :src="Logo" alt="logo" />
        <span>CChat</span>
      </div>
    </div>
    <div class="top-bar-center flex-center h-full">
      <TopMenu />
    </div>
    <div class="top-bar-right">
      <div class="flex-center h-full gap-2">
        <el-tooltip :content="`项目地址⭐${starCount}`">
          <a target="_blank" :href="githubUrl">
            <IconFont name="icon-github-fill" size="20" />
          </a>
        </el-tooltip>
        <el-tooltip :content="`用户手册`">
          <a target="_blank" :href="githubUrl">
            <IconFont name="icon-yonghushouce" size="20" />
          </a>
        </el-tooltip>
      </div>
      <Avatar />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import TopMenu from './top-menu/index.vue'
import Logo from '@/assets/logo.png'
import Avatar from './avatar/index.vue'

const router = useRouter()
const starCount = ref(0)
const githubUrl = 'https://github.com/bigcyy/customized-chat'

const fetchGitHubStars = async () => {
  try {
    const response = await fetch('https://api.github.com/repos/bigcyy/customized-chat')
    const data = await response.json()
    starCount.value = data.stargazers_count
  } catch (error) {
    console.error('获取 star 数失败:', error)
  }
}

onMounted(() => {
  fetchGitHubStars()
})
</script>

<style lang="scss" scoped>
.top-bar-container {
  height: var(--app-header-height);
  box-sizing: border-box;
  padding: var(--app-header-padding);

  .app-title {
    img {
      height: 100%;
      width: auto;
      object-fit: contain;
    }
  }

  .top-bar-right {
    display: flex;
    align-items: center;
    gap: 20px;
  }
}
</style>

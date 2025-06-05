<template>
  <div class="">
    <div class="flex-between">
      <h4>应用</h4>
      <el-input v-model="search" placeholder="请输入内容" style="width: 240px" />
    </div>
    <div class="content-body">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="application-card application-start" style="--el-card-padding: 8px">
            <div class="cursor flex align-center application-start-item" @click="openCreateApplication">
              <el-icon class="mr-8"><DocumentAdd /></el-icon>
              <span class="text-sm">创建应用</span>
            </div>
            <div class="divider"></div>
            <div class="cursor flex align-center application-start-item">
              <el-icon class="mr-8 "><Upload /></el-icon>
              <span class="text-sm">导入应用</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="application-card">
            <div class="card-content">
              <div class="card-header">
                <div class="app-info">
                  <ColorAvater name="advance" pinyinColor shape="square" />
                  <div class="app-text">
                    <div class="app-name">advance</div>
                    <div class="app-creator">创建者: admin</div>
                  </div>
                </div>
                <el-tag type="warning">高级编排</el-tag>
              </div>
              <div class="card-description">test</div>
              <div class="card-actions">
                <el-button-group>
                  <el-tooltip content="演示" placement="top">
                    <el-button type="primary" text>
                      <el-icon><VideoPlay /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="设置" placement="top">
                    <el-button type="primary" text @click="openApplicationSetting">
                      <el-icon><Setting /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-button type="primary" text>
                    <el-icon><More /></el-icon>
                  </el-button>
                </el-button-group>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <CreateApplicationDialog ref="createApplicationDialog" />
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import CreateApplicationDialog from './component/CreateApplicationDialog.vue'
import ColorAvater from '@/components/avaters/coloer-avater.vue'
import { VideoPlay, Setting, More } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const search = ref('')
const createApplicationDialog = ref<InstanceType<typeof CreateApplicationDialog>>()
const openCreateApplication = () => {
  createApplicationDialog.value?.open()
}

const listApplications = () => {
  console.log('listApplications')
}

const openApplicationSetting = () => {
  router.push('/application/1/simple/setting')
}
</script>

<style lang="scss" scoped>
.application-start {
  background-color: #eff0f1;

  &:hover {
    background-color: #ffffff;
  }

  .application-start-item {
    padding: 8px;
    border-radius: 2px;

    &:hover {
      background-color: #eff0f1;
    }
  }
}

.content-body {
  margin-top: 20px;
}

.application-card {
  height: 160px;
  .card-content {
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
  }

  .app-info {
    display: flex;
    gap: 12px;
    align-items: center;
  }

  .app-text {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .app-name {
    font-size: 16px;
    font-weight: 500;
  }

  .app-creator {
    font-size: 12px;
    color: #999;
  }

  .card-description {
    margin-top: 12px;
    color: #666;
    font-size: 14px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .card-actions {
    display: flex;
    justify-content: flex-start;
    padding-top: 16px;
  }
}
</style>

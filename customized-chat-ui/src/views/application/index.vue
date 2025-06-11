<template>
  <div class="">
    <div class="flex-between">
      <h4>Agent</h4>
      <el-input v-model="search" placeholder="请输入内容" style="width: 240px" />
    </div>
    <div class="content-body" v-loading="loading">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="application-card application-start" style="--el-card-padding: 8px">
            <div class="cursor flex align-center application-start-item" @click="openCreateApplication">
              <el-icon class="mr-8"><DocumentAdd /></el-icon>
              <span class="text-sm">创建Agent</span>
            </div>
            <div class="divider"></div>
            <div class="cursor flex align-center application-start-item">
              <el-icon class="mr-8 "><Upload /></el-icon>
              <span class="text-sm">导入Agent</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" v-for="application in applicationPage?.records" :key="application.id">
          <el-card shadow="hover" class="application-card">
            <div class="card-content">
              <div class="card-header">
                <div class="app-info">
                  <ColorAvater :name="application.name" pinyinColor shape="square" />
                  <div class="app-text">
                    <div class="app-name">{{ application.name }}</div>
                    <div class="app-creator">创建者: admin</div>
                  </div>
                </div>
                <el-tag type="warning">{{ application.applicationType }}</el-tag>
              </div>
              <div class="card-description">{{ application.description }}</div>
              <div class="card-actions">
                <el-button-group>
                  <el-tooltip content="演示" placement="top">
                    <el-button type="primary" text>
                      <el-icon><VideoPlay /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="设置" placement="top">
                    <el-button type="primary" text @click="openApplicationSetting(application.id)">
                      <el-icon><Setting /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="设置" placement="top">
                    <el-button type="primary" text @click="handleDelete(application.id!)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </el-tooltip>
                </el-button-group>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <div class="pagination-section">
        <el-pagination
          layout="prev, pager, next" 
          :total="applicationPage?.total" 
          :page-size="size" 
          v-model:current-page="page"
          @current-change="loadApplicationPage"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import ColorAvater from '@/components/avaters/coloer-avater.vue'
import { VideoPlay, Setting, Delete } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import applicationApi from '@/api/application'
import type { ApplicationForm } from '@/api/type/application'
import type { Page } from '@/request/Result'
import { MsgError, MsgConfirm, MsgSuccess } from '@/utils/message'

const router = useRouter()
const search = ref('')
const applicationPage = ref<Page<ApplicationForm>>()
const loading = ref(false)
const page = ref(1)
const size = ref(15)

const openCreateApplication = () => {
  router.push('/application/new/setting')
}

const openApplicationSetting = (id?: number) => {
  if (id) {
    router.push(`/application/${id}/setting`)
  }
}

const loadApplicationPage = () => {
  loading.value = true
  applicationApi.listApplications(page.value, size.value).then(resp => {
    applicationPage.value = resp.data.agents
  }).catch(err => {
    MsgError(err.message)
  }).finally(() => {
    loading.value = false
  })
}

const handleDelete = async (id: number) => {
  MsgConfirm('确定删除该Agent吗？', '').then(async () => {
    await applicationApi.deleteApplication(id)
    MsgSuccess('删除成功')
    loadApplicationPage()
  }).catch(err => {})
}

onMounted(() => {
  loadApplicationPage()
})
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
  height: 80vh;
  display: flex;
  flex-direction: column;
  justify-content: space-between;

  .pagination-section {
    margin-top: 10px;
    display: flex;
    justify-content: center;
  }
}

.application-card {
  margin-top: 10px;
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

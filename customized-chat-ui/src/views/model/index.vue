<template>
  <div class="content-container">
    <div class="content-header">
      <h4>模型</h4>
    </div>
    <div class="content-body">
      <div class="content-body-header flex flex-between">
        <el-button type="primary" @click="handleAddModel">新增模型</el-button>
        <el-input v-model="search" placeholder="请输入内容" style="width: 240px" />
      </div>
      <el-row :gutter="20" class="model-cards" v-loading="loading">
        <el-col :span="8" v-for="(item, index) in filterCardData" :key="index">
          <el-card class="model-card">
            <div class="card-header">
              <span
                :innerHTML="item.provider.providerIconPath"
                alt=""
                style="height: 32px; width: 32px"
              />

              <div class="model-info">
                <div class="model-name">{{ item.displayName }}</div>
                <el-tag size="small" type="info">
                  {{ item.provider.providerName }}
                </el-tag>
              </div>
            </div>
            <div class="card-content">
              <div class="info-item">
                <span class="label">模型类型:</span>
                <span class="value">{{ item.modelType.name }}</span>
              </div>
              <div class="info-item">
                <span class="label">基础模型:</span>
                <span class="value">{{ item.modelName }}</span>
              </div>
            </div>
            <div class="card-actions">
              <el-button size="small" @click="handleEdit(index, item)">编辑</el-button>

              <el-button size="small" type="danger" @click="deleteModel(item)">删除</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    <CreateModelDialog
      ref="createModelRef"
      @change="openSelectProviderDialog()"
      @submit="listModels()"
    />
    <SelectProviderDialog ref="selectProviderRef" @change="openCreateModelDialog($event)" />
    <UpdateModelDialog ref="updateModelRef" @update="listModels()" />
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, onMounted } from 'vue'
import SelectProviderDialog from './component/SelectProviderDialog.vue'
import CreateModelDialog from './component/CreateModelDialog.vue'
import UpdateModelDialog from './component/UpdateModelDialog.vue'
import { type Provider, type ModelVO } from '@/api/type/model'
import modelApi from '@/api/model'
import { MsgConfirm } from '@/utils/message'

const selectProviderRef = ref<InstanceType<typeof SelectProviderDialog>>()
const createModelRef = ref<InstanceType<typeof CreateModelDialog>>()
const cardData = ref<Array<ModelVO>>([])
const loading = ref(false)
const search = ref('')
const updateModelRef = ref<InstanceType<typeof UpdateModelDialog>>()
const filterCardData = computed(() =>
  cardData.value.filter(
    (data) => !search.value || data.displayName.toLowerCase().includes(search.value.toLowerCase())
  )
)
const handleEdit = (index: number, row: ModelVO) => {
  updateModelRef.value?.open(row.id)
}
const deleteModel = (item: ModelVO) => {
  MsgConfirm('删除模型', `确定删除模型：${item.displayName} ？`, {
    confirmButtonText: '确定',
    confirmButtonClass: 'danger'
  })
    .then(() => {
      modelApi.deleteModel(item.id).then((res) => {
        listModels()
      })
    })
    .catch(() => {})
}
const handleAddModel = () => {
  selectProviderRef.value?.open()
}
const openCreateModelDialog = (provider: Provider) => {
  createModelRef.value?.open(provider)
}
const openSelectProviderDialog = () => {
  selectProviderRef.value?.open()
}

const listModels = () => {
  modelApi.getSavedModels(loading).then((res) => {
    cardData.value = res.data.models
  })
}

onMounted(() => {
  listModels()
})
</script>

<style scoped lang="scss">
.content-body {
  background-color: var(--app-view-bg-color);
  box-sizing: border-box;
  min-width: 847px;
  padding: 20px;
}

.model-cards {
  margin-top: 20px;
}

.model-card {
  margin-bottom: 20px;

  .card-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;

    .model-info {
      margin-left: 12px;

      .model-name {
        font-size: 16px;
        font-weight: bold;
        margin-bottom: 4px;
      }
    }
  }

  .card-content {
    .info-item {
      margin-bottom: 8px;

      .label {
        color: #909399;
        margin-right: 8px;
      }

      .value {
        color: #303133;
      }
    }
  }

  .card-actions {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}
</style>

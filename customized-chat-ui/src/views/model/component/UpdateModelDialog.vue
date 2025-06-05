<template>
  <el-dialog
    v-model="dialogVisible"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :destroy-on-close="true"
    @closed="close"
    append-to-body
  >
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基础信息" name="modelInfo">
        <el-form label-width="120px" :model="baseFormData" label-position="top">
          <el-form-item>
            <template #label>
              <div class="flex align-center" style="display: inline-flex">
                <div class="mr-4">
                  <span> 模型名称 </span>
                </div>
                <el-tooltip effect="dark" placement="right">
                  <template #content>
                    <span>自定义模型的显示名称</span>
                  </template>
                  <el-icon><QuestionFilled /></el-icon>
                </el-tooltip>
              </div>
            </template>
            <el-input v-model="baseFormData.displayName" />
          </el-form-item>
          <el-form-item>
            <template #label>
              <div class="flex align-center" style="display: inline-flex">
                <div class="mr-4">
                  <span> 模型类型 </span>
                </div>
                <el-tooltip effect="dark" placement="right">
                  <template #content>
                    <p>
                      大语言模型：在Agent中与AI对话的推理模型。<br />向量模型：在知识库中对文档内容进行向量化的模型。
                    </p>
                  </template>
                  <el-icon><QuestionFilled /></el-icon>
                </el-tooltip>
              </div>
            </template>
            <el-select
              v-model="baseFormData.modelType"
              placeholder="请选择模型类型"
              v-loading="model_type_loading"
              disabled
            >
              <el-option
                v-for="(modelType, index) in modelTypes"
                :key="index"
                :label="modelType.name"
                :value="modelType.key"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="基础模型">
            <el-select
              v-model="baseFormData.modelName"
              placeholder="请选择模型"
              v-loading="modelListLoading"
            >
              <el-option
                v-for="(model, index) in modelList"
                :key="index"
                :label="model"
                :value="model"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="API URL">
            <el-input v-model="baseFormData.apiUrl" />
          </el-form-item>
          <el-form-item label="API Key">
            <el-input v-model="baseFormData.apiKey" />
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="高级设置" name="advancedSettings">
        <el-empty
          v-if="!baseFormData.modelType || !baseFormData.modelName"
          description="请先选择模型类型和模型"
        />
        <el-empty
          v-else-if="baseFormData.modelType !== 'LLM'"
          description="所选模型不支持高级设置"
        />
        <el-form :model="baseFormData.modelConfig" label-width="120px" v-else>
          <el-form-item label="Temperature">
            <el-slider
              v-model.number="baseFormData.modelConfig.temperature"
              show-input
              :min="0.01"
              :max="1.0"
              :step="0.01"
            />
          </el-form-item>
          <el-form-item label="Max Tokens">
            <el-slider
              v-model.number="baseFormData.modelConfig.maxTokens"
              show-input
              :min="1"
              :max="163840"
              :step="1"
            />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <span>
        <el-button @click="close">取消</el-button>
        <el-button type="primary" @click="update" :loading="loading"> 更新 </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { type Provider, type ModelType, type ModelConfigForm } from '@/api/type/model'
import modelApi from '@/api/model'
import { MsgSuccess } from '@/utils/message'

const dialogVisible = ref(false)
const loading = ref(false)
const modelTypes = ref<Array<ModelType>>([])
const modelList = ref<Array<string>>([])
const model_type_loading = ref(false)
const modelListLoading = ref(false)
const activeTab = ref('modelInfo')
const baseFormData = ref<ModelConfigForm>({
  displayName: '',
  modelType: '',
  modelName: '',
  provider: '',
  modelConfig: {
    temperature: 0.7,
    maxTokens: 10240
  },
  apiUrl: '',
  apiKey: ''
})

const emit = defineEmits(['update'])

const open = async (id: number) => {
  // 获取模型
  await modelApi.getModelById(id).then((res) => {
    baseFormData.value = res.data.model
    if (baseFormData.value.modelConfig) {
      baseFormData.value.modelConfig.maxTokens = Number(baseFormData.value.modelConfig.maxTokens)
      baseFormData.value.modelConfig.temperature = Number(
        baseFormData.value.modelConfig.temperature
      )
    }
  })
  // 获取根据模型的提供商获取模型的所有类型
  if (baseFormData.value?.provider) {
    modelApi.getModelTypes(baseFormData.value?.provider, model_type_loading).then((res) => {
      modelTypes.value = res.data.types
    })

    // 根据模型的类型以及提供商获取所有可以选择的模型
    listModel(baseFormData.value.modelType)
  }
  dialogVisible.value = true
}

const close = () => {
  modelListLoading.value = false
  model_type_loading.value = false
  modelList.value = []
  loading.value = false
  activeTab.value = 'modelInfo'
  baseFormData.value = {
    displayName: '',
    modelType: '',
    modelName: '',
    provider: '',
    modelConfig: {
      temperature: 0.7,
      maxTokens: 10240
    },
    apiUrl: '',
    apiKey: ''
  }
  dialogVisible.value = false
}

const listModel = (modelType: string) => {
  modelApi.getModelByType(baseFormData.value.provider, modelType, modelListLoading).then((res) => {
    modelList.value = res.data.models
  })
}

const update = () => {
  modelApi.updateModel(baseFormData.value, loading).then((res) => {
    close()
    MsgSuccess('更新成功')
    emit('update')
  })
}

defineExpose({
  open,
  close
})
</script>

<style scoped lang="scss">
.select-provider {
  font-size: 16px;
  color: rgba(100, 106, 115, 1);
  font-weight: 400;
  line-height: 24px;
  cursor: pointer;

  &:hover {
    color: var(--el-color-primary);
  }
}

.active-breadcrumb {
  font-size: 16px;
  color: rgba(31, 35, 41, 1);
  font-weight: 500;
  line-height: 24px;
}
</style>

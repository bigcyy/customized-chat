<template>
  <el-dialog
    title="创建Agent"
    v-model="visible"
    width="600px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :destroy-on-close="true"
    @closed="close"
    append-to-body
  >
    <el-form :model="applicationForm" label-position="top">
      <el-form-item label="Agent名称" prop="name">
        <el-input v-model="applicationForm.name" placeholder="请输入Agent名称" />
      </el-form-item>
      <el-form-item label="Agent描述" prop="description">
        <el-input v-model="applicationForm.description" placeholder="请输入Agent描述" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="createApplication" :loading="isCreating" :disabled="isCreating">
        {{ isCreating ? '创建中...' : '创建Agent' }}
      </el-button>
      <el-button @click="close" :disabled="isCreating">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { type ApplicationForm } from '@/api/type/application'
import applicationApi from '@/api/application'
import { MsgSuccess } from '@/utils/message'
import { useRouter } from 'vue-router'

const visible = ref(false)
const router = useRouter()
const isCreating = ref(false)

const applicationForm = ref<ApplicationForm>({
  name: '',
  description: ''
})

const open = () => {
  visible.value = true
}

const createApplication = () => {
  if (isCreating.value) return
  
  isCreating.value = true
  applicationApi.createApplication(applicationForm.value)
    .then((res) => {
      close()
      MsgSuccess('创建成功')
      router.push({
        path: `/application/${res.data.id}/setting`
      })
    })
    .catch((error) => {
      console.error('创建失败:', error)
    })
    .finally(() => {
      isCreating.value = false
    })
}

const close = () => {
  applicationForm.value = {
    name: '',
    description: '',
  }
  visible.value = false
  isCreating.value = false
}

defineExpose({
  open,
  close
})
</script>

<style lang="scss" scoped></style>

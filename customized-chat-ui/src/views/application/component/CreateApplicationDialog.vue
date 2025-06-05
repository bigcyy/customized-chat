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
      <el-form-item label="Agent类型" prop="applicationType">
        <el-radio-group v-model="applicationForm.applicationType">
          <el-radio-button value="SIMPLE">简单Agent</el-radio-button>
          <el-radio-button value="COMPLEX">复杂Agent</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="createApplication">创建Agent</el-button>
      <el-button @click="close">取消</el-button>
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

const applicationForm = ref<ApplicationForm>({
  name: '',
  description: '',
  applicationType: 'SIMPLE'
})

const open = () => {
  visible.value = true
}

const createApplication = () => {
  applicationApi.createApplication(applicationForm.value).then((res) => {
    close()
    MsgSuccess('创建成功')
    router.push({
      path: `/application/${res.data.id}/${applicationForm.value.applicationType}/setting`
    })
  })
}

const close = () => {
  applicationForm.value = {
    name: '',
    description: '',
    applicationType: 'SIMPLE'
  }
  visible.value = false
}

defineExpose({
  open,
  close
})
</script>

<style lang="scss" scoped></style>

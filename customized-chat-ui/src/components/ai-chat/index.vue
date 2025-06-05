<template>
  <div class="custom-scrollbar">
    <Opening :application="application" :visible="visible" />
    <Opening :application="application" :visible="visible" />
    <Opening :application="application" :visible="visible" />
    <Opening :application="application" :visible="visible" />
    <Opening :application="application" :visible="visible" />
    <Opening :application="application" :visible="visible" />
    <Opening :application="application" :visible="visible" />
    <Opening :application="application" :visible="visible" />
    <Opening :application="application" :visible="visible" />
    <Opening :application="application" :visible="visible" />
    <Opening :application="application" :visible="visible" />
    <Opening :application="application" :visible="visible" />
    <Opening :application="application" :visible="visible" />
  </div>
</template>

<script setup lang="ts">
import { defineProps, ref } from 'vue'
import Opening from './opening/index.vue'

defineProps<{
  application: any
}>()

const visible = ref(true)
</script>

<style scoped lang="scss">
</style>

<template>
  <svg class="icon-svg icon" aria-hidden="true" :style="styleObject" @click="handleClick">
    <use :xlink:href="`#${name}`"></use>
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue'

// 定义组件属性
const props = defineProps({
  // 图标名称
  name: {
    type: String,
    required: true
  },
  // 图标大小
  size: {
    type: [Number, String],
    default: 16
  },
  // 图标颜色
  color: {
    type: String,
    default: ''
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否旋转
  spin: {
    type: Boolean,
    default: false
  }
})

// 定义事件
const emit = defineEmits(['click'])

// 计算样式对象
const styleObject = computed(() => {
  const size = typeof props.size === 'number' ? `${props.size}px` : props.size

  return {
    width: size,
    height: size,
    fill: props.color,
    cursor: props.disabled ? 'not-allowed' : 'pointer',
    opacity: props.disabled ? 0.5 : 1,
    animation: props.spin ? 'icon-spin 1s infinite linear' : 'none'
  }
})

// 点击事件处理
const handleClick = (event: MouseEvent) => {
  if (!props.disabled) {
    emit('click', event)
  }
}
</script>

<style scoped>
.icon-svg {
  vertical-align: -0.15em;
  overflow: hidden;
}

.icon {
  width: 1em;
  height: 1em;
  vertical-align: -0.15em;
  fill: currentColor;
  overflow: hidden;
}

@keyframes icon-spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>

<template>
  <MdPreview 
    noIconfont 
    noPrettier 
    :codeFoldable="false" 
    :modelValue="source"
    editorId="preview-only"
    class="custom-markdown"
  />
</template>

<script setup lang="ts">
import { config } from 'md-editor-v3'
import { MdPreview } from 'md-editor-v3'

// 配置markdown渲染规则
config({
  markdownItConfig(md) {
    // 图片渲染规则
    md.renderer.rules.image = (tokens, idx, options, env, self) => {
      tokens[idx].attrSet('style', 'display:inline-block;min-height:33px;padding:0;margin:0')
      if (tokens[idx].content) {
        tokens[idx].attrSet('title', tokens[idx].content)
      }
      tokens[idx].attrSet(
        'onerror',
        'this.src="/ui/assets/load_error.png";this.onerror=null;this.height="33px"'
      )
      return md.renderer.renderToken(tokens, idx, options)
    }
    // 链接渲染规则 - 新窗口打开
    md.renderer.rules.link_open = (tokens, idx, options, env, self) => {
      tokens[idx].attrSet('target', '_blank')
      return md.renderer.renderToken(tokens, idx, options)
    }
  }
})

withDefaults(
  defineProps<{
    source?: string
  }>(),
  {
    source: ''
  }
)
</script>

<style lang="scss">
// 全局markdown样式，不使用scoped
.custom-markdown {
  .md-editor-preview {
    padding: 0 !important;
    background: transparent !important;
    
    // 段落
    p {
      margin: 0 0 8px 0 !important;
      line-height: 1.6 !important;
      
      &:last-child {
        margin-bottom: 0 !important;
      }
    }
    
    // 列表
    ul, ol {
      margin: 8px 0 !important;
      padding-left: 20px !important;
      
      li {
        margin: 4px 0 !important;
        line-height: 1.5 !important;
      }
    }
    
    // 标题
    h1, h2, h3, h4, h5, h6 {
      margin: 12px 0 8px 0 !important;
      
      &:first-child {
        margin-top: 0 !important;
      }
    }
    
    // 代码块
    pre {
      margin: 8px 0 !important;
      border-radius: 4px !important;
      
      code {
        font-size: 14px !important;
      }
    }
    
    // 行内代码
    p code, li code {
      padding: 2px 4px !important;
      border-radius: 3px !important;
      font-size: 0.9em !important;
    }
    
    // 引用
    blockquote {
      margin: 8px 0 !important;
      padding: 8px 12px !important;
      border-radius: 4px !important;
      
      p {
        margin: 0 !important;
      }
    }
    
    // 表格
    table {
      margin: 8px 0 !important;
      
      th, td {
        padding: 8px 12px !important;
      }
    }
    
    // 强调
    strong {
      font-weight: 600 !important;
    }
    
    // 链接
    a {
      text-decoration: none !important;
      
      &:hover {
        text-decoration: underline !important;
      }
    }
    
    // 分隔线
    hr {
      margin: 16px 0 !important;
    }
    
    // 清除默认的外边距
    > *:first-child {
      margin-top: 0 !important;
    }
    
    > *:last-child {
      margin-bottom: 0 !important;
    }
  }
}
</style>

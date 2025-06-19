<template>
  <div class="md-renderer">
    <!-- 渲染工具调用 -->
    <div v-if="toolExecutions && toolExecutions.length > 0" class="tool-executions">
      <div 
        v-for="(toolExecution, index) in toolExecutions" 
        :key="toolExecution.request.id"
        class="tool-execution-card"
      >
        <div 
          class="tool-header"
          @click="toggleTool(index)"
        >
          <div class="tool-info">
            <el-icon class="tool-icon">
              <Tools />
            </el-icon>
            <span class="tool-name">{{ toolExecution.request.name }}</span>
            <el-tag size="small" type="info">工具调用</el-tag>
          </div>
          <el-icon 
            class="expand-icon"
            :class="{ 'expanded': expandedTools[index] }"
          >
            <ArrowDown />
          </el-icon>
        </div>
        
        <el-collapse-transition>
          <div v-show="expandedTools[index]" class="tool-content">
            <!-- 工具调用参数 -->
            <div class="tool-section" v-if="toolExecution.request.arguments">
              <div class="section-title">
                <el-icon><Setting /></el-icon>
                <span>调用参数</span>
              </div>
              <div class="section-content">
                <pre class="tool-code">{{ formatArguments(toolExecution.request.arguments) }}</pre>
              </div>
            </div>
            
            <!-- 工具调用结果 -->
            <div class="tool-section" v-if="toolExecution.result">
              <div class="section-title">
                <el-icon><CircleCheck /></el-icon>
                <span>执行结果</span>
              </div>
              <div class="section-content">
                <div class="tool-result">
                  <!-- 如果结果是JSON，尝试格式化渲染 -->
                  <pre v-if="isJsonResult(toolExecution.result)" class="tool-code">{{ formatJson(toolExecution.result) }}</pre>
                  <!-- 否则使用markdown渲染 -->
                  <MdPreview 
                    v-else
                    noIconfont 
                    noPrettier 
                    :codeFoldable="false" 
                    :modelValue="toolExecution.result"
                    editorId="tool-result-preview"
                    class="custom-markdown tool-result-markdown"
                  />
                </div>
              </div>
            </div>
          </div>
        </el-collapse-transition>
      </div>
    </div>
    
    <!-- 渲染markdown内容 -->
    <MdPreview 
      v-if="source"
      noIconfont 
      noPrettier 
      :codeFoldable="false" 
      :modelValue="source"
      editorId="preview-only"
      class="custom-markdown"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { config } from 'md-editor-v3'
import { MdPreview } from 'md-editor-v3'
import { Tools, ArrowDown, Setting, CircleCheck } from '@element-plus/icons-vue'
import type { ToolExecution } from '@/request/Result'

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

interface Props {
  source?: string
  toolExecutions?: ToolExecution[]
}

const props = withDefaults(defineProps<Props>(), {
  source: '',
  toolExecutions: () => []
})

// 控制工具展开状态
const expandedTools = reactive<Record<number, boolean>>({})

// 切换工具展开状态
const toggleTool = (index: number) => {
  expandedTools[index] = !expandedTools[index]
}

// 格式化工具调用参数
const formatArguments = (args: string) => {
  try {
    const parsed = JSON.parse(args)
    return JSON.stringify(parsed, null, 2)
  } catch (e) {
    return args
  }
}

// 判断是否为JSON结果
const isJsonResult = (result: string) => {
  try {
    JSON.parse(result)
    return true
  } catch (e) {
    return false
  }
}

// 格式化JSON结果
const formatJson = (result: string) => {
  try {
    const parsed = JSON.parse(result)
    return JSON.stringify(parsed, null, 2)
  } catch (e) {
    return result
  }
}

// 初始化第一个工具为展开状态
if (props.toolExecutions && props.toolExecutions.length > 0) {
  expandedTools[0] = true
}
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
  
  // 工具结果中的markdown样式调整
  &.tool-result-markdown {
    .md-editor-preview {
      font-size: 13px !important;
      
      p {
        margin: 4px 0 !important;
      }
      
      pre {
        margin: 4px 0 !important;
        font-size: 11px !important;
      }
    }
  }
}
</style>

<style scoped lang="scss">
.md-renderer {
  .tool-executions {
    margin-bottom: 12px;
    
    .tool-execution-card {
      border: 1px solid #e4e7ed;
      border-radius: 8px;
      margin-bottom: 8px;
      overflow: hidden;
      background: #fafbfc;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .tool-header {
        padding: 10px 12px;
        background: #f8f9fa;
        border-bottom: 1px solid #e4e7ed;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: space-between;
        transition: background-color 0.2s;
        
        &:hover {
          background: #f0f2f5;
        }
        
        .tool-info {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .tool-icon {
            color: #409eff;
            font-size: 16px;
          }
          
          .tool-name {
            font-weight: 500;
            color: #303133;
            font-size: 14px;
          }
        }
        
        .expand-icon {
          color: #909399;
          transition: transform 0.3s;
          
          &.expanded {
            transform: rotate(180deg);
          }
        }
      }
      
      .tool-content {
        .tool-section {
          padding: 12px;
          
          &:not(:last-child) {
            border-bottom: 1px solid #f0f0f0;
          }
          
          .section-title {
            display: flex;
            align-items: center;
            gap: 6px;
            margin-bottom: 8px;
            font-size: 13px;
            font-weight: 500;
            color: #606266;
            
            .el-icon {
              font-size: 14px;
            }
          }
          
          .section-content {
            .tool-code {
              background: #f8f9fa;
              border: 1px solid #e9ecef;
              border-radius: 4px;
              padding: 8px 10px;
              font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
              font-size: 12px;
              line-height: 1.4;
              color: #495057;
              margin: 0;
              white-space: pre-wrap;
              word-break: break-all;
              max-height: 200px;
              overflow-y: auto;
            }
            
            .tool-result {
              background: white;
              border: 1px solid #e9ecef;
              border-radius: 4px;
              padding: 8px 10px;
              max-height: 300px;
              overflow-y: auto;
            }
          }
        }
      }
    }
  }
}

// 自定义滚动条
.tool-code::-webkit-scrollbar,
.tool-result::-webkit-scrollbar {
  width: 4px;
  height: 4px;
}

.tool-code::-webkit-scrollbar-track,
.tool-result::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 2px;
}

.tool-code::-webkit-scrollbar-thumb,
.tool-result::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.tool-code::-webkit-scrollbar-thumb:hover,
.tool-result::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>

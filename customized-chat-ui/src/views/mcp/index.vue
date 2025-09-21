<template>
  <div>
    <div class="content-header"><h4>MCP</h4></div>
    <div class="content-body">
      <div class="mcp-tool">
        <el-button type="primary" @click="dialogVisible = true">添加 MCP</el-button>
        <el-input placeholder="搜索" v-model="search" clearable style="width: 240px;"></el-input>
      </div>
      <div class="mcp-list">
        <el-row :gutter="20" v-loading="loading">
          <!-- 有数据时显示卡片列表 -->
          <template v-if="mcpServerPageData?.records && mcpServerPageData.records.length > 0">
            <el-col :span="8" v-for="server in mcpServerPageData.records" :key="server.id">
              <el-card class="mcp-server-card" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span class="server-name">{{ server.serverName }}</span>
                    <el-tag :type="server.type === 'stdio' ? 'primary' : 'success'" size="small">
                      {{ server.type.toUpperCase() }}
                    </el-tag>
                  </div>
                </template>
                
                <div class="card-content">
                  <p class="server-description" v-if="server.serverDescription">
                    {{ server.serverDescription }}
                  </p>
                  <p class="no-description" v-else>暂无描述</p>
                  
                  <!-- STDIO 类型的配置信息 -->
                  <div class="config-info" v-if="server.type === 'stdio'">
                    <div class="config-item" v-if="server.command">
                      <span class="config-label">命令:</span>
                      <span class="config-value">{{ server.command }}</span>
                    </div>
                    <div class="config-item" v-if="server.args && server.args.length > 0">
                      <span class="config-label">参数:</span>
                      <div class="args-list">
                        <el-tag v-for="(argValue, index) in server.args" :key="index" size="small" class="arg-tag">
                          {{ argValue }}
                        </el-tag>
                      </div>
                    </div>
                  </div>
                  
                  <!-- SSE 类型的配置信息 -->
                  <div class="config-info" v-if="server.type === 'sse'">
                    <div class="config-item" v-if="server.sseUrl">
                      <span class="config-label">URL:</span>
                      <span class="config-value">{{ server.sseUrl }}</span>
                    </div>
                    <div class="config-item" v-if="server.header && Object.keys(server.header).length > 0">
                      <span class="config-label">请求头:</span>
                      <div class="headers-list">
                        <div v-for="(value, key) in server.header" :key="key" class="header-item">
                          <span class="header-key">{{ key }}:</span>
                          <span class="header-value">{{ value }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                
                <template #footer>
                  <div class="card-actions">
                    <el-button size="small" type="primary" text @click="openEditDialog(server)">编辑</el-button>
                    <el-button size="small" type="danger" text @click="doDeleteMcpServer(server.id!)">删除</el-button>
                  </div>
                </template>
              </el-card>
            </el-col>
          </template>
          
          <!-- 空状态显示 -->
          <el-col :span="24" v-else-if="!loading">
            <el-empty description="暂无MCP服务器" class="empty-state">
              <el-button type="primary" @click="dialogVisible = true">添加第一个MCP服务器</el-button>
            </el-empty>
          </el-col>
        </el-row>  
        <!-- 分页组件 -->
        <div class="pagination-wrapper" v-if="mcpServerPageData?.total && mcpServerPageData.total > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[6, 12, 18, 24]"
            :total="mcpServerPageData.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
    <el-dialog
      v-model="dialogVisible"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="true"
      :destroy-on-close="true"
      @closed="close"
      append-to-body
    >
      <template #header>
        <div class="dialog-header">
          <h3>{{ isEditMode ? '编辑服务器' : '添加服务器' }}</h3>
          <p class="dialog-subtitle">{{ isEditMode ? '修改 MCP 服务器配置' : '创建新的 MCP 服务器配置' }}</p>
        </div>
      </template>
      <el-form label-width="auto" :model="formData" label-position="top">
        <el-form-item label="名称">
          <el-input v-model="formData.serverName" placeholder="输入服务器名称" />
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input 
            v-model="formData.serverDescription" 
            type="textarea" 
            placeholder="输入服务器描述"
            :rows="3"
          />
        </el-form-item>
        
        <el-form-item label="类型">
          <el-select v-model="formData.type" placeholder="stdio">
            <el-option label="STDIO" value="stdio" />
            <el-option label="SSE" value="sse" />
          </el-select>
        </el-form-item>

        <el-form-item label="命令" v-if="formData.type === 'stdio'">
          <el-input v-model="formData.command" placeholder="UVX" />
        </el-form-item>
        
        <el-form-item label="参数" v-if="formData.type === 'stdio'">
          <div class="args-config">
            <div v-for="(arg, index) in formData.args" :key="index" class="arg-item">
              <el-input placeholder="请输入参数"
                        v-model="formData.args![index]"
                        class="arg-input"/>
              <el-button type="danger"
                       size="small"
                       text
                       @click="removeCurrentArg(index)"
                       :icon="Delete"/>
            </div>
            <el-button type="primary" 
                       size="small" 
                       text 
                       @click="addCurrentArg" 
                       :icon="Plus">
              添加参数
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="SSE URL" v-if="formData.type === 'sse'">
            <el-input 
              v-model="formData.sseUrl" 
              placeholder="请输入SSE服务器URL"
            />
        </el-form-item>

        <el-form-item label="请求头" v-if="formData.type === 'sse'">
          <div class="header-config">
            <div class="header-items">
              <div 
                v-for="(kv, index) in headerKVList" 
                :key="index"
                class="header-item"
              >
                <el-input 
                  v-model="kv.key"
                  placeholder="Header名称"
                  class="header-key"
                  @blur="updateHeaderKey(index)"/>
                <el-input 
                  v-model="kv.value" 
                  placeholder="Header值"
                  class="header-value"/>
                <el-button 
                  type="danger" 
                  size="small" 
                  text
                  @click="removeHeader(index)"
                  :icon="Delete"/>
              </div>
            </div>
            <el-button 
              type="primary" 
              size="small" 
              text
              @click="addHeader"
              :icon="Plus"
            >
              添加Header
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span>
          <el-button @click="close">取消</el-button>
          <el-button type="primary" @click="isEditMode ? doUpdateMcpServer() : doCreateMcpServer()" :loading="loading">
            {{ isEditMode ? '更新' : '创建' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script lang='ts' setup>
  import { ref, onMounted } from 'vue'
  import type { MCPServer } from '@/api/type/mcpServer'
  import { Plus, Delete } from '@element-plus/icons-vue'
  import mcpServerApi from '@/api/mcpServer'
  import type { Page } from '@/request/Result'
  import { MsgSuccess } from '@/utils/message'
  const search = ref<string>('')
  const dialogVisible = ref<boolean>(false)
  const loading = ref<boolean>(false)
  const mcpServerPageData = ref<Page<MCPServer>>()
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(12)
  const isEditMode = ref<boolean>(false)

  const formData = ref<MCPServer>({
    serverName: '',
    serverDescription: '',
    type: 'stdio',
    command: '',
    args: [],
    sseUrl: '',
    header: {},
    isEnabled: true
  })

  const headerKVList = ref<{
    key: string,
    value: string
  }[]>([])

  const close = () => {
    dialogVisible.value = false
    isEditMode.value = false
    formData.value = {
      serverName: '',
      serverDescription: '',
      type: 'stdio',
      command: '',
      args: [],
      sseUrl: '',
      header: {},
      isEnabled: true
    }
    headerKVList.value = []
  }

  const doCreateMcpServer = () => {
    listToRecord() // 将header键值对列表转换为对象
    mcpServerApi.createMcpServer(formData.value, loading).then(() => {
      MsgSuccess('创建成功')
      loadMcpServerPage(currentPage.value, pageSize.value)
      close()
    }).catch((err) => {
      console.error('创建MCP服务器失败:', err)
    })
  }

  const doUpdateMcpServer = () => {
    listToRecord() // 将header键值对列表转换为对象
    mcpServerApi.updateMcpServer(formData.value, loading).then(() => {
      MsgSuccess('更新成功')
      loadMcpServerPage(currentPage.value, pageSize.value)
      close()
    }).catch((err) => {
      console.error('更新MCP服务器失败:', err)
    })
  }

  const openEditDialog = (server: MCPServer) => {
    isEditMode.value = true
    formData.value = { ...server }
    recordToList() // 将header对象转换为键值对列表
    dialogVisible.value = true
  }

  const doDeleteMcpServer = (id: number) => {
    mcpServerApi.deleteMcpServer(id).then(() => {
      MsgSuccess('删除成功')
      loadMcpServerPage(currentPage.value, pageSize.value)
    }).catch((err) => {
      console.error('删除MCP服务器失败:', err)
    })
  }

  const addCurrentArg = () => {
    if (!formData.value.args) {
      formData.value.args = [];
    }
    formData.value.args.push('');
  }

  const removeCurrentArg = (index: number) => {
    if (formData.value.args) {
      formData.value.args.splice(index, 1);
    }
  }

  const addHeader = () => {
    headerKVList.value.push({
      key: "",
      value: ""
    })
  }

  const updateHeaderKey = (index: number) => {
    headerKVList.value[index].key;
  }

  const removeHeader = (index: number) => {
    headerKVList.value.splice(index, 1);
  }

  const listToRecord = () => {
    formData.value.header = {};
    for (let kv of headerKVList.value) {
      formData.value.header[kv.key] = kv.value;
    }
  }

  const recordToList = () => {
    headerKVList.value = [];
    if (!formData.value.header) {
      formData.value.header = {}
    }
    headerKVList.value = Object.entries(formData.value.header).map(([key, value]) => {
      return {key: key, value: value};
    });
  }

  const loadMcpServerPage = async (page: number, size: number) => {
    loading.value = true
    try {
      const res = await mcpServerApi.getMcpServerList(page, size)
      mcpServerPageData.value = res.data['servers']
    } catch (err) {
      console.error('加载MCP服务器列表失败:', err)
    } finally {
      loading.value = false
    }
  }

  const handleSizeChange = (val: number) => {
    pageSize.value = val
    currentPage.value = 1
    loadMcpServerPage(currentPage.value, pageSize.value)
  }

  const handleCurrentChange = (val: number) => {
    currentPage.value = val
    loadMcpServerPage(currentPage.value, pageSize.value)
  }

  onMounted(() => {
    loadMcpServerPage(currentPage.value, pageSize.value)
  })
</script>
<style scoped lang="scss">
  .content-body {
    padding: 20px;
    background-color: var(--app-view-bg-color);
  }
  .mcp-tool {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
  }

  .mcp-list {
    margin-bottom: 20px;
  }

  .mcp-server-card {
    height: 100%;
    margin-bottom: 20px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .server-name {
        font-weight: 600;
        font-size: 16px;
        color: #303133;
      }
    }

    .card-content {
      padding: 0;

      .server-description {
        margin: 0 0 16px 0;
        color: #606266;
        font-size: 14px;
        line-height: 1.5;
        word-break: break-word;
      }

      .no-description {
        margin: 0 0 16px 0;
        color: #C0C4CC;
        font-size: 14px;
        font-style: italic;
      }

      .config-info {
        .config-item {
          margin-bottom: 12px;
          
          &:last-child {
            margin-bottom: 0;
          }

          .config-label {
            display: inline-block;
            font-weight: 500;
            color: #909399;
            font-size: 13px;
            margin-right: 8px;
            min-width: 50px;
          }

          .config-value {
            color: #303133;
            font-size: 13px;
            word-break: break-all;
          }

          .args-list {
            margin-top: 6px;

            .arg-tag {
              margin-right: 6px;
              margin-bottom: 4px;
            }
          }

          .headers-list {
            margin-top: 6px;

            .header-item {
              display: flex;
              align-items: center;
              margin-bottom: 4px;
              font-size: 12px;

              .header-key {
                color: #909399;
                margin-right: 4px;
                min-width: 80px;
              }

              .header-value {
                color: #303133;
                word-break: break-all;
              }
            }
          }
        }
      }
    }

    .card-actions {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
    }
  }

  .empty-state {
    margin: 60px 0;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 30px;
  }

  .dialog-header {
    text-align: left;
    
    h3 {
      margin: 0 0 8px 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
    
    .dialog-subtitle {
      margin: 0;
      font-size: 14px;
      color: #909399;
      font-weight: normal;
    }
  }

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
    line-height: 1.4;
  }

  :deep(.el-form-item) {
    margin-bottom: 10px;
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #303133;
  }

  :deep(.el-input__wrapper) {
    border-radius: 6px;
  }

  :deep(.el-select) {
    width: 100%;
  }

  :deep(.el-textarea .el-input__wrapper) {
    border-radius: 6px;
  }

  .arg-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .header-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
  }
</style>
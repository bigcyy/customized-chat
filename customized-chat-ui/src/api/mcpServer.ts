import { Result } from '@/request/Result'
import { get, post, del, put } from '@/request'
import type { MCPServer } from '@/api/type/mcpServer'
import type { Ref } from 'vue'

const getMcpServerList = (page:number, size:number, loading?: Ref<boolean>) : Promise<Result<any>> => {
    return get("/mcp-server", {page, size}, loading ? loading : undefined)
}

const createMcpServer = (mcpServer: MCPServer, loading?: Ref<boolean>) : Promise<Result<any>> => {
    return post("/mcp-server", mcpServer, {}, loading ? loading : undefined)
}

const updateMcpServer = (mcpServer: MCPServer, loading?: Ref<boolean>) : Promise<Result<any>> => {
    return put(`/mcp-server/${mcpServer.id}`, mcpServer, {}, loading ? loading : undefined)
}

const deleteMcpServer = (id: number, loading?: Ref<boolean>) : Promise<Result<any>> => {
    return del(`/mcp-server/${id}`, {}, loading ? loading : undefined)
}

export default {getMcpServerList, createMcpServer, updateMcpServer, deleteMcpServer}
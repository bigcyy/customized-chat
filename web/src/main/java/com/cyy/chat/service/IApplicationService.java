package com.cyy.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cyy.chat.model.Application;
import com.cyy.chat.model.McpServer;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author CYY
 * @since 2025-02-25
 */
public interface IApplicationService extends IService<Application> {

    /**
     * 获取应用关联的MCP服务器列表
     */
    List<McpServer> getApplicationMcpServers(Long applicationId);

    /**
     * 更新应用关联的MCP服务器
     */
    void updateApplicationMcpServers(Long applicationId, List<Long> mcpServerIds);

    /**
     * 移除应用的MCP服务器关联
     */
    void removeApplicationMcpServer(Long applicationId, Long mcpServerId);
}

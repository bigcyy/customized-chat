package com.cyy.chat.service;

import com.cyy.chat.model.McpServer;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * MCP服务器配置 服务类
 * </p>
 *
 * @author CYY
 * @since 2025-09-17
 */
public interface IMcpServerService extends IService<McpServer> {

    /**
     * 获取所有启用的MCP服务器
     * @return 启用的MCP服务器列表
     */
    List<McpServer> getEnabledServers();

    /**
     * 根据名称查找服务器
     * @param serverName 服务器名称
     * @return MCP服务器
     */
    McpServer getByServerName(String serverName);

    /**
     * 切换服务器启用状态
     * @param id 服务器ID
     * @return 切换后的启用状态
     */
    Boolean toggleEnabled(Long id);
}

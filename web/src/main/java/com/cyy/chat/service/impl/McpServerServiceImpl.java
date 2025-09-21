package com.cyy.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyy.chat.dao.McpServerMapper;
import com.cyy.chat.model.McpServer;
import com.cyy.chat.service.IMcpServerService;
import com.cyy.common.exception.ClientGlobalException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * MCP服务器配置 服务实现类
 * </p>
 *
 * @author CYY
 * @since 2025-09-17
 */
@Service
public class McpServerServiceImpl extends ServiceImpl<McpServerMapper, McpServer> implements IMcpServerService {

    @Override
    public List<McpServer> getEnabledServers() {
        LambdaQueryWrapper<McpServer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(McpServer::getIsDeleted, false)
                   .eq(McpServer::getEnabled, true)
                   .orderByDesc(McpServer::getCreateTime);
        return this.list(queryWrapper);
    }

    @Override
    public McpServer getByServerName(String serverName) {
        LambdaQueryWrapper<McpServer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(McpServer::getServerName, serverName)
                   .eq(McpServer::getIsDeleted, false);
        return this.getOne(queryWrapper);
    }

    @Override
    public Boolean toggleEnabled(Long id) {
        McpServer mcpServer = this.getById(id);
        if (mcpServer == null || mcpServer.getIsDeleted()) {
            throw new ClientGlobalException("MCP服务器不存在");
        }
        
        Boolean newEnabledStatus = !mcpServer.getEnabled();
        mcpServer.setEnabled(newEnabledStatus);
        this.updateById(mcpServer);
        
        return newEnabledStatus;
    }
}

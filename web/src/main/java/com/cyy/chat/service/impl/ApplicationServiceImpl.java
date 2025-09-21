package com.cyy.chat.service.impl;

import com.cyy.chat.dao.ApplicationMapper;
import com.cyy.chat.model.Application;
import com.cyy.chat.model.McpServer;
import com.cyy.chat.service.IApplicationService;
import com.cyy.chat.service.IMcpServerService;
import com.cyy.common.exception.ClientGlobalException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author CYY
 * @since 2025-02-25
 */
@Service
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application> implements IApplicationService {

    @Resource
    private IMcpServerService mcpServerService;

    @Override
    public List<McpServer> getApplicationMcpServers(Long applicationId) {
        Application application = this.getById(applicationId);
        if (application == null) {
            throw new ClientGlobalException("应用不存在");
        }
        
        List<Long> mcpServerIds = application.getMcpServerIds();
        if (mcpServerIds == null || mcpServerIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return mcpServerService.listByIds(mcpServerIds);
    }

    @Override
    public void updateApplicationMcpServers(Long applicationId, List<Long> mcpServerIds) {
        Application application = this.getById(applicationId);
        if (application == null) {
            throw new ClientGlobalException("应用不存在");
        }
        
        application.setMcpServerIds(mcpServerIds);
        this.updateById(application);
    }

    @Override
    public void removeApplicationMcpServer(Long applicationId, Long mcpServerId) {
        Application application = this.getById(applicationId);
        if (application == null) {
            throw new ClientGlobalException("应用不存在");
        }
        
        List<Long> mcpServerIds = application.getMcpServerIds();
        if (mcpServerIds != null) {
            mcpServerIds = new ArrayList<>(mcpServerIds); // 创建可修改的副本
            mcpServerIds.remove(mcpServerId);
            application.setMcpServerIds(mcpServerIds);
            this.updateById(application);
        }
    }
}

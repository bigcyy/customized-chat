package com.cyy.chat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyy.chat.model.McpServer;
import com.cyy.chat.service.IMcpServerService;
import com.cyy.common.exception.ClientGlobalException;
import com.cyy.common.utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * MCP服务器配置 前端控制器
 * </p>
 *
 * @author CYY
 * @since 2025-09-17
 */
@RestController
@RequestMapping("/api/v1/mcp-server")
@Tag(name = "MCP服务器管理", description = "MCP服务器配置相关接口")
public class McpServerController {

    @Resource
    private IMcpServerService mcpServerService;

    @PostMapping
    @Operation(summary = "创建MCP服务器", description = "创建新的MCP服务器配置")
    public R create(@RequestBody McpServer mcpServer) {
        // 验证必填字段
        if (!StringUtils.hasText(mcpServer.getServerName())) {
            throw new ClientGlobalException("服务器名称不能为空");
        }
        if (!StringUtils.hasText(mcpServer.getType())) {
            throw new ClientGlobalException("传输类型不能为空");
        }
        
        // 根据类型验证对应字段
        if ("stdio".equals(mcpServer.getType())) {
            if (!StringUtils.hasText(mcpServer.getCommand())) {
                throw new ClientGlobalException("stdio类型必须指定命令");
            }
        } else if ("sse".equals(mcpServer.getType())) {
            if (!StringUtils.hasText(mcpServer.getSseUrl())) {
                throw new ClientGlobalException("sse类型必须指定URL");
            }
        } else {
            throw new ClientGlobalException("不支持的传输类型");
        }

        // 设置默认值
        mcpServer.setId(null); // 防止前端传入ID
        if (mcpServer.getEnabled() == null) {
            mcpServer.setEnabled(true);
        }
        if (mcpServer.getIsDeleted() == null) {
            mcpServer.setIsDeleted(false);
        }

        mcpServerService.save(mcpServer);
        return R.ok().data("id", mcpServer.getId());
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新MCP服务器", description = "更新指定ID的MCP服务器配置")
    public R update(@Parameter(description = "服务器ID") @PathVariable Long id, 
                    @RequestBody McpServer mcpServer) {
        McpServer existingServer = mcpServerService.getById(id);
        if (existingServer == null || existingServer.getIsDeleted()) {
            throw new ClientGlobalException("MCP服务器不存在");
        }

        mcpServer.setId(id);
        mcpServerService.updateById(mcpServer);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除MCP服务器", description = "逻辑删除指定ID的MCP服务器")
    public R delete(@Parameter(description = "服务器ID") @PathVariable Long id) {
        McpServer existingServer = mcpServerService.getById(id);
        if (existingServer == null || existingServer.getIsDeleted()) {
            throw new ClientGlobalException("MCP服务器不存在");
        }

        existingServer.setIsDeleted(true);
        mcpServerService.updateById(existingServer);
        return R.ok();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取MCP服务器详情", description = "根据ID获取MCP服务器详细信息")
    public R getById(@Parameter(description = "服务器ID") @PathVariable Long id) {
        McpServer mcpServer = mcpServerService.getById(id);
        if (mcpServer == null || mcpServer.getIsDeleted()) {
            throw new ClientGlobalException("MCP服务器不存在");
        }
        return R.ok().data("server", mcpServer);
    }

    @GetMapping
    @Operation(summary = "获取MCP服务器列表", description = "分页查询MCP服务器列表，支持按名称搜索")
    public R list(@Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
                  @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
                  @Parameter(description = "搜索关键词") @RequestParam(required = false) String search,
                  @Parameter(description = "是否启用") @RequestParam(required = false) Boolean enabled) {
        
        Page<McpServer> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<McpServer> queryWrapper = new LambdaQueryWrapper<>();
        
        // 不查询已删除的记录
        queryWrapper.eq(McpServer::getIsDeleted, false);
        
        // 根据搜索条件过滤
        if (StringUtils.hasText(search)) {
            queryWrapper.and(wrapper -> wrapper
                .like(McpServer::getServerName, search)
                .or()
                .like(McpServer::getServerDescription, search)
            );
        }
        
        // 根据启用状态过滤
        if (enabled != null) {
            queryWrapper.eq(McpServer::getEnabled, enabled);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(McpServer::getCreateTime);
        
        Page<McpServer> result = mcpServerService.page(pageParam, queryWrapper);
        return R.ok().data("servers", result);
    }

    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的MCP服务器", description = "获取所有启用状态的MCP服务器列表")
    public R getEnabledServers() {
        LambdaQueryWrapper<McpServer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(McpServer::getIsDeleted, false)
                   .eq(McpServer::getEnabled, true)
                   .orderByDesc(McpServer::getCreateTime);
        
        List<McpServer> servers = mcpServerService.list(queryWrapper);
        return R.ok().data("servers", servers);
    }

    @PutMapping("/{id}/toggle")
    @Operation(summary = "切换MCP服务器启用状态", description = "启用或禁用指定的MCP服务器")
    public R toggleEnabled(@Parameter(description = "服务器ID") @PathVariable Long id) {
        McpServer existingServer = mcpServerService.getById(id);
        if (existingServer == null || existingServer.getIsDeleted()) {
            throw new ClientGlobalException("MCP服务器不存在");
        }

        existingServer.setEnabled(!existingServer.getEnabled());
        mcpServerService.updateById(existingServer);
        return R.ok().data("enabled", existingServer.getEnabled());
    }
}

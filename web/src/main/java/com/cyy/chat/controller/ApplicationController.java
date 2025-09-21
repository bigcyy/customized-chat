package com.cyy.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyy.chat.controller.dto.ApplicationDto;
import com.cyy.chat.model.Application;
import com.cyy.chat.model.McpServer;
import com.cyy.chat.model.McpSetting;
import com.cyy.chat.model.ModelSetting;
import com.cyy.chat.service.IApplicationService;
import com.cyy.chat.service.IMcpServerService;
import com.cyy.common.converter.BeanConverter;
import com.cyy.common.exception.ClientGlobalException;
import com.cyy.common.utils.R;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author CYY
 * @since 2025-02-25
 */
@Controller
@RestController
@RequestMapping("/api/v1/application")
@Tag(name = "应用相关模块", description = "应用相关接口")
public class ApplicationController {

    @Resource
    private IApplicationService applicationService;

    @Resource
    private IMcpServerService mcpServerService;

    @PostMapping
    public R add(@RequestBody ApplicationDto applicationDto) {
        Application app = BeanConverter
                .source(applicationDto).target(Application.class)
                .streamMap(ApplicationDto::getModelSetting)
                .to((target, dto) ->{
                    try {
                        String modelSettingString = new ObjectMapper().writeValueAsString(dto);
                        target.setModelSetting(modelSettingString);
                    } catch (JsonProcessingException e) {
                        throw new IllegalArgumentException("模型配置转换失败", e);
                    }
                })
                .convert();
        app.setApplicationType("Agent");
        app.setMcpServerIds(applicationDto.getMcpServerIds());
        applicationService.save(app);
        return R.ok().data("id",app.getId());
    }

    @PutMapping
    @Operation(summary = "更新应用信息")
    public R update(@RequestBody ApplicationDto applicationDto) {
        Application application = new Application();
        ModelSetting modelSetting = applicationDto.getModelSetting();
        if(modelSetting != null){
            try {
                String modelSettingString = new ObjectMapper().writeValueAsString(modelSetting);
                application.setModelSetting(modelSettingString);
            } catch (JsonProcessingException e) {
                throw new ClientGlobalException("模型配置转换失败", e.getMessage());
            }
        }

        McpSetting mcpSetting = applicationDto.getMcpSetting();
        if(mcpSetting != null){
            try {
                String mcpSettingString = new ObjectMapper().writeValueAsString(mcpSetting);
                application.setMcpSetting(mcpSettingString);
            } catch (JsonProcessingException e) {
                throw new ClientGlobalException("模型配置转换失败", e.getMessage());
            }
        }
        BeanUtils.copyProperties(applicationDto, application);
        applicationService.updateById(application);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除应用")
    public R delete(@Parameter(description = "应用 id", example = "1") @PathVariable Long id) {
        applicationService.removeById(id);
        return R.ok();
    }

    @GetMapping("/{applicationId}")
    public R get(@Parameter(description = "应用 id", example = "1") @PathVariable Long applicationId) {
        Application application = applicationService.getById(applicationId);
        ApplicationDto applicationDto = new ApplicationDto();
        Optional.ofNullable(application.getModelSetting())
                .ifPresent(modelSetting -> {
                    try {
                        ModelSetting modelSettingObj = new ObjectMapper().readValue(modelSetting, ModelSetting.class);
                        applicationDto.setModelSetting(modelSettingObj);
                    } catch (JsonProcessingException e) {
                        throw new ClientGlobalException("模型配置转换失败", e.getMessage());
                    }
        });
        Optional.ofNullable(application.getMcpSetting())
                .ifPresent(mcpSetting -> {
                    try {
                        McpSetting mcpSettingObj = new ObjectMapper().readValue(mcpSetting, McpSetting.class);
                        applicationDto.setMcpSetting(mcpSettingObj);
                    }catch (JsonProcessingException e){
                        throw new ClientGlobalException("mcp 配置转换失败", e.getMessage());
                    }
                });
        // 排除 mcpServerIds，因为我们需要单独处理
        BeanUtils.copyProperties(application, applicationDto, "mcpServerIds");
        // 单独设置MCP服务器ID列表，确保类型安全
        List<Long> mcpServerIds = application.getMcpServerIds();
        System.out.println("DEBUG GET: mcpServerIds type: " + (mcpServerIds != null ? mcpServerIds.getClass() : "null"));
        if (mcpServerIds != null && !mcpServerIds.isEmpty()) {
            System.out.println("DEBUG GET: first element type: " + mcpServerIds.get(0).getClass());
        }
        applicationDto.setMcpServerIds(mcpServerIds);
        return R.ok().data("application",applicationDto);
    }



    @GetMapping
    @Operation(summary = "获取应用列表")
    public R list(
            @Parameter(description = "List current page", example = "0") @RequestParam(defaultValue = "0") int pageIndex,
            @Parameter(description = "Number of list pagination ", example = "8") @RequestParam(defaultValue = "8") int pageSize) {
        // query page from database
        Page<Application> page = new Page<>(pageIndex, pageSize);
        applicationService.page(page);
        // page<entity> -> page<dto>
        List<ApplicationDto> applicationDtoList = page.getRecords().stream().map(item -> {
            ApplicationDto applicationDto = new ApplicationDto();
            // 排除 mcpServerIds，因为我们需要单独处理
            BeanUtils.copyProperties(item, applicationDto, "mcpServerIds");
            try {
                if (item.getModelSetting() != null) {
                    applicationDto.setModelSetting(new ObjectMapper().readValue(item.getModelSetting(), ModelSetting.class));
                }
                // 单独设置MCP服务器ID列表，确保类型安全
                List<Long> mcpServerIds = item.getMcpServerIds();
                System.out.println("DEBUG: mcpServerIds type: " + (mcpServerIds != null ? mcpServerIds.getClass() : "null"));
                if (mcpServerIds != null && !mcpServerIds.isEmpty()) {
                    System.out.println("DEBUG: first element type: " + mcpServerIds.get(0).getClass());
                }
                applicationDto.setMcpServerIds(mcpServerIds);
                return applicationDto;
            } catch (JsonProcessingException e) {
                throw new ClientGlobalException("模型配置转换失败", e.getMessage());
            }
        }).toList();
        Page<ApplicationDto> dtoPage = new Page<>();
        BeanUtils.copyProperties(page, dtoPage);
        dtoPage.setRecords(applicationDtoList);

        return R.ok().data("agents",dtoPage);
    }

    @GetMapping("/{applicationId}/mcp-servers")
    @Operation(summary = "获取应用关联的MCP服务器")
    public R getApplicationMcpServers(@Parameter(description = "应用 id", example = "1") @PathVariable Long applicationId) {
        Application application = applicationService.getById(applicationId);
        if (application == null) {
            throw new ClientGlobalException("应用不存在");
        }
        
        List<McpServer> mcpServers = applicationService.getApplicationMcpServers(applicationId);
        return R.ok().data("mcpServers", mcpServers);
    }

//    @PostMapping("/{applicationId}/mcp-servers")
//    @Operation(summary = "为应用添加MCP服务器")
//    public R addApplicationMcpServer(
//            @Parameter(description = "应用 id", example = "1") @PathVariable Long applicationId,
//            @RequestBody List<Long> mcpServerIds) {
//        applicationService.updateApplicationMcpServers(applicationId, mcpServerIds);
//        return R.ok();
//    }
//
//    @DeleteMapping("/{applicationId}/mcp-servers/{mcpServerId}")
//    @Operation(summary = "移除应用的MCP服务器关联")
//    public R removeApplicationMcpServer(
//            @Parameter(description = "应用 id", example = "1") @PathVariable Long applicationId,
//            @Parameter(description = "MCP服务器 id", example = "1") @PathVariable Long mcpServerId) {
//        applicationService.removeApplicationMcpServer(applicationId, mcpServerId);
//        return R.ok();
//    }
}

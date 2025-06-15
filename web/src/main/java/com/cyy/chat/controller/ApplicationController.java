package com.cyy.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyy.chat.controller.dto.ApplicationDto;
import com.cyy.chat.model.Application;
import com.cyy.chat.model.ModelSetting;
import com.cyy.chat.service.IApplicationService;
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
        BeanUtils.copyProperties(application, applicationDto);
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
            BeanUtils.copyProperties(item, applicationDto);
            try {
                if (item.getModelSetting() != null) {
                    applicationDto.setModelSetting(new ObjectMapper().readValue(item.getModelSetting(), ModelSetting.class));
                }
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
}

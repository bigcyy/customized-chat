package com.cyy.chat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyy.chat.controller.dto.ApplicationDto;
import com.cyy.chat.controller.dto.TempChatDto;
import com.cyy.chat.model.Application;
import com.cyy.chat.model.ChatMessage;
import com.cyy.chat.model.ChatSession;
import com.cyy.chat.model.ModelSetting;
import com.cyy.chat.service.IApplicationService;
import com.cyy.chat.service.IChatRecordService;
import com.cyy.chat.service.IChatSessionService;
import com.cyy.common.converter.BeanConverter;
import com.cyy.common.exception.ClientGlobalException;
import com.cyy.common.utils.R;
import com.cyy.common.utils.SnowFlakeIdGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

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
    private IChatSessionService chatSessionService;

    @Resource
    private IChatRecordService chatRecordService;

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
        Application app = BeanConverter.source(applicationDto).target(Application.class).convert();
        applicationService.updateById(app);
        return R.ok();
    }

    @DeleteMapping
    @Operation(summary = "删除应用")
    public R delete(
            @Parameter(description = "应用 id", example = "1") @RequestParam Long applicationId) {
        applicationService.removeById(applicationId);
        return R.ok();
    }

    @GetMapping("/{applicationId}")
    public R get(@Parameter(description = "应用 id", example = "1") @PathVariable Long applicationId) {
        Application application = applicationService.getById(applicationId);
        return R.ok().data("application",application);
    }

    @PostMapping("/{applicationId}/chat/session")
    @Operation(
            summary = "打开指定应用的聊天会话"
    )
    public R openChatSession(@PathVariable Long applicationId){
        ChatSession session = ChatSession.builder()
                .applicationId(applicationId)
                .isDeleted(false)
                .chatAbstract("新聊天")
                .build();
        // todo: 优化为先存储至缓存，当有聊天时才存储到数据库
        chatSessionService.save(session);
        return R.ok().data("sessionId",session.getId());
    }

    @PostMapping("/temp/chat/session")
    @Operation(
            summary = "打开临时聊天会话"
    )
    public R openTempChatSession(){
        // todo 添加缓存后这里可以预处理数据
        return R.ok().data("sessionId", SnowFlakeIdGenerator.generateId());
    }

    @PostMapping(value = "/temp/chat/session/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String,String>> tempChat(@PathVariable Long sessionId, @RequestBody TempChatDto tempChatDto){
        // todo: 通过 sessionId 去获取对话
        ChatSession session = ChatSession.builder().id(sessionId).build();
        return chatRecordService.tempChat(tempChatDto.getApplication(),session,tempChatDto.getChatMessage(),tempChatDto.getChatHistories())
                .map(message -> Map.of("message", message));
    }

    @PostMapping(value = "/{applicationId}/chat/session/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@PathVariable Long applicationId, @PathVariable Long sessionId, @RequestBody ChatMessage userMessage){
        // 检查应用 id
        Application application = applicationService.getById(applicationId);
        if(application == null){
            throw new ClientGlobalException("应用不存在");
        }
        // 检查会话 id
        // todo: 优化为从缓存读取
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<ChatSession>()
                .and(w -> w.eq(ChatSession::getId, sessionId))
                .and(w -> w.eq(ChatSession::getApplicationId, applicationId));
        ChatSession chatSession = chatSessionService.getOne(queryWrapper);
        if(chatSession == null || chatSession.getIsDeleted()){
            throw new ClientGlobalException("会话不存在");
        }
        // 执行聊天
        return chatRecordService.chat(application, chatSession, userMessage);
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

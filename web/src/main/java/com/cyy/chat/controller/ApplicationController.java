package com.cyy.chat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyy.chat.controller.dto.ApplicationDto;
import com.cyy.chat.model.Application;
import com.cyy.chat.model.ChatMessage;
import com.cyy.chat.model.ChatRecord;
import com.cyy.chat.model.ChatSession;
import com.cyy.chat.service.IApplicationService;
import com.cyy.chat.service.IChatRecordService;
import com.cyy.chat.service.IChatSessionService;
import com.cyy.common.converter.BeanConverter;
import com.cyy.common.exception.ClientGlobalException;
import com.cyy.common.utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

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
        Application app = BeanConverter.source(applicationDto).target(Application.class)
                .convert();
        applicationService.save(app);
        return R.ok().data("applicationId",app.getId());
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
        return chatRecordService.chat(application,chatSession, userMessage);
    }

}

package com.cyy.chat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyy.chat.controller.dto.TempChatDto;
import com.cyy.chat.controller.vo.AiResponseVO;
import com.cyy.chat.model.Application;
import com.cyy.chat.model.ChatMessage;
import com.cyy.chat.model.ChatSession;
import com.cyy.chat.service.IApplicationService;
import com.cyy.chat.service.IChatMessageService;
import com.cyy.chat.service.IChatSessionService;
import com.cyy.common.exception.ClientGlobalException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "聊天消息", description = "聊天消息相关接口")
public class ChatMessageController {

    @Resource
    private IChatMessageService chatMessageService;
    
    @Resource
    private IApplicationService applicationService;
    
    @Resource
    private IChatSessionService chatSessionService;

    /**
     * 临时聊天
     */
    @PostMapping(value = "/temp/chat/session/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "临时聊天")
    public Flux<AiResponseVO> tempChat(@PathVariable Long sessionId, @RequestBody TempChatDto tempChatDto){
        // todo: 通过 sessionId 去获取对话
        ChatSession session = ChatSession.builder().id(sessionId).build();
        return chatMessageService.tempChat(tempChatDto.getApplication(),session,tempChatDto.getChatMessage(),tempChatDto.getChatHistories());
    }

    /**
     * 应用聊天
     */
    @PostMapping(value = "/application/{applicationId}/chat/session/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "应用聊天")
    public Flux<AiResponseVO> chat(@PathVariable Long applicationId, @PathVariable Long sessionId, @RequestBody ChatMessage userMessage){
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
        return chatMessageService.chat(application, chatSession, userMessage);
    }
}
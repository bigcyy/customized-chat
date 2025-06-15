package com.cyy.chat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyy.chat.model.ChatMessage;
import com.cyy.chat.model.ChatSession;
import com.cyy.chat.service.IApplicationService;
import com.cyy.chat.service.IChatMessageService;
import com.cyy.chat.service.IChatRecordService;
import com.cyy.chat.service.IChatSessionService;
import com.cyy.common.exception.ClientGlobalException;
import com.cyy.common.utils.R;
import com.cyy.common.utils.SnowFlakeIdGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 聊天会话表 前端控制器
 * </p>
 *
 * @author CYY
 * @since 2025-03-10
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "聊天会话管理", description = "聊天会话相关接口")
public class ChatSessionController {

    @Resource
    private IChatSessionService chatSessionService;

    @Resource
    private IChatMessageService chatMessageService;
    
    @Resource
    private IChatRecordService chatRecordService;
    
    @Resource
    private IApplicationService applicationService;

    /**
     * 获取应用的聊天会话列表
     */
    @GetMapping("/application/{applicationId}/chat/sessions")
    @Operation(summary = "获取应用的聊天会话列表")
    public R getChatSessions(@PathVariable Long applicationId) {
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getApplicationId, applicationId)
                .eq(ChatSession::getIsDeleted, false)
                .orderByDesc(ChatSession::getUpdateTime);

        List<ChatSession> sessions = chatSessionService.list(queryWrapper);

        return R.ok().data("sessions", sessions);
    }

    /**
     * 获取单个聊天会话详情
     */
    @GetMapping("/chat/session/{sessionId}")
    @Operation(summary = "获取单个聊天会话详情")
    public R getChatSession(@PathVariable Long sessionId) {
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null || session.getIsDeleted()) {
            throw new ClientGlobalException("会话不存在");
        }

        return R.ok().data("session", session);
    }

    /**
     * 获取会话的聊天消息
     */
    @GetMapping("/chat/session/{sessionId}/messages")
    @Operation(summary = "获取会话的聊天消息")
    public R getSessionMessages(@PathVariable Long sessionId) {
        // 验证会话是否存在
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null || session.getIsDeleted()) {
            throw new ClientGlobalException("会话不存在");
        }

        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getMessageIndex);

        List<ChatMessage> messages = chatMessageService.list(queryWrapper);
        return R.ok().data("messages", messages);
    }
    /**
     * 更新聊天会话
     */
    @PutMapping("/chat/session/{sessionId}")
    @Operation(summary = "更新聊天会话")
    public R updateChatSession(@PathVariable Long sessionId, @RequestBody ChatSession sessionData) {
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null || session.getIsDeleted()) {
            throw new ClientGlobalException("会话不存在");
        }

        session.setChatAbstract(sessionData.getChatAbstract());
        session.setClientId(sessionData.getClientId());
        session.setUpdateTime(LocalDateTime.now());

        chatSessionService.updateById(session);

        return R.ok().data("session", session);
    }

    /**
     * 删除聊天会话
     */
    @DeleteMapping("/chat/session/{sessionId}")
    @Operation(summary = "删除聊天会话")
    public R deleteSession(@PathVariable Long sessionId) {
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null || session.getIsDeleted()) {
            throw new ClientGlobalException("会话不存在");
        }

        session.setIsDeleted(true);
        session.setUpdateTime(LocalDateTime.now());

        chatSessionService.updateById(session);
        return R.ok().message("删除成功");
    }

    /**
     * 打开指定应用的聊天会话
     */
    @PostMapping("/application/{applicationId}/chat/session")
    @Operation(summary = "打开指定应用的聊天会话")
    public R openChatSession(@PathVariable Long applicationId){
        ChatSession session = ChatSession.builder()
                .applicationId(applicationId)
                .isDeleted(false)
                .chatAbstract("新聊天")
                .build();
        // todo: 优化为先存储至缓存，当有聊天时才存储到数据库
        chatSessionService.save(session);
        return R.ok().data("session",session);
    }

    /**
     * 打开临时聊天会话
     */
    @PostMapping("/temp/chat/session")
    @Operation(summary = "打开临时聊天会话")
    public R openTempChatSession(){
        // todo 添加缓存后这里可以预处理数据
        return R.ok().data("sessionId", SnowFlakeIdGenerator.generateId());
    }
}

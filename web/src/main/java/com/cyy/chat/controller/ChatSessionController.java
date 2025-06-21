package com.cyy.chat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyy.chat.model.ChatMessage;
import com.cyy.chat.model.ChatSession;
import com.cyy.chat.service.IChatMessageService;
import com.cyy.chat.service.IChatSessionService;
import com.cyy.common.exception.ClientGlobalException;
import com.cyy.common.exception.SystemGlobalException;
import com.cyy.common.utils.R;
import com.cyy.common.utils.SnowFlakeIdGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

import com.cyy.chat.controller.dto.ChatMessageDto;
import com.cyy.chat.model.MessageType;
import com.cyy.chat.model.ToolExecutionDetail;
import com.cyy.chat.model.ToolExecutionReq;
import com.cyy.chat.model.ToolExecutionResp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class ChatSessionController {

    @Resource
    private IChatSessionService chatSessionService;

    @Resource
    private IChatMessageService chatMessageService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

        // 转为 messages dto
        List<ChatMessageDto> messageDtoList = convertToMessageDtoList(messages);
        
        return R.ok().data("messages", messageDtoList);
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

    /**
     * 将ChatMessage列表转换为ChatMessageDto列表
     */
    private List<ChatMessageDto> convertToMessageDtoList(List<ChatMessage> messages) {
        List<ChatMessageDto> dtoList = messages.stream()
                .filter(message -> MessageType.valueOfRole(message.getRole()) != MessageType.SYSTEM) // 跳过系统消息
                .map(this::convertToMessageDto).toList();
        if (dtoList.isEmpty()) {
            return List.of();
        }
        List<ChatMessageDto> list = buildMessage(dtoList, dtoList.get(0), 0);
        return mergeAiMessage(list);
    }

    private List<ChatMessageDto> mergeAiMessage(List<ChatMessageDto> list) {
        List<ChatMessageDto> ans = new LinkedList<>();
        if(list.size() == 1) return ans; // 只有一个消息，直接返回

        for(int i = 0; i < list.size();){
            ans.add(list.get(i));  // 添加 user 消息
            ChatMessageDto curMergeHead = null;
            int index = i + 1;
            while (index < list.size() && MessageType.valueOfRole(list.get(index).getRole()) == MessageType.AI) {
                // 合并AI消息
                ChatMessageDto cur = list.get(index);
                if (curMergeHead == null) {
                    curMergeHead = cur;
                    ans.add(curMergeHead);
                } else {
                    // 合并到当前合并头
                    curMergeHead.setMessageText(curMergeHead.getMessageText() + cur.getMessageText());
                    if(curMergeHead.getToolExecutionDetail() != null &&
                            cur.getToolExecutionDetail() != null) {
                        curMergeHead.getToolExecutionDetail().addAll(cur.getToolExecutionDetail());
                    }
                }
                index++;
            }
            i = index;
        }
        return ans;
    }

    /**
     * 将单个ChatMessage转换为ChatMessageDto
     */
    private ChatMessageDto convertToMessageDto(ChatMessage message) {
        ChatMessageDto dto = ChatMessageDto.builder()
                .id(message.getId())
                .sessionId(message.getSessionId())
                .messageIndex(message.getMessageIndex())
                .messageText(message.getMessageText())
                .messageToken(message.getMessageToken())
                .updateTime(message.getUpdateTime())
                .createTime(message.getCreateTime())
                .role(message.getRole())
                .build();
        
        // 解析工具执行响应（仅对tool_execution_result类型消息有效，但这些消息会被跳过）
        if (message.getToolExecutionResp() != null && !message.getToolExecutionResp().isEmpty()) {
            try {
                ToolExecutionResp toolResp = objectMapper.readValue(
                    message.getToolExecutionResp(), ToolExecutionResp.class);
                dto.setToolExecutionResp(toolResp);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse toolExecutionResp: {}", message.getToolExecutionResp(), e);
            }
        }
        
        // 解析工具执行请求
        if (message.getToolExecutionReqs() != null && !message.getToolExecutionReqs().isEmpty()) {
            try {
                List<ToolExecutionReq> toolReqs = objectMapper.readValue(
                    message.getToolExecutionReqs(), new TypeReference<List<ToolExecutionReq>>() {});
                dto.setToolExecutionReqs(toolReqs);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse toolExecutionReqs: {}", message.getToolExecutionReqs(), e);
            }
        }
        
        return dto;
    }

    /**
     * 查找并构建工具执行详情
     */
    private ToolExecutionDetail buildToolDetail(@NonNull List<ToolExecutionReq> toolExecutionReqs, ChatMessageDto tool) {
        if(toolExecutionReqs.size() != 1) throw new IllegalArgumentException("ToolExecutionReqs must contain exactly one request");
        ToolExecutionReq toolExecutionReq = toolExecutionReqs.get(0);

        if(tool.getToolExecutionResp() == null) {
            log.warn("Tool execution response is empty for message: {}", tool);
            throw new SystemGlobalException("Tool execution response is empty");
        }

        if(!tool.getToolExecutionResp().getId().equals(toolExecutionReq.getId())){
            log.warn("Tool execution response ID does not match request ID: reqId={}, respId={}",
                    toolExecutionReq.getId(), tool.getToolExecutionResp().getId());
            throw new SystemGlobalException("Tool execution response ID does not match request ID");
        }
        return ToolExecutionDetail.builder()
                .result(tool.getToolExecutionResp().getText())
                .request(toolExecutionReq)
                .build();

    }

    private List<ChatMessageDto> buildMessage(List<ChatMessageDto> all, ChatMessageDto root, int index){
        List<ChatMessageDto> children = new ArrayList<>(all.size() - index);
        if(index == all.size()) return children;
        if(index + 1 == all.size()) {
            children.add(root);
            return children;
        }
        if(MessageType.valueOfRole(root.getRole()) == MessageType.USER){
            children.add(root);
            children.addAll(buildMessage(all, all.get(index + 1), index + 1));
            return children;
        }
        if(MessageType.valueOfRole(root.getRole()) == MessageType.AI){
            // 后面的tool合并到root中
            int i = index + 1;
            List<ToolExecutionDetail> details = new ArrayList<>();
            for(; i < all.size(); i++) {
                ChatMessageDto next = all.get(i);
                if(MessageType.valueOfRole(next.getRole()) != MessageType.TOOL_EXECUTION_RESULT) {
                    // 遇到下一个用户消息或者ai，停止合并
                    break;
                }
                // 合并工具执行结果
                ToolExecutionDetail toolDetail = buildToolDetail(root.getToolExecutionReqs(), next);
                details.add(toolDetail);
            }
            root.setToolExecutionDetail(details);
            if (i == all.size()){
                children.add(root);
            } else {
                children.add(root);
                children.addAll(buildMessage(all,all.get(i), i));
            }
            return children;
        }
        throw new SystemGlobalException("unexpected message type: " + root.getRole());
    }
}

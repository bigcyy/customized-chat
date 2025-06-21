package com.cyy.chat.utils;


import com.cyy.chat.model.ChatMessage;
import com.cyy.chat.model.MessageType;
import com.cyy.chat.model.ToolExecutionReq;
import com.cyy.chat.model.ToolExecutionResp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MessageConverter {
    public static dev.langchain4j.data.message.ChatMessage toL4jMessage(@NonNull ChatMessage chatMessage){
        ChatMessageType msgType = RoleTypeAdaptor.getMsgType(chatMessage.getRole());
        if (msgType == ChatMessageType.AI){
            List<ToolExecutionRequest> toolExecutionRequests = List.of();
            if(chatMessage.getToolExecutionReqs() != null && !chatMessage.getToolExecutionReqs().isBlank()){
                try {
                    List<ToolExecutionReq> toolExecutionReqList = new ObjectMapper().readValue(chatMessage.getToolExecutionReqs(), new TypeReference<>() {});
                    toolExecutionRequests = toolExecutionReqList.stream()
                            .map(toolReq -> ToolExecutionRequest.builder()
                                    .id(toolReq.getId())
                                    .name(toolReq.getName())
                                    .arguments(toolReq.getArguments())
                                    .build()).toList();
                } catch (JsonProcessingException e) {
                    log.error("Failed to parse tool execution requests from JSON: {}", chatMessage.getToolExecutionReqs(), e);
                    throw new IllegalArgumentException("Invalid tool execution requests format", e);
                }
            }
            return AiMessage.from(chatMessage.getMessageText(),toolExecutionRequests);
        } else if (msgType == ChatMessageType.USER) {
            // todo 考虑其他数据类型
            return UserMessage.builder()
                    .contents(List.of(new TextContent(chatMessage.getMessageText())))
                    .build();
        } else if (msgType == ChatMessageType.SYSTEM) {
            return SystemMessage.from(chatMessage.getMessageText());
        } else if (msgType == ChatMessageType.TOOL_EXECUTION_RESULT) {
            ToolExecutionResp toolExecutionResp;
            try {
                toolExecutionResp = new ObjectMapper().readValue(chatMessage.getToolExecutionResp(), ToolExecutionResp.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse tool execution response from JSON: {}", chatMessage.getToolExecutionResp(), e);
                throw new IllegalArgumentException("Invalid tool execution response format", e);
            }
            if (toolExecutionResp == null) {
                throw new IllegalArgumentException("Tool execution response cannot be null for TOOL_EXECUTION_RESULT message type");
            }
            return ToolExecutionResultMessage.from(toolExecutionResp.getId(), toolExecutionResp.getToolName(), toolExecutionResp.getText());
        } else {
            throw new IllegalArgumentException("msg type not supported");
        }
    }

    public static ChatMessage.ChatMessageBuilder toChatMessage(@NonNull dev.langchain4j.data.message.ChatMessage chatMessage) {
        MessageType role = RoleTypeAdaptor.getRole(chatMessage.type());
        ChatMessage.ChatMessageBuilder messageBuilder = ChatMessage.builder().role(role.getRole());
        if (role == MessageType.AI) {
            AiMessage l4jMessage = (AiMessage) chatMessage;
            messageBuilder.messageText(l4jMessage.text());
            if(l4jMessage.hasToolExecutionRequests()){
                List<ToolExecutionReq> toolExecutionReqs = l4jMessage.toolExecutionRequests().stream()
                        .map(toolReq -> ToolExecutionReq.builder()
                                .id(toolReq.id())
                                .name(toolReq.name())
                                .arguments(toolReq.arguments())
                                .build()).toList();
                try {
                    messageBuilder.toolExecutionReqs(new ObjectMapper().writeValueAsString(toolExecutionReqs));
                } catch (JsonProcessingException e) {
                    log.error("Failed to convert tool execution requests to JSON: {}", toolExecutionReqs, e);
                    throw new IllegalArgumentException("Invalid tool execution requests format", e);
                }
            }
        } else if (role == MessageType.USER) {
            UserMessage l4jMessage = (UserMessage) chatMessage;
            // todo 考虑非文本内容
            messageBuilder.messageText(l4jMessage.singleText());
        } else if (role == MessageType.SYSTEM){
            SystemMessage l4jMessage = (SystemMessage) chatMessage;
            messageBuilder.messageText(l4jMessage.text());
        } else if (role == MessageType.TOOL_EXECUTION_RESULT){
            ToolExecutionResultMessage l4jMessage = (ToolExecutionResultMessage) chatMessage;
            ToolExecutionResp toolExecutionResp = ToolExecutionResp.builder()
                    .id(l4jMessage.id())
                    .toolName(l4jMessage.toolName())
                    .text(l4jMessage.text())
                    .build();
            try {
                messageBuilder.toolExecutionResp(new ObjectMapper().writeValueAsString(toolExecutionResp));
            }catch (JsonProcessingException e){
                log.error("Failed to convert tool execution response to JSON: {}", toolExecutionResp, e);
                throw new IllegalArgumentException("Invalid tool execution response format", e);
            }
        } else {
            throw new IllegalArgumentException("msg type not supported");
        }
        return messageBuilder;
    }
}

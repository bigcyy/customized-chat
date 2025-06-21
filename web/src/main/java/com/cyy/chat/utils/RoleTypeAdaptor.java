package com.cyy.chat.utils;

import com.cyy.chat.model.MessageType;
import dev.langchain4j.data.message.ChatMessageType;
import jakarta.validation.constraints.NotNull;
import org.springframework.util.Assert;

/**
 * 适配 Langchain4j 中的消息类型
 */
public class RoleTypeAdaptor {
    public static ChatMessageType getMsgType(@NotNull String role) {
        Assert.hasText(role, "role must not be empty");
        MessageType messageType = MessageType.valueOfRole(role);
        return getMsgType(messageType);
    }

    public static ChatMessageType getMsgType(@NotNull MessageType msgType) {
        if(msgType == MessageType.AI) {
            return ChatMessageType.AI;
        } else if(msgType == MessageType.USER) {
            return ChatMessageType.USER;
        } else if(msgType == MessageType.SYSTEM) {
            return ChatMessageType.SYSTEM;
        } else if(msgType == MessageType.TOOL_EXECUTION_RESULT) {
            return ChatMessageType.TOOL_EXECUTION_RESULT;
        } else {
            throw new IllegalArgumentException("unknown msg type: " + msgType);
        }
    }

    public static MessageType getRole(@NotNull ChatMessageType msgType) {
        if (msgType == ChatMessageType.AI){
            return MessageType.AI;
        } else if(msgType == ChatMessageType.USER){
            return MessageType.USER;
        } else if(msgType == ChatMessageType.SYSTEM){
            return MessageType.SYSTEM;
        } else if(msgType == ChatMessageType.TOOL_EXECUTION_RESULT) {
            return MessageType.TOOL_EXECUTION_RESULT;
        } else {
            throw new IllegalArgumentException("unknown msgType: " + msgType);
        }
    }
}

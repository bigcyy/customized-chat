package com.cyy.chat.utils;

import dev.langchain4j.data.message.ChatMessageType;
import org.springframework.util.Assert;

/**
 * 适配 Langchain4j 中的消息类型
 * todo 将消息类型字符串改为枚举类
 */
public class RoleTypeAdaptor {
    public static ChatMessageType getMsgType(String role) {
        Assert.hasText(role, "role must not be empty");
        if("assistant".equals(role)){
            return ChatMessageType.AI;
        } else if ("user".equals(role)) {
            return ChatMessageType.USER;
        }else {
            throw new IllegalArgumentException("unknown role: " + role);
        }
    }

    public static String getRole(ChatMessageType msgType) {
        Assert.notNull(msgType, "msgType must not be null");
        if(msgType == ChatMessageType.AI){
            return "assistant";
        }else if(msgType == ChatMessageType.USER){
            return "user";
        }else {
            throw new IllegalArgumentException("unknown msgType: " + msgType);
        }
    }
}

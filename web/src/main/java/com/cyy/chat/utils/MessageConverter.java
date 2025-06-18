package com.cyy.chat.utils;


import com.cyy.chat.model.ChatMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import org.springframework.util.Assert;

import java.util.List;

public class MessageConverter {
    public static dev.langchain4j.data.message.ChatMessage toLangChain4jMsg(ChatMessage chatMessage){
        ChatMessageType msgType = RoleTypeAdaptor.getMsgType(chatMessage.getRole());
        Assert.notNull(msgType,"msg type not null");
        if(msgType == ChatMessageType.AI){
            return AiMessage.builder()
                    .text(chatMessage.getMessageText())
                    .build();
        }else if(msgType == ChatMessageType.USER){
            return UserMessage.builder()
                    .contents(List.of(new TextContent(chatMessage.getMessageText())))
                    .build();
        }else {
            throw new IllegalArgumentException("msg type not supported");
        }
    }
}

package com.cyy.chat.controller.dto;

import com.cyy.chat.model.Application;
import com.cyy.chat.model.ChatMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 临时聊天的请求对象，包含了应用的信息以及聊天请求信息
 */
@Getter
@Setter
@ToString
public class TempChatDto {
    /**
     * 应用信息
     */
    private Application application;

    /**
     * 聊天请求信息
     */
    private ChatMessage chatMessage;

    /**
     * 聊天记录
     */
    private List<ChatMessage> chatHistories;
}

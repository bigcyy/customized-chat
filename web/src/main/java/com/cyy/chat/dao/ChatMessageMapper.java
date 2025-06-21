package com.cyy.chat.dao;

import com.cyy.chat.model.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author CYY
 * @since 2025-03-14
 */
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    ChatMessage getLastMessage(Long sessionId);

    List<ChatMessage> getChatMessageListBySessionId(Long sessionId);
}


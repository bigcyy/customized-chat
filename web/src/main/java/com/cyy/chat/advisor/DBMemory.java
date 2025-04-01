package com.cyy.chat.advisor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyy.chat.dao.ChatMessageMapper;
import com.cyy.chat.model.ChatMessage;
import com.cyy.common.exception.SystemGlobalException;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DBMemory implements ChatMemory {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Override
    public void add(String conversationId, List<Message> messages) {
        // 查询会话对应的最后一次聊天
        ChatMessage lastMessage = chatMessageMapper.getLastMessage(Long.parseLong(conversationId));
        // 判断最后一次聊天是否是用户信息，若不是抛出异常
        if(lastMessage == null){
            throw new SystemGlobalException("会话异常");
        }

        // 插入新的聊天记录
        if(messages.size() > 1){
            // todo 什么时候会大于1？
        }else{
            // todo token花销
            ChatMessage chatMessage;
            if(MessageType.USER == MessageType.valueOf(lastMessage.getRole())){
                chatMessage = ChatMessage.builder()
                        .messageText(messages.getLast().getText())
                        .messageIndex(lastMessage.getMessageIndex() + 1)
                        .sessionId(Long.parseLong(conversationId))
                        .role(MessageType.ASSISTANT.name())
                        .build();
            }else{
                chatMessage = ChatMessage.builder()
                        .messageText(messages.getLast().getText())
                        .messageIndex(lastMessage.getMessageIndex() + 1)
                        .sessionId(Long.parseLong(conversationId))
                        .role(MessageType.USER.name())
                        .build();
            }

            chatMessageMapper.insert(chatMessage);
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        // 创建查询条件
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, conversationId)
                .orderByDesc(ChatMessage::getMessageIndex)
                .last("LIMIT " + lastN);

        // 执行查询并转换结果
        List<ChatMessage> chatMessages = chatMessageMapper.selectList(queryWrapper);

        // 将结果按照messageIndex正序排列（因为之前是倒序查询的）
        Collections.reverse(chatMessages);

        // 转换为Message对象列表
        return chatMessages.stream()
                .map(msg -> {
                    if(MessageType.USER == MessageType.valueOf(msg.getRole())){
                        return new UserMessage(msg.getMessageText());
                    }else{
                        return new AssistantMessage(msg.getMessageText());
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
    }
}

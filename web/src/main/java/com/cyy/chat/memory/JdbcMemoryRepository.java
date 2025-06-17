package com.cyy.chat.memory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyy.chat.dao.ChatMessageMapper;
import com.cyy.chat.model.ChatMessage;
import com.cyy.common.exception.SystemGlobalException;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JdbcMemoryRepository implements ChatMemoryRepository {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Override
    public List<String> findConversationIds() {
        return List.of();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        // 创建查询条件
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, conversationId)
                .orderByDesc(ChatMessage::getMessageIndex);

        // 执行查询并转换结果
        List<ChatMessage> chatMessages = chatMessageMapper.selectList(queryWrapper);

        // 将结果按照 messageIndex 正序排列（因为之前是倒序查询的）
        Collections.reverse(chatMessages);

        // 转换为Message对象列表
        return chatMessages.stream()
                .map(msg -> {
                    if(MessageType.USER == MessageType.fromValue(msg.getRole())){
                        return new UserMessage(msg.getMessageText());
                    }else{
                        return new AssistantMessage(msg.getMessageText());
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        if(messages == null || messages.isEmpty()){
            throw new SystemGlobalException("DBMemory 不允许添加空消息！");
        }
        // 查询会话对应的最后一次聊天
        ChatMessage lastMessage = chatMessageMapper.getLastMessage(Long.parseLong(conversationId));
        // 判断最后一次聊天是否存在，不存在则插入一条用户消息
        if(lastMessage == null){
            if(messages.get(0).getMessageType() != MessageType.USER){
                throw new SystemGlobalException("第一条消息不允许添加非用户消息！");
            }
            lastMessage = ChatMessage.builder()
                    .messageIndex(0)
                    .messageText(messages.get(0).getText())
                    .sessionId(Long.parseLong(conversationId))
                    .role(MessageType.USER.getValue())
                    .build();
            chatMessageMapper.insert(lastMessage);
            return;
        }

        // 插入新的聊天记录
        if(messages.size() > 1){
            // todo 什么时候会大于1？
        }else{
            // todo token花销
            Message messagesLast = messages.get(messages.size() - 1);
            ChatMessage chatMessage = ChatMessage.builder()
                    .messageText(messagesLast.getText())
                    .messageIndex(lastMessage.getMessageIndex() + 1)
                    .sessionId(Long.parseLong(conversationId))
                    .role(messagesLast.getMessageType().getValue())
                    .build();
            chatMessageMapper.insert(chatMessage);
        }

    }

    @Override
    public void deleteByConversationId(String conversationId) {

    }
}

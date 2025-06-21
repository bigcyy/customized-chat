package com.cyy.chat.memory;

import com.cyy.chat.dao.ChatMessageMapper;
import com.cyy.chat.model.ChatMessage;
import com.cyy.chat.utils.MessageConverter;
import com.cyy.chat.utils.RoleTypeAdaptor;
import com.cyy.common.exception.SystemGlobalException;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JdbcMemoryRepository implements ChatMemoryStore {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    /**
     * 从数据库获取所有历史消息。
     * MessageWindowChatMemory 在初始化时会调用此方法。
     *
     * @param memoryId 会话ID
     * @return 该会话的所有历史消息，按时间升序排列
     */
    @Override
    public List<dev.langchain4j.data.message.ChatMessage> getMessages(Object memoryId) {
        List<ChatMessage> chatMessages = chatMessageMapper.getChatMessageListBySessionId((Long) memoryId);

        // 将实体转换为 LangChain4j 的 ChatMessage 对象
        return chatMessages.stream()
                .map(MessageConverter::toL4jMessage)
                .collect(Collectors.toList());
    }

    /**
     * 将单条新消息持久化到数据库。
     * MessageWindowChatMemory 的 add() 方法会调用此方法。
     *
     * 注意：这个方法现在只期望处理单条消息的插入。
     * 实际的 LangChain4j ChatMemoryStore 接口的 updateMessages 传入的是 List<ChatMessage>，
     * 这与我们期望的“只插入一条新消息”不完全匹配。
     * 为此，我们需要在 MessageWindowChatMemory 中进行适配，确保只将新消息传递过来。
     * 或者，如果 updateMessages 确实收到了整个列表（如 MessageWindowChatMemory 现有逻辑），
     * 我们需要识别并插入最新那条。下面提供两种适应方式。
     *
     * @param memoryId 会话ID
     * @param messages 包含新消息的列表（通常只包含一条）
     */
    @Override
    public void updateMessages(@NonNull Object memoryId, @NonNull List<dev.langchain4j.data.message.ChatMessage> messages) {
        Assert.notEmpty(messages, "Messages list must not be empty");

        Long conversationId = (Long) memoryId;
        dev.langchain4j.data.message.ChatMessage messageToPersist = messages.get(messages.size() - 1);

        // 1.获取数据库中当前会话的所有记录
        List<ChatMessage> chatMessages = chatMessageMapper.getChatMessageListBySessionId(conversationId);
        int nextMessageIndex = (chatMessages != null && !chatMessages.isEmpty()) ? chatMessages.size() : 0;
        // 存储
        ChatMessage.ChatMessageBuilder messageBuilder = MessageConverter.toChatMessage(messageToPersist);
        ChatMessage messageEntity = messageBuilder
                .sessionId(conversationId)
                .messageIndex(nextMessageIndex)
                .build();

        chatMessageMapper.insert(messageEntity);
    }

    /**
     * 应当通过 api 接口删除，不应该通过这个方法删除
     * @param memoryId The ID of the chat memory.
     */
    @Override
    public void deleteMessages(Object memoryId) {
        throw new SystemGlobalException("Unsupported Operation: Deleting chat messages is not allowed in 'store all history' mode.");
    }
}

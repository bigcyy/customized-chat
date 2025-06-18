package com.cyy.chat.memory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyy.chat.dao.ChatMessageMapper;
import com.cyy.chat.model.ChatMessage;
import com.cyy.chat.utils.RoleTypeAdaptor;
import com.cyy.common.exception.SystemGlobalException;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, memoryId)
                .orderByAsc(ChatMessage::getMessageIndex); // 确保按消息索引升序

        List<ChatMessage> chatMessages = chatMessageMapper.selectList(queryWrapper);

        // 将实体转换为 LangChain4j 的 ChatMessage 对象
        return chatMessages.stream()
                .map(msg -> {
                    ChatMessageType type = RoleTypeAdaptor.getMsgType(msg.getRole());
                    switch (type) {
                        case USER:
                            return new UserMessage(msg.getMessageText());
                        case AI:
                            return new AiMessage(msg.getMessageText());
                        case SYSTEM:
                            return new SystemMessage(msg.getMessageText());
                        // 添加其他消息类型的映射，例如 TOOL_EXECUTION_RESULT, TOOL_EXECUTION_REQUEST 等
                        default:
                            throw new IllegalArgumentException("Unsupported chat message role: " + msg.getRole());
                    }
                })
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
    public void updateMessages(Object memoryId, List<dev.langchain4j.data.message.ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return; // 没有消息需要更新
        }

        Long conversationId = (Long) memoryId;

        // 获取数据库中当前会话的最新消息索引
        ChatMessage lastDbMessage = chatMessageMapper.getLastMessage(conversationId);
        int nextMessageIndex = (lastDbMessage != null) ? lastDbMessage.getMessageIndex() + 1 : 0;

        // 获取要插入的最新一条消息（通常 MessageWindowChatMemory 传过来的 list 里，最新添加的就在最后）
        dev.langchain4j.data.message.ChatMessage messageToPersist = messages.get(messages.size() - 1);

        // 检查这条消息是否已经存在（根据文本和角色判断，防止重复插入）
        // 这一步对于防止重复非常重要，尤其是在系统重启后再次添加相同消息的情况下。
        // 一个更健壮的检查可以包括消息的时间戳或哈希值。
        boolean alreadyExists = false;
        if (lastDbMessage != null &&
                lastDbMessage.getMessageText().equals(extractText(messageToPersist)) &&
                RoleTypeAdaptor.getMsgType(lastDbMessage.getRole()) == messageToPersist.type()) {
            alreadyExists = true; // 最后一条消息相同，假设已存在
        }

        if (!alreadyExists) {
            // 构建并插入新的 ChatMessage 实体
            ChatMessage newChatMessage = ChatMessage.builder()
                    .messageText(extractText(messageToPersist))
                    .messageIndex(nextMessageIndex)
                    .sessionId(conversationId)
                    .role(RoleTypeAdaptor.getRole(messageToPersist.type()))
                    .build();
            chatMessageMapper.insert(newChatMessage);
        }
    }

    /**
     * 在你的“存储所有历史记录”的方案中，通常不应该调用此方法。
     * 如果业务需要完全删除某个会话的历史，则应在此处实现。
     *
     * @param memoryId 会话ID
     */
    @Override
    public void deleteMessages(Object memoryId) {
        // 如果你需要存储所有历史记录，这里通常会阻止删除操作
        throw new SystemGlobalException("Unsupported Operation: Deleting chat messages is not allowed in 'store all history' mode.");
        // 如果允许删除，你可以这样实现：
        // chatMessageMapper.delete(new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getSessionId, memoryId));
    }

    private String extractText(dev.langchain4j.data.message.ChatMessage chatMessage) {
        if (chatMessage.type() == ChatMessageType.USER) {
            UserMessage userMessage = (UserMessage) chatMessage;
            return userMessage.singleText();
        } else if (chatMessage.type() == ChatMessageType.AI) {
            AiMessage aiMessage = (AiMessage) chatMessage;
            return aiMessage.text();
        } else if (chatMessage.type() == ChatMessageType.SYSTEM) {
            SystemMessage systemMessage = (SystemMessage) chatMessage;
            return systemMessage.text();
        }
        // 根据需要添加其他消息类型的文本提取
        throw new IllegalArgumentException("Unknown chat message type: " + chatMessage.type());
    }
}

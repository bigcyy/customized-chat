package com.cyy.chat.memory;

import com.cyy.common.exception.SystemGlobalException;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.service.memory.ChatMemoryService;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * 这是一个定制的聊天记忆实现，它结合了滑动窗口机制和数据库持久化。
 * 与标准的 MessageWindowChatMemory 不同，它在添加消息时只将新消息持久化到数据库，
 * 并在内存中维护一个滑动窗口，用于LLM上下文。
 * 启动时从 ChatMemoryStore 加载所有历史消息，然后应用滑动窗口。
 */
@Slf4j
public class PersistentMessageWindowChatMemory implements ChatMemory {

    private final Object id;
    private final Integer maxMessages;
    private final ChatMemoryStore store; // 这里的 store 现在只负责追加和全量读取
    private final List<ChatMessage> messages; // 内存中维护的滑动窗口消息

    private PersistentMessageWindowChatMemory(Builder builder) {
        this.id = builder.id;
        this.maxMessages = builder.maxMessages;
        this.store = builder.store();
        this.messages = new LinkedList<>(); // 使用 LinkedList 以便高效地移除头部

        // 在构建时，从存储中加载所有历史消息并初始化内存窗口
        loadMessagesFromStoreAndApplyWindow();
    }

    /**
     * 从 ChatMemoryStore 加载所有消息，然后应用滑动窗口，填充到内存列表。
     */
    private void loadMessagesFromStoreAndApplyWindow() {
        List<ChatMessage> allHistoricalMessages = store.getMessages(id); // 获取所有历史消息
        synchronized (messages) { // 同步访问内存列表
            messages.clear(); // 清空当前内存
            if (allHistoricalMessages != null && !allHistoricalMessages.isEmpty()) {
                // 如果有系统消息，先处理
                Optional<SystemMessage> systemMessage = findSystemMessage(allHistoricalMessages);
                systemMessage.ifPresent(messages::add);

                // 添加其他非系统消息，并应用滑动窗口
                allHistoricalMessages.stream()
                        .filter(msg -> !(msg instanceof SystemMessage))
                        .forEach(msg -> {
                            messages.add(msg);
                            ensureCapacity(messages, maxMessages); // 确保容量
                        });
            }
            // 确保最终加载后也应用了容量限制
            ensureCapacity(messages, maxMessages);
        }
    }


    @Override
    public Object id() {
        return id;
    }

    /**
     * 添加消息到内存窗口并持久化新消息。
     *
     * @param message 要添加的消息
     */
    @Override
    public void add(ChatMessage message) {
        // 1. 将新消息持久化到数据库
        // 注意：ChatMemoryStore 接口的 updateMessages 期望 List<ChatMessage>。
        // 这里我们只将要添加的这条消息包装成列表，传递给 JdbcMemoryRepository。
        // JdbcMemoryRepository 内部应识别并只插入这条新消息。
        try {
            List<ChatMessage> singleMessageList = Collections.singletonList(message);
            store.updateMessages(id, singleMessageList);
        } catch (Exception e) {
            // 处理持久化失败的情况，例如记录日志或抛出自定义异常
            System.err.println("Error persisting message to store: " + e.getMessage());
            // 根据你的业务需求决定是否要阻止消息添加到内存中
            // 如果持久化失败，通常不应该继续添加到内存，以保持内存与存储一致
            throw new RuntimeException("Failed to persist chat message", e);
        }

        // 2. 将消息添加到内存窗口，并执行滑动窗口逻辑
        synchronized (messages) { // 确保线程安全
            if (message instanceof SystemMessage) {
                Optional<SystemMessage> systemMessage = findSystemMessage(messages);
                if (systemMessage.isPresent()) {
                    if (systemMessage.get().equals(message)) {
                        return; // 不添加相同的系统消息
                    } else {
                        messages.remove(systemMessage.get()); // 替换现有系统消息
                    }
                }
            }
            messages.add(message);
            ensureCapacity(messages, maxMessages);
        }
    }

    private static Optional<SystemMessage> findSystemMessage(List<ChatMessage> messages) {
        return messages.stream()
                .filter(message -> message instanceof SystemMessage)
                .map(message -> (SystemMessage) message)
                .findAny();
    }

    /**
     * 返回当前内存窗口中的消息列表。
     *
     * @return 内存中的消息列表
     */
    @Override
    public List<ChatMessage> messages() {
        synchronized (messages) {
            // 返回一个拷贝，防止外部修改
            return new LinkedList<>(messages);
        }
    }

    private static void ensureCapacity(List<ChatMessage> messages, int maxMessages) {
        while (messages.size() > maxMessages) {
            int messageToEvictIndex = 0;
            if (!messages.isEmpty() && messages.get(0) instanceof SystemMessage) {
                messageToEvictIndex = 1;
            }
            if (messageToEvictIndex >= messages.size()) { // 防止越界
                break;
            }

            ChatMessage evictedMessage = messages.remove(messageToEvictIndex);
            if (evictedMessage instanceof AiMessage aiMessage && aiMessage.hasToolExecutionRequests()) {
                while (messageToEvictIndex < messages.size()
                        && messages.get(messageToEvictIndex) instanceof ToolExecutionResultMessage) {
                    messages.remove(messageToEvictIndex);
                }
            }
        }
    }

    /**
     * 清空内存中的消息，并尝试清空持久化存储。
     */
    @Override
    public void clear() {
        synchronized (messages) {
            messages.clear();
        }
        // 如果你的 JdbcMemoryRepository 允许删除，这里可以调用 store.deleteMessages(id);
        // 但在“存储所有历史”模式下，通常不执行此操作。
        // 如果你希望清空会话上下文但不删除历史记录，则只清空内存即可。
        // 如果要完全清空（包括数据库），你需要修改 JdbcMemoryRepository 的 deleteMessages 方法。
        // 由于你的 JdbcMemoryRepository 抛出异常，这里会捕获或重新抛出。
        try {
            store.deleteMessages(id);
        } catch (SystemGlobalException e) {
            System.err.println("Warning: Could not clear persistent chat memory for ID " + id + ": " + e.getMessage());
            // 决定是吞掉异常还是向上抛出
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Object id = ChatMemoryService.DEFAULT;
        private Integer maxMessages;
        private ChatMemoryStore store;

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder maxMessages(Integer maxMessages) {
            this.maxMessages = maxMessages;
            return this;
        }

        public Builder chatMemoryStore(ChatMemoryStore store) {
            this.store = store;
            return this;
        }

        // 这里的 store() 方法需要修改，因为它现在依赖于外部注入的 store
        // 如果没有提供 store，应该抛出异常，因为 SingleSlotChatMemoryStore 不适用于此方案
        private ChatMemoryStore store() {
            if (store == null) {
                throw new IllegalArgumentException("ChatMemoryStore must be provided for PersistentMessageWindowChatMemory.");
            }
            return store;
        }

        public PersistentMessageWindowChatMemory build() {
            return new PersistentMessageWindowChatMemory(this);
        }
    }

    public static PersistentMessageWindowChatMemory withMaxMessages(int maxMessages, ChatMemoryStore store) {
        return builder().maxMessages(maxMessages).chatMemoryStore(store).build();
    }
}
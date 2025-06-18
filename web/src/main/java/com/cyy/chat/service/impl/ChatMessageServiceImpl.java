package com.cyy.chat.service.impl;

import com.cyy.chat.controller.dto.ApplicationDto;
import com.cyy.chat.memory.JdbcMemoryRepository;
import com.cyy.chat.memory.PersistentMessageWindowChatMemory;
import com.cyy.chat.model.Application;
import com.cyy.chat.model.ChatMessage;
import com.cyy.chat.dao.ChatMessageMapper;
import com.cyy.chat.model.ChatSession;
import com.cyy.chat.model.Model;
import com.cyy.chat.provider.ModelFactory;
import com.cyy.chat.service.Agent;
import com.cyy.chat.service.IChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyy.chat.service.IModelService;
import com.cyy.chat.utils.RoleTypeAdaptor;
import com.cyy.common.exception.ApplicationNoModelConfigException;
import com.cyy.common.exception.SystemGlobalException;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author CYY
 * @since 2025-03-14
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private IModelService modelService;

    @Resource
    private ModelFactory modelFactory;

    @Resource
    private JdbcMemoryRepository jdbcMemoryRepository;

    @Override
    public Flux<String> chat(Application application, ChatSession chatSession, ChatMessage userMessage) {
        // 保存当前信息
        StreamingChatModel chatModel = buildStreamingChatModel(application.getModelId());

        ChatMemory chatMemory = PersistentMessageWindowChatMemory
                .builder()
                .id(chatSession.getId())
                .maxMessages(4)
                .chatMemoryStore(jdbcMemoryRepository)
                .build();

        Agent agent = AiServices.builder(Agent.class)
                .streamingChatModel(chatModel)
                .chatMemory(chatMemory)
                .build();

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        TokenStream tokenStream = agent.chat(userMessage.getMessageText());
        tokenStream.onPartialResponse(sink::tryEmitNext)
            .onCompleteResponse(aiMessageResponse -> sink.tryEmitComplete())
            .onError(sink::tryEmitError)
            .start();

        return sink.asFlux();
    }

    @Override
    public Flux<String> tempChat(ApplicationDto application, ChatSession chatSession, ChatMessage userMessage, List<ChatMessage> chatHistories) {

        // 获取应用对应的模型信息
        StreamingChatModel chatModel = buildStreamingChatModel(application.getModelId());
        // 封装记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(4)
                .id(chatSession.getId())
                .build();

        if(chatHistories != null && !chatHistories.isEmpty()){
            chatHistories.sort(Comparator.comparing(ChatMessage::getMessageIndex));
            toLangChainMessageList(chatHistories).forEach(chatMemory::add);
        }

        Agent agent = AiServices.builder(Agent.class)
                .streamingChatModel(chatModel)
                .chatMemory(chatMemory)
                .build();

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        agent.chat(userMessage.getMessageText())
                .onPartialResponse(sink::tryEmitNext)
                .onCompleteResponse(aiMessageResponse -> sink.tryEmitComplete())
                .onError(sink::tryEmitError)
                .start();

        return sink.asFlux();
    }

    /**
     * 根据模型 id 创建模型
     * @param modelId 模型 id
     */
    private StreamingChatModel buildStreamingChatModel(Long modelId){
        if(modelId == null){
            throw new ApplicationNoModelConfigException();
        }
        Model model = modelService.getById(modelId);
        if(model == null) {
            throw new ApplicationNoModelConfigException();
        }
        return modelFactory
                .getProvider(model.getProvider())
                .getStreamingChatModel(model.getApiUrl(), model.getApiKey(), model.getModelName());
    }


    private dev.langchain4j.data.message.ChatMessage toLangChainMessage(ChatMessage message){
        if(message == null){
            throw new SystemGlobalException("消息为空");
        }
        ChatMessageType type = RoleTypeAdaptor.getMsgType(message.getRole());
        switch (type) {
            case USER:
                return new UserMessage(message.getMessageText());
            case AI:
                return new AiMessage(message.getMessageText());
            case SYSTEM:
                return new SystemMessage(message.getMessageText());
            // 添加其他消息类型的映射，例如 TOOL_EXECUTION_RESULT, TOOL_EXECUTION_REQUEST 等
            default:
                throw new IllegalArgumentException("Unsupported chat message role: " + message.getRole());
        }
    }

    /**
     * 将 ChatMessage 转为 LangChain4j 的 Message
     * @param messages 消息列表
     * @return SpringAi 的 Message 列表
     */
    private List<dev.langchain4j.data.message.ChatMessage> toLangChainMessageList(List<ChatMessage> messages){
        messages = messages == null ? new ArrayList<>() : messages;
        return messages.stream().map(this::toLangChainMessage).collect(Collectors.toList());
    }

}

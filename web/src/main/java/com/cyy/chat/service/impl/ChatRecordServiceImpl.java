package com.cyy.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyy.chat.advisor.DBMemory;
import com.cyy.chat.controller.dto.ApplicationDto;
import com.cyy.chat.dao.ChatMessageMapper;
import com.cyy.chat.model.*;
import com.cyy.chat.dao.ChatRecordMapper;
import com.cyy.chat.provider.ModelFactory;
import com.cyy.chat.service.IChatRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyy.chat.service.IModelService;
import com.cyy.common.exception.ApplicationNoModelConfigException;
import com.cyy.common.exception.SystemGlobalException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 对话记录表 服务实现类
 * </p>
 *
 * @author CYY
 * @since 2025-03-10
 */
@Service
public class ChatRecordServiceImpl extends ServiceImpl<ChatRecordMapper, ChatRecord> implements IChatRecordService {

    @Resource
    private IModelService modelService;

    @Resource
    private ModelFactory modelFactory;
    @Resource
    private ChatMessageMapper chatMessageMapper;
    @Resource
    private DBMemory dbMemory;

    @Override
    public Flux<String> chat(Application application, ChatSession chatSession, ChatMessage userMessage) {
        // 保存当前信息
        ChatMessage lastMessage = chatMessageMapper.getLastMessage(chatSession.getId());
        ChatModel chatModel = buildChatModel(application.getModelId());

        ChatClient chatClient = buildChatClient(dbMemory, chatModel);

        return chatClient
                .prompt(userMessage.getMessageText())
                .advisors(advisorSpec -> {
                    advisorSpec.param("chat_memory_conversation_id", chatSession.getId());
                    advisorSpec.param("chat_memory_response_size", 4);
                })
                .stream()
                .content();
    }

    @Override
    public Flux<String> tempChat(ApplicationDto application, ChatSession chatSession, ChatMessage userMessage, List<ChatMessage> chatHistories) {

        // 获取应用对应的模型信息
        ChatModel chatModel = buildChatModel(application.getModelId());

        // 封装记忆
        InMemoryChatMemory memory = new InMemoryChatMemory();
        if(chatHistories != null && !chatHistories.isEmpty()){
            chatHistories.sort(Comparator.comparing(ChatMessage::getMessageIndex));
            List<Message> messages = chatMessageListToSpringAiMessageList(chatHistories);
            memory.add(String.valueOf(chatSession.getId()),messages);
        }

        // 构建客户端
        ChatClient chatClient = buildChatClient(memory, chatModel);

        // 执行聊天
        return chatClient
                .prompt(userMessage.getMessageText())
                .stream()
                .content();
    }

    public List<Message> buildMessageList(Application application, ChatSession chatSession, ChatRecord chatRecord, Model model) {

        // 获取应用的模型配置
        ModelSetting modelSetting = this.getModelSetting(application, model);
        // 获取历史聊天信息
        LambdaQueryWrapper<ChatRecord> queryWrapper = new LambdaQueryWrapper<ChatRecord>()
                .eq(ChatRecord::getChatId, chatSession.getId());
        List<ChatRecord> chatRecords = this.list(queryWrapper);
        chatRecords = chatRecords.stream()
                .sorted((Comparator.comparing(ChatRecord::getIndex)))
                .collect(Collectors.toList());
        // 封装用户消息
        List<Message> messageList = this.getUserMessageList(modelSetting, chatRecord, chatRecords);
        // 获取系统提示词
        SystemMessage systemMessage = this.getSystemMessage(modelSetting);
        // todo 用 LinkedList ?
        messageList.add(0,systemMessage);
        return messageList;
    }

    @Override
    public ModelSetting getModelSetting(Application application, Model model) {

        modelConfig modelParamsSettingFromModel = null;
        ModelSetting modelSettingFromApplication = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            if (model.getModelConfig() != null){
                modelParamsSettingFromModel = objectMapper.readValue(model.getModelConfig(), modelConfig.class);
            }
            if(application.getModelSetting() != null) {
                modelSettingFromApplication = objectMapper.readValue(application.getModelSetting(), ModelSetting.class);
            }
        } catch (JsonProcessingException e) {
            throw new SystemGlobalException("模型配置解析失败");
        }
        return this.mergeModelSetting(modelSettingFromApplication, modelParamsSettingFromModel);
    }

    @Override
    public ModelSetting mergeModelSetting(ModelSetting modelSettingFromApplication, modelConfig modelParamsSettingFromModel) {
        if(modelSettingFromApplication.getModelConfig().getMaxTokens() == null){
            modelSettingFromApplication.getModelConfig().setMaxTokens(modelParamsSettingFromModel.getMaxTokens());
        }
        if(modelSettingFromApplication.getModelConfig().getTemperature() == null){
            modelSettingFromApplication.getModelConfig().setTemperature(modelParamsSettingFromModel.getTemperature());
        }
        if(modelSettingFromApplication.getModelConfig().getTopP() == null){
            modelSettingFromApplication.getModelConfig().setTopP(modelParamsSettingFromModel.getTopP());
        }
        return modelSettingFromApplication;
    }

    @Override
    public SystemMessage getSystemMessage(ModelSetting modelSetting) {
        String defaultSystem = modelSetting.getSystem();
        return new SystemMessage(defaultSystem);
    }

    @Override
    public List<Message> getUserMessageList(ModelSetting modelSetting, ChatRecord chatRecord, List<ChatRecord> chatRecords) {
        // todo 考虑知识库引用
        // 处理默认的 {question} 并转为 userQuestion
        String noReferencesPrompt = modelSetting.getNoReferencesPrompt();
        PromptTemplate promptTemplate = new PromptTemplate(noReferencesPrompt);
        UserMessage userQuestion = (UserMessage) promptTemplate.createMessage(Map.of("question", chatRecord.getQueryText()));

        // 将历史聊天对话转为 Message
        List<Message> messageList = new java.util.ArrayList<>(chatRecords.stream()
                .sorted(Comparator.comparing(ChatRecord::getIndex))
                .limit(modelSetting.getChatMemory())
                .flatMap(record -> Stream.<Message>of(new UserMessage(record.getQueryText()), new AssistantMessage(record.getResponseText()))
                ).toList());

        // 添加用户的询问
        messageList.add(userQuestion);
        return messageList;
    }

    /**
     * 构建聊天代理
     * @param memory 聊天记忆
     * @param model 被代理的模型
     * @return 包含了模型和模型 Advisors 的聊天代理
     */
    private ChatClient buildChatClient(ChatMemory memory, ChatModel model){
        return ChatClient.builder(model)
                .defaultAdvisors(new MessageChatMemoryAdvisor(memory))
                .build();
    }

    /**
     * 根据模型 id 创建模型
     * @param modelId 模型 id
     */
    private ChatModel buildChatModel(Long modelId){
        if(modelId == null){
            throw new ApplicationNoModelConfigException();
        }
        Model model = modelService.getById(modelId);
        if(model == null) {
            throw new ApplicationNoModelConfigException();
        }
        return modelFactory
                .getProvider(model.getProvider())
                .getChatModel(model.getApiUrl(), model.getApiKey(), model.getModelName());
    }

    private Message chatMessageToSpringAiMessage(ChatMessage message){
        if(message == null){
            throw new SystemGlobalException("消息为空");
        }
        if(message.getRole() == null){
            throw new SystemGlobalException("消息角色为空");
        }
        if(MessageType.USER == MessageType.fromValue(message.getRole())){
            return new UserMessage(message.getMessageText());
        }else if(MessageType.ASSISTANT == MessageType.fromValue(message.getRole())){
            return new AssistantMessage(message.getMessageText());
        }else {
            throw new SystemGlobalException("消息类型错误");
        }
    }

    /**
     * 将 ChatMessage 转为 SpringAi 的 Message
     * @param messages 消息列表
     * @return SpringAi 的 Message 列表
     */
    private List<Message> chatMessageListToSpringAiMessageList(List<ChatMessage> messages){
        if(messages == null){
            return List.of();
        }
        return messages.stream().map(this::chatMessageToSpringAiMessage).toList();
    }
}

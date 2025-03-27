package com.cyy.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyy.chat.advisor.DBMemory;
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
import org.springframework.ai.chat.client.DefaultChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.Content;
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
        //todo 封装成工作流

        // 保存当前信息
        ChatMessage lastMessage = chatMessageMapper.getLastMessage(chatSession.getId());

        userMessage = ChatMessage
                .builder()
                .role(MessageType.USER.name())
                .sessionId(chatSession.getId())
                .messageText(userMessage.getMessageText())
                .messageIndex(lastMessage == null ? 0 : lastMessage.getMessageIndex() + 1)
                .build();

        chatMessageMapper.insert(userMessage);
        // 获取应用对应的模型信息
        Long modelId = application.getModelId();
        if(modelId == null){
            throw new ApplicationNoModelConfigException();
        }
        Model model = modelService.getById(modelId);
        if(model == null) {
            throw new ApplicationNoModelConfigException();
        }
        // 封装 MessageList
//        List<Message> messageList = this.buildMessageList(application, chatSession, chatRecord, model);
        // 获取应用的模型配置
//        ModelSetting modelSetting = this.getModelSetting(application, model);
        // 将所有信息封装成 prompt
//        Prompt prompt = new Prompt(messageList);
        // 根据模型信息创建模型
        ChatModel chatModel = modelFactory
                .getProvider(model.getProvider())
                .getChatModel(model.getApiUrl(), model.getApiKey(), model.getModelName());
        // 调用模型获取响应

        var chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(dbMemory))
                .build();

        return chatClient
                .prompt(userMessage.getMessageText())
                .advisors(advisorSpec -> {
                    advisorSpec.param("chat_memory_conversation_id", chatSession.getId());
                    advisorSpec.param("chat_memory_response_size", 4);
                })
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

        ModelParamsSetting modelParamsSettingFromModel = null;
        ModelSetting modelSettingFromApplication = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            if (model.getModelConfig() != null){
                modelParamsSettingFromModel = objectMapper.readValue(model.getModelConfig(), ModelParamsSetting.class);
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
    public ModelSetting mergeModelSetting(ModelSetting modelSettingFromApplication, ModelParamsSetting modelParamsSettingFromModel) {
        if(modelSettingFromApplication.getModelParamsSetting().getMaxTokens() == null){
            modelSettingFromApplication.getModelParamsSetting().setMaxTokens(modelParamsSettingFromModel.getMaxTokens());
        }
        if(modelSettingFromApplication.getModelParamsSetting().getTemperature() == null){
            modelSettingFromApplication.getModelParamsSetting().setTemperature(modelParamsSettingFromModel.getTemperature());
        }
        if(modelSettingFromApplication.getModelParamsSetting().getTopP() == null){
            modelSettingFromApplication.getModelParamsSetting().setTopP(modelParamsSettingFromModel.getTopP());
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

}

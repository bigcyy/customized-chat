package com.cyy.chat.service;

import com.cyy.chat.model.*;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.Content;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * <p>
 * 对话记录表 服务类
 * </p>
 *
 * @author CYY
 * @since 2025-03-10
 */
public interface IChatRecordService extends IService<ChatRecord> {

    Flux<String> chat(Application application, ChatSession chatSession, ChatMessage userMessage);
    Flux<String> tempChat(Application application, ChatSession chatSession, ChatMessage userMessage,List<ChatMessage> chatHistories);
    List<Message> buildMessageList(Application application, ChatSession chatSession, ChatRecord chatRecord, Model model);

    ModelSetting getModelSetting(Application application, Model model);

    ModelSetting mergeModelSetting(ModelSetting modelSettingFromApplication, ModelParamsSetting modelParamsSettingFromModel);

    SystemMessage getSystemMessage(ModelSetting modelSetting);

    List<Message> getUserMessageList(ModelSetting modelSetting, ChatRecord chatRecord, List<ChatRecord> chatRecords);
}

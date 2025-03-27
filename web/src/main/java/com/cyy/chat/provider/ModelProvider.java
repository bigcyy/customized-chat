package com.cyy.chat.provider;


import com.cyy.common.enums.ModelType;
import com.cyy.common.exception.ClientGlobalException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelDescription;

import java.util.List;

public interface ModelProvider {
    List<? extends ModelDescription> listSupportedModels();
    List<? extends ModelDescription> listSupportedModelByTypes(List<ModelType> type);
    String getProviderName();
    String getProviderIconPath();
    List<ModelType> listSupportedModelTypes();
    default Boolean checkModelConnect(String baseUrl, String apiKey, String modelId){
        ChatModel chatModel = this.getChatModel(baseUrl, apiKey, modelId);
        Prompt prompt = new Prompt("only say hi");
        try {
            ChatResponse response = chatModel.call(prompt);
            return response != null;
        }catch (RuntimeException err){
            throw new ClientGlobalException("无法访问模型");
        }
    }
    ChatModel getChatModel(String baseUrl, String apiKey, String modelId);
}

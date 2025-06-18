package com.cyy.chat.provider;


import com.cyy.common.enums.ModelType;
import com.cyy.common.exception.ClientGlobalException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.springframework.util.Assert;

import java.util.List;

public interface ModelProvider {
    List<String> listSupportedModels();
    String getProviderName();
    String getProviderIconPath();
    List<ModelType> listSupportedModelTypes();
    default Boolean checkModelConnect(String baseUrl, String apiKey, String modelId){
        ChatModel chatModel = this.getChatModel(baseUrl, apiKey, modelId);
        String response = chatModel.chat("only say hi");
        Assert.hasText(response,"无法访问模型");
        return true;
    }
    ChatModel getChatModel(String baseUrl, String apiKey, String modelId);

    StreamingChatModel getStreamingChatModel(String baseUrl, String apiKey, String modelId);
}

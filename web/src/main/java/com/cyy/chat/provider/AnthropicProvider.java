package com.cyy.chat.provider;

import com.cyy.common.enums.ModelType;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModelName;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class AnthropicProvider implements ModelProvider{
    @Override
    public List<String> listSupportedModels() {
        return Arrays.stream(AnthropicChatModelName.values())
                .map(AnthropicChatModelName::toString)
                .toList();
    }

    @Override
    public String getProviderName() {
        return "Anthropic";
    }

    @Override
    public String getProviderIconPath() {
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" shape-rendering=\"geometricPrecision\" text-rendering=\"geometricPrecision\" image-rendering=\"optimizeQuality\" fill-rule=\"evenodd\" clip-rule=\"evenodd\" viewBox=\"0 0 512 512\"><rect fill=\"#CC9B7A\" width=\"512\" height=\"512\" rx=\"104.187\" ry=\"105.042\"/><path fill=\"#1F1F1E\" fill-rule=\"nonzero\" d=\"M318.663 149.787h-43.368l78.952 212.423 43.368.004-78.952-212.427zm-125.326 0l-78.952 212.427h44.255l15.932-44.608 82.846-.004 16.107 44.612h44.255l-79.126-212.427h-45.317zm-4.251 128.341l26.91-74.701 27.083 74.701h-53.993z\"/></svg>";
    }

    @Override
    public List<ModelType> listSupportedModelTypes() {
        return List.of(ModelType.LLM);
    }

    @Override
    public ChatModel getChatModel(String baseUrl, String apiKey, String modelId) {
        return AnthropicChatModel
                .builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelId)
                .build();

    }

    @Override
    public StreamingChatModel getStreamingChatModel(String baseUrl, String apiKey, String modelId) {
        return AnthropicStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelId)
                .build();
    }
}

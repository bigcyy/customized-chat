package com.cyy.chat.provider;

import com.cyy.common.exception.ClientGlobalException;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelDescription;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class AnthropicProvider implements EnrichedModelProvider{
    @Override
    public List<? extends ModelDescription> listSupportedModels() {
        return Arrays.stream(AnthropicApi.ChatModel.values()).toList();
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
    public ChatModel getChatModel(String baseUrl, String apiKey, String modelId) {
        AnthropicApi api = new AnthropicApi(baseUrl,apiKey);
        AnthropicChatOptions options = AnthropicChatOptions
                .builder()
                .model(AnthropicApi.ChatModel.valueOf(modelId))
                .build();
        return AnthropicChatModel
                .builder()
                .defaultOptions(options)
                .anthropicApi(api)
                .build();

    }
}

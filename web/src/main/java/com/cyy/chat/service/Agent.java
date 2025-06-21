package com.cyy.chat.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface Agent {
    TokenStream chat(@MemoryId Long sessionId, @UserMessage String userMessage);
}

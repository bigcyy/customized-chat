package com.cyy.chat.service;

import dev.langchain4j.service.TokenStream;

public interface Agent {
    TokenStream chat(String userMessage);
}

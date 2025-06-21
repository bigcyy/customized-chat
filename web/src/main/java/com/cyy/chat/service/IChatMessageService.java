package com.cyy.chat.service;

import com.cyy.chat.controller.dto.ApplicationDto;
import com.cyy.chat.controller.vo.AiResponseVO;
import com.cyy.chat.model.Application;
import com.cyy.chat.model.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cyy.chat.model.ChatSession;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author CYY
 * @since 2025-03-14
 */
public interface IChatMessageService extends IService<ChatMessage> {
    Flux<AiResponseVO> chat(Application application, ChatSession chatSession, ChatMessage userMessage);
    Flux<AiResponseVO> tempChat(ApplicationDto application, ChatSession chatSession, ChatMessage userMessage, List<ChatMessage> chatHistories);
}

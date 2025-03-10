package com.cyy.chat.service.impl;

import com.cyy.chat.model.ChatSession;
import com.cyy.chat.dao.ChatSessionMapper;
import com.cyy.chat.service.IChatSessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 聊天会话表 服务实现类
 * </p>
 *
 * @author CYY
 * @since 2025-03-10
 */
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements IChatSessionService {

}

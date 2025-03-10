package com.cyy.chat.service.impl;

import com.cyy.chat.model.ChatRecord;
import com.cyy.chat.dao.ChatRecordMapper;
import com.cyy.chat.service.IChatRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}

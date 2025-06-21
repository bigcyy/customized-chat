package com.cyy.chat.controller.dto;

import com.cyy.chat.model.ToolExecutionDetail;
import com.cyy.chat.model.ToolExecutionReq;
import com.cyy.chat.model.ToolExecutionResp;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天消息数据传输对象")
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessageDto {

    private Long id;

    private Long sessionId;

    private Integer messageIndex;

    private String messageText;

    private Integer messageToken;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;

    private String role;

    @Schema(description = "工具执行请求和响应细节的 JSON 字符串")
    private ToolExecutionResp toolExecutionResp;

    @Schema(description = "对于 ai 可能会有工具执行请求列表")
    private List<ToolExecutionReq> toolExecutionReqs;

    @Schema(description = "工具执行的细节，用于前端渲染，后端需要从 Ai 消息和后续紧邻的 tool result 消息组装完整的 Ai 消息(包括 ToolExecutionDetail)")
    private List<ToolExecutionDetail> toolExecutionDetail;

}

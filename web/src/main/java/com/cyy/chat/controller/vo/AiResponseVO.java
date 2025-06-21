package com.cyy.chat.controller.vo;

import com.cyy.chat.model.ToolExecutionDetail;
import com.cyy.common.utils.SnowFlakeIdGenerator;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "封装 ai 的流式响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiResponseVO {
    @Schema(description = "这一段响应的 id")
    private Long chunkId = SnowFlakeIdGenerator.generateId();
    @Schema(description = "这一段响应所属的会话 id")
    private Long sessionId;
    @Schema(description = "这一段响应所属的消息 id")
    private Long messageId;
    @Schema(description = "响应的内容")
    private String message;
    @Schema(description = "是否流式输出结束")
    @Builder.Default
    private Boolean isEnd = false;
    @Builder.Default
    private Boolean isError = false;
    @Schema(description = "工具调用响应")
    private ToolExecutionDetail toolExecutionDetail;
}

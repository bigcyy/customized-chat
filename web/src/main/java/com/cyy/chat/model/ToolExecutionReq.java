package com.cyy.chat.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "工具调用请求")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class ToolExecutionReq{
    @Schema(description = "请求的 id")
    private String id;
    @Schema(description = "请求的 tool 名字")
    private String name;
    @Schema(description = "请求的指令")
    private String arguments;

    public static ToolExecutionReq from(ToolExecutionRequest l4jToolExecutionReq) {
        return ToolExecutionReq.builder()
                .id(l4jToolExecutionReq.id())
                .name(l4jToolExecutionReq.name())
                .arguments(l4jToolExecutionReq.arguments())
                .build();
    }
}
package com.cyy.chat.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.langchain4j.service.tool.ToolExecution;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "封装工具调用的细节，包括工具调用请求和响应的结果")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class ToolExecutionDetail {
    @Schema(description = "工具调用请求")
    private ToolExecutionReq request;
    @Schema(description = "工具调用结果")
    private String result;

    public static ToolExecutionDetail from(ToolExecution l4jToolExecution) {
       return ToolExecutionDetail.builder()
                .request(ToolExecutionReq.from(l4jToolExecution.request()))
                .result(l4jToolExecution.result())
                .build();
    }
}
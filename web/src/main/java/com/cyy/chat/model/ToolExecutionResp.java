package com.cyy.chat.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "封装工具调用的响应，包括工具调用请求和响应的结果")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class ToolExecutionResp {
    private String id;
    private String toolName;
    private String text;
}

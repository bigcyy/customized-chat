package com.cyy.chat.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "配置 MCP 的相关内容")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpSetting {

    @Schema(description = "sse 传输形式的 mcp servers")
    List<SseTransport> sseServers;
    @Schema(description = "stdio 传输形式的 mcp servers")
    List<StdioTransport> stdioServers;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "sse 格式")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SseTransport{
        @Schema(description = "传输类型, sse 或 stdio")
        String type;
        @Schema(description = "sse url")
        String sseUrl;
        @Schema(description = "发送请求时附带的请求头，可用于鉴权")
        Map<String, String> header;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "stdio 格式")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StdioTransport{
        @Schema(description = "传输类型, sse 或 stdio")
        String type;
        @Schema(description = "命令, uvx 或 npx")
        String command;
        @Schema(description = "命令的附带参数")
        List<String> args;
    }
}

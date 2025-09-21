package com.cyy.chat.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cyy.chat.config.handler.StringListJsonTypeHandler;
import com.cyy.chat.config.handler.MapJsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * MCP服务器配置表
 * </p>
 *
 * @author CYY
 * @since 2025-09-17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value = "mcp_server", autoResultMap = true)
@Schema(description = "MCP服务器配置")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpServer implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "服务器ID")
    private Long id;

    @Schema(description = "服务器名称")
    private String serverName;

    @Schema(description = "服务器描述")
    private String serverDescription;

    @Schema(description = "传输类型：stdio 或 sse")
    private String type;

    @Schema(description = "命令（stdio类型使用）")
    private String command;

    @Schema(description = "命令参数（stdio类型使用）")
    @TableField(typeHandler = StringListJsonTypeHandler.class)
    private List<String> args;

    @Schema(description = "SSE服务器URL（sse类型使用）")
    private String sseUrl;

    @Schema(description = "请求头配置（sse类型使用）")
    @TableField(typeHandler = MapJsonTypeHandler.class)
    private Map<String, String> header;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "是否删除：0-未删除，1-已删除")
    private Boolean isDeleted;
}

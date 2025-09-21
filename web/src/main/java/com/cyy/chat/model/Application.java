package com.cyy.chat.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.cyy.chat.config.handler.LongListJsonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 * <p>
 * 
 * </p>
 *
 * @author CYY
 * @since 2025-02-25
 */
@Getter
@Setter
@ToString
@Schema(name = "Application", description = "")
@TableName(autoResultMap = true)
public class Application implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    /**
     * 应用描述
     */
    @Schema(description = "应用描述")
    private String description;

    /**
     * 开场白
     */
    @Schema(description = "开场白")
    private String prologue;

    /**
     * 工作流配置
     */
    @Schema(description = "工作流配置")
    private String workflow;

    /**
     * 应用图标URL
     */
    @Schema(description = "应用图标URL")
    private String icon;

    /**
     * 应用类型
     */
    @Schema(description = "应用类型")
    private String applicationType;

    /**
     * 关联的模型ID
     */
    @Schema(description = "关联的模型ID")
    private Long modelId;

    @Schema(description = "模型设置")
    private String modelSetting;

    @Schema(description = "数据集设置")
    private String datasetSetting;

    @Schema(description = "mcp 设置")
    private String mcpSetting;

    /**
     * 关联的MCP服务器ID列表
     */
    @Schema(description = "关联的MCP服务器ID列表")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> mcpServerIds;
}

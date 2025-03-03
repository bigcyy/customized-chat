package com.cyy.chat.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
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

    /**
     * {	    \"model_params_setting\": \"覆盖模型参数\",	    \"chat_memory\": \"对话记忆配置\",	    \"prompt\": \"提示词\",	    \"system\": \"系统级配置\",	    \"no_references_prompt\": \"无参考文献时的提示\"	  }
     */
    @Schema(description = "{	    \"model_params_setting\": \"覆盖模型参数\",	    \"chat_memory\": \"对话记忆配置\",	    \"prompt\": \"提示词\",	    \"system\": \"系统级配置\",	    \"no_references_prompt\": \"无参考文献时的提示\"	  }")
    private String modelSetting;

    /**
     * {	    \"top_n\": \"返回结果数量\",	    \"similarity\": \"相似度阈值\",	    \"search_mode\": \"搜索模式\",	    \"no_references_setting\": {	      \"value\": \"无文献默认值\",	      \"status\": \"无文献状态\"	    },	    \"max_paragraph_char_number\": \"段落最大字符数\"	  }
     */
    @Schema(description = "{	    \"top_n\": \"返回结果数量\",	    \"similarity\": \"相似度阈值\",	    \"search_mode\": \"搜索模式\",	    \"no_references_setting\": {	      \"value\": \"无文献默认值\",	      \"status\": \"无文献状态\"	    },	    \"max_paragraph_char_number\": \"段落最大字符数\"	  }")
    private String datasetSetting;
}

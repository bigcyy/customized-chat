package com.cyy.chat.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
/**
 * <p>
 * 
 * </p>
 *
 * @author CYY
 * @since 2025-03-14
 */
@Getter
@Setter
@ToString
@TableName("chat_message")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ChatMessage", description = "")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Integer messageIndex;

    private String messageText;

    private Integer messageToken;

    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private String role;

    @Schema(description = "工具执行响应的 JSON 字符串")
    private String toolExecutionResp;

    @Schema(description = "对于 ai 可能会有工具执行请求列表 JSON")
    private String toolExecutionReqs;
}

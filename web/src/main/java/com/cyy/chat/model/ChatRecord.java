package com.cyy.chat.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 * <p>
 * 对话记录表
 * </p>
 *
 * @author CYY
 * @since 2025-03-10
 */
@Getter
@Setter
@ToString
@TableName("chat_record")
@Schema(name = "ChatRecord", description = "对话记录表")
public class ChatRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long chatId;

    /**
     * 记录对话在会话中的顺序，确保对话按正确顺序显示
     */
    @Schema(description = "记录对话在会话中的顺序，确保对话按正确顺序显示")
    private Integer index;

    /**
     * 用户问题
     */
    @Schema(description = "用户问题")
    private String queryText;

    /**
     * 请求消耗的token数量
     */
    @Schema(description = "请求消耗的token数量")
    private Integer queryToken;

    /**
     * 生成的回答
     */
    @Schema(description = "生成的回答")
    private String responseText;

    /**
     * 响应消耗的token数量
     */
    @Schema(description = "响应消耗的token数量")
    private Integer responseToken;

    /**
     * 最后更新时间
     */
    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

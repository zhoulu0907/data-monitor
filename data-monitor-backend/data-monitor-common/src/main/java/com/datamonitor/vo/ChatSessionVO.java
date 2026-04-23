package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天会话 VO
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天会话")
public class ChatSessionVO {

    @Schema(description = "会话ID")
    private String id;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "创建时间")
    private String createdAt;

    @Schema(description = "更新时间")
    private String updatedAt;
}

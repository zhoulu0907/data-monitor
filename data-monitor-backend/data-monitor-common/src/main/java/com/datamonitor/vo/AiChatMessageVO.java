package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天消息")
public class AiChatMessageVO {

    @Schema(description = "消息ID")
    private String id;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "时间戳")
    private String timestamp;
}

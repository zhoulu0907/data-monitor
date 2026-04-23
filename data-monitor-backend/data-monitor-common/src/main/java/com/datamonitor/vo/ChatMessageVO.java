package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息 VO
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天消息")
public class ChatMessageVO {

    @Schema(description = "消息ID")
    private Integer id;

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "组件数据")
    private String componentData;

    @Schema(description = "时间戳")
    private String timestamp;
}

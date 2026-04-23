package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 对话请求
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI对话请求")
public class ChatRequestVO {

    @Schema(description = "会话ID，首次传null")
    private String sessionId;

    @Schema(description = "用户消息")
    private String message;

    @Schema(description = "模型标识")
    private String model;
}

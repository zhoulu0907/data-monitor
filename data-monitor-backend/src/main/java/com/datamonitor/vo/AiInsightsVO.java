package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI洞察
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI洞察")
public class AiInsightsVO {

    @Schema(description = "AI简报列表")
    private List<AiBriefingVO> briefings;

    @Schema(description = "AI预警列表")
    private List<AiWarningVO> warnings;

    @Schema(description = "聊天历史记录")
    private List<AiChatMessageVO> chatHistory;
}

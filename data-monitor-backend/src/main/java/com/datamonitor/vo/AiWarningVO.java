package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI预警
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI预警")
public class AiWarningVO {

    @Schema(description = "预警ID")
    private String id;

    @Schema(description = "预警级别")
    private String level;

    @Schema(description = "预警标题")
    private String title;

    @Schema(description = "影响范围")
    private String scope;

    @Schema(description = "时间戳")
    private String timestamp;
}

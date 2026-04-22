package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 漏斗阶段
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "漏斗阶段")
public class FunnelStageVO {

    @Schema(description = "阶段名称")
    private String stage;

    @Schema(description = "阶段值")
    private Double value;

    @Schema(description = "转化率")
    private Double rate;
}

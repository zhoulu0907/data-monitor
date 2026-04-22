package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * KPI 指标项
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "KPI指标项")
public class KpiItemVO {

    @Schema(description = "指标标题")
    private String title;

    @Schema(description = "指标值")
    private Double value;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "对比标签")
    private String comparisonLabel;

    @Schema(description = "对比值")
    private String comparisonValue;

    @Schema(description = "趋势")
    private String trend;
}

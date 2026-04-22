package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源汇总
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "资源汇总")
public class ResourceSummaryVO {

    @Schema(description = "服务器数量")
    private Integer serverCount;

    @Schema(description = "算力成本")
    private Double computingCost;

    @Schema(description = "AI服务成本")
    private Double aiServiceCost;
}

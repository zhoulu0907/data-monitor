package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 资源利用率
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "资源利用率")
public class ResourceUtilizationVO {

    @Schema(description = "资源汇总")
    private ResourceSummaryVO summary;

    @Schema(description = "资源图表项列表")
    private List<ResourceChartItemVO> charts;
}

package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 市场管线
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "市场管线")
public class MarketPipelineVO {

    @Schema(description = "漏斗阶段列表")
    private List<FunnelStageVO> funnel;

    @Schema(description = "行业收入分布")
    private List<IndustryItemVO> industryShare;
}

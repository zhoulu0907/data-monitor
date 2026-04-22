package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 顶部6个KPI汇总
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "顶部6个KPI汇总")
public class KpiSummaryVO {

    @Schema(description = "年度合同额")
    private KpiItemVO annualContract;

    @Schema(description = "年度营收")
    private KpiItemVO annualRevenue;

    @Schema(description = "年度净利润")
    private KpiItemVO annualNetIncome;

    @Schema(description = "合同回款率")
    private KpiItemVO contractCollectionRate;

    @Schema(description = "人均产值")
    private KpiItemVO perCapitaOutput;

    @Schema(description = "资产负债率")
    private KpiItemVO assetLiabilityRatio;
}

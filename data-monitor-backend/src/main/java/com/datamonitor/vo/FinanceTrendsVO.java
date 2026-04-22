package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 财务趋势
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "财务趋势")
public class FinanceTrendsVO {

    @Schema(description = "月份列表")
    private List<String> months;

    @Schema(description = "营收数据")
    private List<Double> revenue;

    @Schema(description = "净利润数据")
    private List<Double> netIncome;

    @Schema(description = "成本预算")
    private List<Double> costBudget;

    @Schema(description = "实际成本")
    private List<Double> actualCost;

    @Schema(description = "人员数据")
    private PersonnelDataVO personnel;

    @Schema(description = "资源利用率")
    private ResourceUtilizationVO resource;
}

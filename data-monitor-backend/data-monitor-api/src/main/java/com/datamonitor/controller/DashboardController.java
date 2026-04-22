package com.datamonitor.controller;

import com.datamonitor.common.ApiResponse;
import com.datamonitor.service.DashboardService;
import com.datamonitor.vo.AiInsightsVO;
import com.datamonitor.vo.FinanceTrendsVO;
import com.datamonitor.vo.KpiSummaryVO;
import com.datamonitor.vo.MarketPipelineVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 大屏数据控制器
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** 获取顶部核心 KPI */
    @GetMapping("/kpi/summary")
    public ApiResponse<KpiSummaryVO> getKpiSummary() {
        return ApiResponse.success(dashboardService.getKpiSummary());
    }

    /** 获取市场管线数据 */
    @GetMapping("/market/pipeline")
    public ApiResponse<MarketPipelineVO> getMarketPipeline() {
        return ApiResponse.success(dashboardService.getMarketPipeline());
    }

    /** 获取财务趋势数据 */
    @GetMapping("/finance/trends")
    public ApiResponse<FinanceTrendsVO> getFinanceTrends() {
        return ApiResponse.success(dashboardService.getFinanceTrends());
    }

    /** 获取 AI 智能洞察 */
    @GetMapping("/ai/insights")
    public ApiResponse<AiInsightsVO> getAiInsights() {
        return ApiResponse.success(dashboardService.getAiInsights());
    }
}

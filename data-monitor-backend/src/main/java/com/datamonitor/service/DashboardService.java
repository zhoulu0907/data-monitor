package com.datamonitor.service;

import com.datamonitor.vo.AiInsightsVO;
import com.datamonitor.vo.FinanceTrendsVO;
import com.datamonitor.vo.KpiSummaryVO;
import com.datamonitor.vo.MarketPipelineVO;

/**
 * 大屏数据服务接口
 *
 * @author zhoulu
 * @since 2026-04-22
 */
public interface DashboardService {

    /** 获取顶部核心 KPI */
    KpiSummaryVO getKpiSummary();

    /** 获取市场管线数据 */
    MarketPipelineVO getMarketPipeline();

    /** 获取财务趋势数据 */
    FinanceTrendsVO getFinanceTrends();

    /** 获取 AI 智能洞察 */
    AiInsightsVO getAiInsights();
}

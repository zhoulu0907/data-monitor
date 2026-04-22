package com.datamonitor.service.impl;

import com.datamonitor.service.DashboardService;
import com.datamonitor.vo.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 大屏数据服务 Mock 实现
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Override
    public KpiSummaryVO getKpiSummary() {
        KpiSummaryVO vo = new KpiSummaryVO();
        vo.setAnnualContract(buildKpiItem("年度合同额", 12865.42, "万元", "同比", "+18.7%", "up"));
        vo.setAnnualRevenue(buildKpiItem("年度营业收入", 9742.68, "万元", "目标", "+24.4%", "up"));
        vo.setAnnualNetIncome(buildKpiItem("年度净收入", 3256.15, "万元", "同比", "+12.3%", "up"));
        vo.setContractCollectionRate(buildKpiItem("合同回款率", 87.6, "%", "同比", "+5.2%", "up"));
        vo.setPerCapitaOutput(buildKpiItem("人均产值", 61.5, "万元/人", "同比", "+8.1%", "up"));
        vo.setAssetLiabilityRatio(buildKpiItem("资产负债率", 42.3, "%", "环比", "-2.1%", "down"));
        return vo;
    }

    @Override
    public MarketPipelineVO getMarketPipeline() {
        MarketPipelineVO vo = new MarketPipelineVO();

        vo.setFunnel(Arrays.asList(
                buildFunnelStage("线索", 27010.89, 100.0),
                buildFunnelStage("商机", 18235.87, 67.5),
                buildFunnelStage("方案", 11892.34, 65.2),
                buildFunnelStage("招投标", 7536.21, 63.4),
                buildFunnelStage("签约", 4286.58, 56.9)
        ));

        vo.setIndustryShare(Arrays.asList(
                buildIndustryItem("政务", 38, "#1890ff"),
                buildIndustryItem("交通物流", 16, "#00f3ff"),
                buildIndustryItem("金融保险", 10, "#10b981"),
                buildIndustryItem("制造业", 7, "#f59e0b"),
                buildIndustryItem("能源电力", 6, "#8b5cf6"),
                buildIndustryItem("教育", 5, "#ec4899"),
                buildIndustryItem("医疗健康", 4, "#06b6d4"),
                buildIndustryItem("其他", 14, "#6b7280")
        ));

        return vo;
    }

    @Override
    public FinanceTrendsVO getFinanceTrends() {
        FinanceTrendsVO vo = new FinanceTrendsVO();

        vo.setMonths(Arrays.asList(
                "2025.04", "2025.05", "2025.06", "2025.07",
                "2025.08", "2025.09", "2025.10", "2025.11",
                "2025.12", "2026.01", "2026.02", "2026.03"
        ));

        vo.setRevenue(Arrays.asList(780.5, 812.3, 856.7, 834.2, 798.6, 845.9, 872.1, 890.4, 923.7, 785.3, 756.8, 787.5));
        vo.setNetIncome(Arrays.asList(256.2, 278.4, 289.5, 271.8, 260.3, 285.6, 298.7, 310.2, 325.4, 248.9, 235.6, 255.8));
        vo.setCostBudget(Arrays.asList(520.0, 520.0, 520.0, 520.0, 520.0, 520.0, 520.0, 520.0, 520.0, 520.0, 520.0, 520.0));
        vo.setActualCost(Arrays.asList(524.3, 533.9, 567.2, 562.4, 538.3, 560.3, 573.4, 580.2, 598.3, 536.4, 521.2, 531.7));

        // 人员数据
        PersonnelSummaryVO personnelSummary = new PersonnelSummaryVO();
        personnelSummary.setTotalHeadcount(158);
        personnelSummary.setInternalStaff(112);
        personnelSummary.setOutsourcedStaff(46);
        personnelSummary.setMonthlyInvestment(52.7);

        List<MonthlySaturationVO> saturation = Arrays.asList(
                buildMonthlySaturation("2025.04", 78, 123),
                buildMonthlySaturation("2025.05", 82, 130),
                buildMonthlySaturation("2025.06", 85, 134),
                buildMonthlySaturation("2025.07", 81, 128),
                buildMonthlySaturation("2025.08", 76, 120),
                buildMonthlySaturation("2025.09", 83, 131),
                buildMonthlySaturation("2025.10", 87, 137),
                buildMonthlySaturation("2025.11", 89, 141),
                buildMonthlySaturation("2025.12", 92, 145),
                buildMonthlySaturation("2026.01", 75, 118),
                buildMonthlySaturation("2026.02", 72, 114),
                buildMonthlySaturation("2026.03", 80, 126)
        );

        PersonnelDataVO personnelData = new PersonnelDataVO();
        personnelData.setSummary(personnelSummary);
        personnelData.setSaturation(saturation);
        vo.setPersonnel(personnelData);

        // 资源数据
        ResourceSummaryVO resourceSummary = new ResourceSummaryVO();
        resourceSummary.setServerCount(386);
        resourceSummary.setComputingCost(127.5);
        resourceSummary.setAiServiceCost(42.8);

        List<ResourceChartItemVO> resourceCharts = Arrays.asList(
                buildResourceChartItem("计算资源", 72, 85),
                buildResourceChartItem("存储资源", 68, 80),
                buildResourceChartItem("网络资源", 55, 75),
                buildResourceChartItem("GPU资源", 89, 90),
                buildResourceChartItem("内存资源", 76, 80)
        );

        ResourceUtilizationVO resourceUtilization = new ResourceUtilizationVO();
        resourceUtilization.setSummary(resourceSummary);
        resourceUtilization.setCharts(resourceCharts);
        vo.setResource(resourceUtilization);

        return vo;
    }

    @Override
    public AiInsightsVO getAiInsights() {
        AiInsightsVO vo = new AiInsightsVO();

        vo.setBriefings(Arrays.asList(
                buildBriefing("b001", "positive", "营收增长超预期",
                        "本月营收环比增长12.3%，超额完成季度目标的108%，主要贡献来自政务和金融行业。",
                        "2026-04-22 09:30:00"),
                buildBriefing("b002", "warning", "交通物流行业收入下滑",
                        "交通物流行业收入占比下降3.2%，较上季度减少约180万元，建议关注大客户续约情况。",
                        "2026-04-22 09:30:00"),
                buildBriefing("b003", "positive", "回款率持续提升",
                        "合同回款率达87.6%，同比提升5.2个百分点，回款周期缩短至平均42天。",
                        "2026-04-22 09:30:00")
        ));

        vo.setWarnings(Arrays.asList(
                buildWarning("w001", "high", "XX项目成本超支",
                        "影响季度利润约120万元", "2026-04-22 08:00:00"),
                buildWarning("w002", "medium", "外协人员占比接近预警线",
                        "当前29.1%，预警线30%，涉及交通物流和制造业项目", "2026-04-22 08:00:00"),
                buildWarning("w003", "low", "GPU资源利用率偏高",
                        "当前89%，建议在Q3前完成扩容规划", "2026-04-22 08:00:00")
        ));

        vo.setChatHistory(List.of(
                buildChatMessage("c001", "assistant",
                        "您好，我是AI智策中枢。基于当前经营数据分析，我为您整理了以下要点：\n\n1. 本月营收表现优异，环比增长12.3%\n2. 需关注交通物流行业收入下滑趋势\n3. XX项目成本超支需及时干预\n\n请问您想深入了解哪方面的数据？",
                        "2026-04-22 09:00:00")
        ));

        return vo;
    }

    // ===== 构建辅助方法 =====

    private KpiItemVO buildKpiItem(String title, double value, String unit,
                                   String comparisonLabel, String comparisonValue, String trend) {
        KpiItemVO item = new KpiItemVO();
        item.setTitle(title);
        item.setValue(value);
        item.setUnit(unit);
        item.setComparisonLabel(comparisonLabel);
        item.setComparisonValue(comparisonValue);
        item.setTrend(trend);
        return item;
    }

    private FunnelStageVO buildFunnelStage(String stage, double value, double rate) {
        FunnelStageVO item = new FunnelStageVO();
        item.setStage(stage);
        item.setValue(value);
        item.setRate(rate);
        return item;
    }

    private IndustryItemVO buildIndustryItem(String name, int value, String color) {
        IndustryItemVO item = new IndustryItemVO();
        item.setName(name);
        item.setValue(value);
        item.setColor(color);
        return item;
    }

    private MonthlySaturationVO buildMonthlySaturation(String month, int rate, int headcount) {
        MonthlySaturationVO item = new MonthlySaturationVO();
        item.setMonth(month);
        item.setRate(rate);
        item.setHeadcount(headcount);
        return item;
    }

    private ResourceChartItemVO buildResourceChartItem(String name, int current, int target) {
        ResourceChartItemVO item = new ResourceChartItemVO();
        item.setName(name);
        item.setCurrent(current);
        item.setTarget(target);
        return item;
    }

    private AiBriefingVO buildBriefing(String id, String tag, String title, String summary, String timestamp) {
        AiBriefingVO item = new AiBriefingVO();
        item.setId(id);
        item.setTag(tag);
        item.setTitle(title);
        item.setSummary(summary);
        item.setTimestamp(timestamp);
        return item;
    }

    private AiWarningVO buildWarning(String id, String level, String title, String scope, String timestamp) {
        AiWarningVO item = new AiWarningVO();
        item.setId(id);
        item.setLevel(level);
        item.setTitle(title);
        item.setScope(scope);
        item.setTimestamp(timestamp);
        return item;
    }

    private AiChatMessageVO buildChatMessage(String id, String role, String content, String timestamp) {
        AiChatMessageVO item = new AiChatMessageVO();
        item.setId(id);
        item.setRole(role);
        item.setContent(content);
        item.setTimestamp(timestamp);
        return item;
    }
}

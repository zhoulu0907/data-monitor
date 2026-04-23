package com.datamonitor.service.impl;

import com.datamonitor.entity.*;
import com.datamonitor.mapper.*;
import com.datamonitor.service.DashboardService;
import com.datamonitor.vo.*;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 大屏数据服务 - 明细表聚合实现
 * <p>
 * 所有数据从明细表（contract, revenue_detail, cost_detail 等）聚合计算，
 * 不再依赖汇总表（kpi_item, funnel_stage 等）。
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final AiBriefingMapper aiBriefingMapper;
    private final AiWarningMapper aiWarningMapper;
    private final AiChatMessageMapper aiChatMessageMapper;

    /** 当前统计年份 */
    private static final int CURRENT_YEAR = 2026;
    /** 上一年份 */
    private static final int PREV_YEAR = CURRENT_YEAR - 1;

    // ================================ KPI 汇总 ================================

    @Override
    public KpiSummaryVO getKpiSummary() {
        KpiSummaryVO vo = new KpiSummaryVO();

        // 年度合同额
        BigDecimal contractCur = querySum("SELECT COALESCE(SUM(amount),0) FROM contract WHERE EXTRACT(YEAR FROM sign_date) = " + CURRENT_YEAR);
        BigDecimal contractPrev = querySum("SELECT COALESCE(SUM(amount),0) FROM contract WHERE EXTRACT(YEAR FROM sign_date) = " + PREV_YEAR);
        vo.setAnnualContract(buildKpiItem("年度合同额", contractCur, "万元",
                "同比", calcYoY(contractCur, contractPrev)));

        // 营业收入
        BigDecimal revenueCur = querySum("SELECT COALESCE(SUM(amount),0) FROM revenue_detail WHERE month LIKE '" + CURRENT_YEAR + "%'");
        BigDecimal revenuePrev = querySum("SELECT COALESCE(SUM(amount),0) FROM revenue_detail WHERE month LIKE '" + PREV_YEAR + "%'");
        vo.setAnnualRevenue(buildKpiItem("年度营业收入", revenueCur, "万元",
                "同比", calcYoY(revenueCur, revenuePrev)));

        // 净收入
        BigDecimal costCur = querySum("SELECT COALESCE(SUM(amount),0) FROM cost_detail WHERE month LIKE '" + CURRENT_YEAR + "%'");
        BigDecimal netIncomeCur = revenueCur.subtract(costCur);
        BigDecimal costPrev = querySum("SELECT COALESCE(SUM(amount),0) FROM cost_detail WHERE month LIKE '" + PREV_YEAR + "%'");
        BigDecimal revenuePrevVal = querySum("SELECT COALESCE(SUM(amount),0) FROM revenue_detail WHERE month LIKE '" + PREV_YEAR + "%'");
        BigDecimal netIncomePrev = revenuePrevVal.subtract(costPrev);
        vo.setAnnualNetIncome(buildKpiItem("年度净收入", netIncomeCur, "万元",
                "同比", calcYoY(netIncomeCur, netIncomePrev)));

        // 回款率
        BigDecimal collectionRate = queryRate(
                "SELECT CASE WHEN SUM(plan_amount) = 0 THEN 0 ELSE SUM(COALESCE(actual_amount,0)) / SUM(plan_amount) * 100 END FROM contract_payment WHERE EXTRACT(YEAR FROM plan_date) = " + CURRENT_YEAR);
        BigDecimal collectionRatePrev = queryRate(
                "SELECT CASE WHEN SUM(plan_amount) = 0 THEN 0 ELSE SUM(COALESCE(actual_amount,0)) / SUM(plan_amount) * 100 END FROM contract_payment WHERE EXTRACT(YEAR FROM plan_date) = " + PREV_YEAR);
        BigDecimal collectionDiff = collectionRate.subtract(collectionRatePrev).setScale(1, RoundingMode.HALF_UP);
        vo.setContractCollectionRate(buildKpiItem("合同回款率", collectionRate, "%",
                "同比", formatChange(collectionDiff)));

        // 人均产值
        BigDecimal headcount = querySum("SELECT COUNT(*) FROM employee WHERE status = 'active'");
        BigDecimal perCapita = headcount.intValue() > 0
                ? revenueCur.divide(headcount, 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal revenuePrevForCapita = querySum("SELECT COALESCE(SUM(amount),0) FROM revenue_detail WHERE month LIKE '" + PREV_YEAR + "%'");
        BigDecimal headcountPrev = querySum("SELECT COUNT(*) FROM employee WHERE status = 'active' AND hire_date <= '" + PREV_YEAR + "-12-31'");
        BigDecimal perCapitaPrev = headcountPrev.intValue() > 0
                ? revenuePrevForCapita.divide(headcountPrev, 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        vo.setPerCapitaOutput(buildKpiItem("人均产值", perCapita, "万元/人",
                "同比", calcYoY(perCapita, perCapitaPrev)));

        // 资产负债率
        Row bsRow = Db.selectOneBySql("SELECT total_assets, total_liabilities FROM balance_sheet ORDER BY month DESC LIMIT 1");
        Row bsPrevRow = Db.selectOneBySql("SELECT total_assets, total_liabilities FROM balance_sheet WHERE month LIKE '" + PREV_YEAR + "%' ORDER BY month DESC LIMIT 1");
        BigDecimal ratio = BigDecimal.ZERO;
        BigDecimal ratioPrev = BigDecimal.ZERO;
        if (bsRow != null && bsRow.getBigDecimal("total_assets").compareTo(BigDecimal.ZERO) > 0) {
            ratio = bsRow.getBigDecimal("total_liabilities")
                    .divide(bsRow.getBigDecimal("total_assets"), 1, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        if (bsPrevRow != null && bsPrevRow.getBigDecimal("total_assets").compareTo(BigDecimal.ZERO) > 0) {
            ratioPrev = bsPrevRow.getBigDecimal("total_liabilities")
                    .divide(bsPrevRow.getBigDecimal("total_assets"), 1, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        BigDecimal ratioDiff = ratio.subtract(ratioPrev).setScale(1, RoundingMode.HALF_UP);
        vo.setAssetLiabilityRatio(buildKpiItem("资产负债率", ratio, "%",
                "环比", formatChange(ratioDiff)));

        return vo;
    }

    // ================================ 市场管线 ================================

    @Override
    public MarketPipelineVO getMarketPipeline() {
        MarketPipelineVO vo = new MarketPipelineVO();

        // 漏斗：从 opportunity 聚合
        String[] stages = {"线索", "商机", "方案", "招投标", "签约"};
        String[] stageKeys = {"lead", "opportunity", "proposal", "bidding", "signed"};
        BigDecimal totalPipeline = querySum("SELECT COALESCE(SUM(amount),0) FROM opportunity WHERE status != 'lost'");

        List<FunnelStageVO> funnel = new ArrayList<>();
        for (int i = 0; i < stages.length; i++) {
            BigDecimal stageTotal = querySum(
                    "SELECT COALESCE(SUM(amount),0) FROM opportunity WHERE stage = '" + stageKeys[i] + "' AND status != 'lost'");
            double rate = totalPipeline.compareTo(BigDecimal.ZERO) > 0
                    ? stageTotal.divide(totalPipeline, 1, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0;
            FunnelStageVO item = new FunnelStageVO();
            item.setStage(stages[i]);
            item.setValue(stageTotal.doubleValue());
            item.setRate(rate);
            funnel.add(item);
        }
        vo.setFunnel(funnel);

        // 行业分布：从 contract 聚合
        List<Row> industryRows = Db.selectListBySql(
                "SELECT industry, SUM(amount) as total FROM contract WHERE EXTRACT(YEAR FROM sign_date) = " + CURRENT_YEAR
                        + " GROUP BY industry ORDER BY total DESC");
        String[] colors = {"#1890ff", "#00f3ff", "#10b981", "#f59e0b", "#8b5cf6", "#ec4899", "#06b6d4", "#6b7280"};
        List<IndustryItemVO> industryShare = new ArrayList<>();
        BigDecimal totalContract = querySum("SELECT COALESCE(SUM(amount),0) FROM contract WHERE EXTRACT(YEAR FROM sign_date) = " + CURRENT_YEAR);
        for (int i = 0; i < industryRows.size(); i++) {
            Row row = industryRows.get(i);
            IndustryItemVO item = new IndustryItemVO();
            item.setName(row.getString("industry"));
            item.setValue(totalContract.compareTo(BigDecimal.ZERO) > 0
                    ? row.getBigDecimal("total")
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalContract, 0, RoundingMode.HALF_UP)
                    .intValue()
                    : 0);
            item.setColor(colors[i % colors.length]);
            industryShare.add(item);
        }
        vo.setIndustryShare(industryShare);

        return vo;
    }

    // ================================ 财务趋势 ================================

    @Override
    public FinanceTrendsVO getFinanceTrends() {
        FinanceTrendsVO vo = new FinanceTrendsVO();

        // 滚动12月数据（2025.04 ~ 2026.03）
        String startMonth = (CURRENT_YEAR - 1) + "-04";
        String endMonth = CURRENT_YEAR + "-03";

        List<Row> revenueRows = Db.selectListBySql(
                "SELECT month, SUM(amount) as total FROM revenue_detail WHERE month >= '" + startMonth
                        + "' AND month <= '" + endMonth + "' GROUP BY month ORDER BY month");
        List<Row> costRows = Db.selectListBySql(
                "SELECT month, SUM(amount) as total FROM cost_detail WHERE month >= '" + startMonth
                        + "' AND month <= '" + endMonth + "' GROUP BY month ORDER BY month");

        List<String> months = new ArrayList<>();
        List<Double> revenueList = new ArrayList<>();
        List<Double> netIncomeList = new ArrayList<>();
        List<Double> costBudgetList = new ArrayList<>();
        List<Double> actualCostList = new ArrayList<>();

        // 月度成本预算（按年均摊）
        BigDecimal annualCostBudget = BigDecimal.valueOf(520);

        Map<String, BigDecimal> costMap = new LinkedHashMap<>();
        for (Row r : costRows) {
            costMap.put(r.getString("month"), r.getBigDecimal("total"));
        }

        for (Row r : revenueRows) {
            String month = r.getString("month");
            BigDecimal rev = r.getBigDecimal("total");
            BigDecimal cost = costMap.getOrDefault(month, BigDecimal.ZERO);
            BigDecimal net = rev.subtract(cost);

            months.add(month);
            revenueList.add(rev.doubleValue());
            netIncomeList.add(net.setScale(1, RoundingMode.HALF_UP).doubleValue());
            costBudgetList.add(annualCostBudget.doubleValue());
            actualCostList.add(cost.setScale(1, RoundingMode.HALF_UP).doubleValue());
        }

        vo.setMonths(months);
        vo.setRevenue(revenueList);
        vo.setNetIncome(netIncomeList);
        vo.setCostBudget(costBudgetList);
        vo.setActualCost(actualCostList);

        // 人员数据
        BigDecimal totalHeadcount = querySum("SELECT COUNT(*) FROM employee WHERE status = 'active'");
        BigDecimal internalStaff = querySum("SELECT COUNT(*) FROM employee WHERE status = 'active' AND type = 'internal'");
        BigDecimal outsourcedStaff = querySum("SELECT COUNT(*) FROM employee WHERE status = 'active' AND type = 'outsourced'");
        BigDecimal avgMonthlyCost = querySum("SELECT COALESCE(AVG(monthly_cost),0) FROM employee WHERE status = 'active'");

        PersonnelSummaryVO personnelSummary = new PersonnelSummaryVO();
        personnelSummary.setTotalHeadcount(totalHeadcount.intValue());
        personnelSummary.setInternalStaff(internalStaff.intValue());
        personnelSummary.setOutsourcedStaff(outsourcedStaff.intValue());
        personnelSummary.setMonthlyInvestment(avgMonthlyCost.setScale(1, RoundingMode.HALF_UP).doubleValue());

        // 月度人员饱和度：从 project_assignment 聚合
        List<Row> satRows = Db.selectListBySql(
                "SELECT month, COUNT(DISTINCT employee_id) as headcount, ROUND(AVG(allocation_rate)) as rate "
                        + "FROM project_assignment WHERE month >= '" + startMonth
                        + "' AND month <= '" + endMonth + "' GROUP BY month ORDER BY month");
        List<MonthlySaturationVO> saturationList = new ArrayList<>();
        for (Row r : satRows) {
            MonthlySaturationVO sat = new MonthlySaturationVO();
            sat.setMonth(r.getString("month"));
            sat.setRate(r.getInt("rate"));
            sat.setHeadcount(r.getInt("headcount"));
            saturationList.add(sat);
        }

        PersonnelDataVO personnelData = new PersonnelDataVO();
        personnelData.setSummary(personnelSummary);
        personnelData.setSaturation(saturationList);
        vo.setPersonnel(personnelData);

        // 资源数据
        List<Row> resRows = Db.selectListBySql(
                "SELECT resource_type, utilization_pct, target_pct, SUM(cost) as total_cost "
                        + "FROM resource_usage WHERE month = '" + endMonth + "' GROUP BY resource_type, utilization_pct, target_pct");

        BigDecimal serverCount = querySum(
                "SELECT COALESCE(SUM(total_capacity),0) FROM resource_usage WHERE month = '" + endMonth + "'");
        BigDecimal computingCost = BigDecimal.ZERO;
        BigDecimal aiServiceCost = BigDecimal.ZERO;
        List<ResourceChartItemVO> charts = new ArrayList<>();

        for (Row r : resRows) {
            String type = r.getString("resource_type");
            ResourceChartItemVO chart = new ResourceChartItemVO();
            chart.setName(mapResourceType(type));
            chart.setCurrent(r.getInt("utilization_pct"));
            chart.setTarget(r.getInt("target_pct"));
            charts.add(chart);

            if ("computing".equals(type) || "storage".equals(type)) {
                computingCost = computingCost.add(r.getBigDecimal("total_cost"));
            }
            if ("gpu".equals(type)) {
                aiServiceCost = aiServiceCost.add(r.getBigDecimal("total_cost"));
            }
        }

        ResourceSummaryVO resourceSummary = new ResourceSummaryVO();
        resourceSummary.setServerCount(serverCount.intValue());
        resourceSummary.setComputingCost(computingCost.setScale(1, RoundingMode.HALF_UP).doubleValue());
        resourceSummary.setAiServiceCost(aiServiceCost.setScale(1, RoundingMode.HALF_UP).doubleValue());

        ResourceUtilizationVO resourceUtilization = new ResourceUtilizationVO();
        resourceUtilization.setSummary(resourceSummary);
        resourceUtilization.setCharts(charts);
        vo.setResource(resourceUtilization);

        return vo;
    }

    // ================================ AI 洞察（保留汇总表） ================================

    @Override
    public AiInsightsVO getAiInsights() {
        AiInsightsVO vo = new AiInsightsVO();

        List<AiBriefingEntity> briefings = aiBriefingMapper.selectAll();
        vo.setBriefings(briefings.stream().map(this::toAiBriefingVO).collect(Collectors.toList()));

        List<AiWarningEntity> warnings = aiWarningMapper.selectAll();
        vo.setWarnings(warnings.stream().map(this::toAiWarningVO).collect(Collectors.toList()));

        List<AiChatMessageEntity> chatMessages = aiChatMessageMapper.selectAll();
        vo.setChatHistory(chatMessages.stream().map(this::toAiChatMessageVO).collect(Collectors.toList()));

        return vo;
    }

    // ================================ 工具方法 ================================

    private BigDecimal querySum(String sql) {
        Row row = Db.selectOneBySql(sql);
        return extractFirstBigDecimal(row);
    }

    private BigDecimal queryRate(String sql) {
        return extractFirstBigDecimal(Db.selectOneBySql(sql));
    }

    private BigDecimal extractFirstBigDecimal(Row row) {
        if (row == null || row.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Object value = row.values().iterator().next();
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(value.toString());
    }

    private KpiItemVO buildKpiItem(String title, BigDecimal value, String unit,
                                   String comparisonLabel, String comparisonValue) {
        KpiItemVO item = new KpiItemVO();
        item.setTitle(title);
        item.setValue(value.doubleValue());
        item.setUnit(unit);
        item.setComparisonLabel(comparisonLabel);
        item.setComparisonValue(comparisonValue);
        item.setTrend(comparisonValue.startsWith("+") ? "up" : "down");
        return item;
    }

    /** 计算同比百分比 */
    private String calcYoY(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) return "+0.0%";
        BigDecimal yoy = current.subtract(previous)
                .divide(previous, 1, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return formatChange(yoy);
    }

    /** 格式化变化值 */
    private String formatChange(BigDecimal change) {
        String sign = change.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return sign + change.setScale(1, RoundingMode.HALF_UP) + "%";
    }

    /** 资源类型映射 */
    private String mapResourceType(String type) {
        return switch (type) {
            case "computing" -> "计算资源";
            case "storage" -> "存储资源";
            case "network" -> "网络资源";
            case "gpu" -> "GPU资源";
            case "memory" -> "内存资源";
            default -> type;
        };
    }

    private AiBriefingVO toAiBriefingVO(AiBriefingEntity e) {
        AiBriefingVO item = new AiBriefingVO();
        item.setId(e.getId());
        item.setTag(e.getTag());
        item.setTitle(e.getTitle());
        item.setSummary(e.getSummary());
        item.setTimestamp(e.getTimestamp());
        return item;
    }

    private AiWarningVO toAiWarningVO(AiWarningEntity e) {
        AiWarningVO item = new AiWarningVO();
        item.setId(e.getId());
        item.setLevel(e.getLevel());
        item.setTitle(e.getTitle());
        item.setScope(e.getScope());
        item.setTimestamp(e.getTimestamp());
        return item;
    }

    private AiChatMessageVO toAiChatMessageVO(AiChatMessageEntity e) {
        AiChatMessageVO item = new AiChatMessageVO();
        item.setId(e.getId());
        item.setRole(e.getRole());
        item.setContent(e.getContent());
        item.setTimestamp(e.getTimestamp());
        return item;
    }
}

package com.datamonitor.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

/**
 * Spring AI 声明式 Function Calling 定义
 * <p>
 * 将 9 个数据查询函数注册为 Spring AI 的 @Bean Function，
 * 框架自动生成 JSON Schema、处理参数反序列化和多轮调用编排。
 *
 * @author zhoulu
 * @since 2026-04-24
 */
@Slf4j
@Configuration
public class SpringAiFunctions {

    private static final int CURRENT_YEAR = 2026;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ================================ KPI 摘要 ================================

    @Bean
    @Description("查询企业核心KPI指标摘要，包括年度合同额、营业收入、成本、净收入、回款率、人均产值等")
    public Function<KpiRequest, String> queryKpiSummary() {
        return request -> {
            try {
                ObjectNode result = objectMapper.createObjectNode();

                BigDecimal contractTotal = queryBigDecimal(
                        "SELECT COALESCE(SUM(amount),0) FROM contract WHERE EXTRACT(YEAR FROM sign_date) = " + CURRENT_YEAR);
                result.put("annualContract", contractTotal.setScale(2, RoundingMode.HALF_UP));

                BigDecimal revenueTotal = queryBigDecimal(
                        "SELECT COALESCE(SUM(amount),0) FROM revenue_detail WHERE month LIKE '" + CURRENT_YEAR + "%'");
                result.put("annualRevenue", revenueTotal.setScale(2, RoundingMode.HALF_UP));

                BigDecimal costTotal = queryBigDecimal(
                        "SELECT COALESCE(SUM(amount),0) FROM cost_detail WHERE month LIKE '" + CURRENT_YEAR + "%'");
                result.put("totalCost", costTotal.setScale(2, RoundingMode.HALF_UP));

                BigDecimal netIncome = revenueTotal.subtract(costTotal);
                result.put("netIncome", netIncome.setScale(2, RoundingMode.HALF_UP));

                BigDecimal collectionRate = queryBigDecimal(
                        "SELECT CASE WHEN SUM(plan_amount) = 0 THEN 0 " +
                                "ELSE ROUND(SUM(COALESCE(actual_amount,0)) / SUM(plan_amount) * 100, 1) END " +
                                "FROM contract_payment WHERE EXTRACT(YEAR FROM plan_date) = " + CURRENT_YEAR);
                result.put("collectionRate", collectionRate);

                BigDecimal headcount = queryBigDecimal("SELECT COUNT(*) FROM employee WHERE status = 'active'");
                BigDecimal perCapita = headcount.intValue() > 0
                        ? revenueTotal.divide(headcount, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                result.put("perCapitaOutput", perCapita);
                result.put("headcount", headcount.intValue());

                return objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                log.error("查询KPI摘要失败", e);
                return "{\"error\":\"查询KPI摘要失败: " + e.getMessage() + "\"}";
            }
        };
    }

    public record KpiRequest() {
    }

    // ================================ 月度营收 ================================

    @Bean
    @Description("查询指定年份的月度营收数据，返回每月营收金额")
    public Function<YearRequest, String> queryRevenueMonthly() {
        return request -> {
            try {
                int year = request.year() != null ? request.year() : CURRENT_YEAR;

                ObjectNode result = objectMapper.createObjectNode();
                result.put("year", year);

                ArrayNode monthlyData = objectMapper.createArrayNode();
                var rows = Db.selectListBySql(
                        "SELECT month, SUM(amount) as revenue FROM revenue_detail " +
                                "WHERE month LIKE '" + year + "%' GROUP BY month ORDER BY month");

                BigDecimal total = BigDecimal.ZERO;
                for (Row row : rows) {
                    ObjectNode item = objectMapper.createObjectNode();
                    item.put("month", row.getString("month"));
                    BigDecimal revenue = row.getBigDecimal("revenue").setScale(2, RoundingMode.HALF_UP);
                    item.put("revenue", revenue);
                    monthlyData.add(item);
                    total = total.add(revenue);
                }

                result.set("monthlyData", monthlyData);
                result.put("annualTotal", total.setScale(2, RoundingMode.HALF_UP));

                return objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                log.error("查询月度营收失败", e);
                return "{\"error\":\"查询月度营收失败: " + e.getMessage() + "\"}";
            }
        };
    }

    // ================================ 行业分布 ================================

    @Bean
    @Description("查询指定年份的合同行业分布，返回各行业的合同金额")
    public Function<YearRequest, String> queryContractByIndustry() {
        return request -> {
            try {
                int year = request.year() != null ? request.year() : CURRENT_YEAR;

                ObjectNode result = objectMapper.createObjectNode();
                result.put("year", year);

                ArrayNode industryData = objectMapper.createArrayNode();
                var rows = Db.selectListBySql(
                        "SELECT industry, SUM(amount) as total FROM contract " +
                                "WHERE EXTRACT(YEAR FROM sign_date) = " + year +
                                " GROUP BY industry ORDER BY total DESC");

                BigDecimal totalAmount = BigDecimal.ZERO;
                for (Row row : rows) {
                    ObjectNode item = objectMapper.createObjectNode();
                    String industry = row.getString("industry");
                    BigDecimal total = row.getBigDecimal("total").setScale(2, RoundingMode.HALF_UP);
                    item.put("industry", industry != null ? industry : "未知");
                    item.put("contractAmount", total);
                    industryData.add(item);
                    totalAmount = totalAmount.add(total);
                }

                result.set("industryData", industryData);
                result.put("totalContractAmount", totalAmount.setScale(2, RoundingMode.HALF_UP));

                return objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                log.error("查询行业分布失败", e);
                return "{\"error\":\"查询行业分布失败: " + e.getMessage() + "\"}";
            }
        };
    }

    // ================================ 商机漏斗 ================================

    @Bean
    @Description("查询商机漏斗各阶段数据，包括线索、商机、方案、招投标、签约各阶段的金额和数量")
    public Function<EmptyRequest, String> queryFunnelStages() {
        return request -> {
            try {
                ObjectNode result = objectMapper.createObjectNode();

                String[] stages = {"线索", "商机", "方案", "招投标", "签约"};
                String[] stageKeys = {"lead", "opportunity", "proposal", "bidding", "signed"};

                BigDecimal totalPipeline = queryBigDecimal(
                        "SELECT COALESCE(SUM(amount),0) FROM opportunity WHERE status != 'lost'");
                result.put("totalPipeline", totalPipeline.setScale(2, RoundingMode.HALF_UP));

                ArrayNode funnelData = objectMapper.createArrayNode();
                for (int i = 0; i < stages.length; i++) {
                    BigDecimal stageAmount = queryBigDecimal(
                            "SELECT COALESCE(SUM(amount),0) FROM opportunity WHERE stage = '" +
                                    stageKeys[i] + "' AND status != 'lost'");
                    BigDecimal stageCount = queryBigDecimal(
                            "SELECT COUNT(*) FROM opportunity WHERE stage = '" +
                                    stageKeys[i] + "' AND status != 'lost'");

                    ObjectNode item = objectMapper.createObjectNode();
                    item.put("stage", stages[i]);
                    item.put("stageKey", stageKeys[i]);
                    item.put("amount", stageAmount.setScale(2, RoundingMode.HALF_UP));
                    item.put("count", stageCount.intValue());

                    if (totalPipeline.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal rate = stageAmount.multiply(BigDecimal.valueOf(100))
                                .divide(totalPipeline, 1, RoundingMode.HALF_UP);
                        item.put("rate", rate);
                    } else {
                        item.put("rate", BigDecimal.ZERO);
                    }

                    funnelData.add(item);
                }

                result.set("funnelData", funnelData);

                return objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                log.error("查询商机漏斗失败", e);
                return "{\"error\":\"查询商机漏斗失败: " + e.getMessage() + "\"}";
            }
        };
    }

    // ================================ 资源使用 ================================

    @Bean
    @Description("查询各类资源（计算、存储、网络、GPU、内存）的使用率和成本")
    public Function<EmptyRequest, String> queryResourceUsage() {
        return request -> {
            try {
                ObjectNode result = objectMapper.createObjectNode();

                ArrayNode resourceData = objectMapper.createArrayNode();
                var rows = Db.selectListBySql(
                        "SELECT resource_type, utilization_pct, target_pct, SUM(cost) as total_cost " +
                                "FROM resource_usage GROUP BY resource_type, utilization_pct, target_pct");

                for (Row row : rows) {
                    ObjectNode item = objectMapper.createObjectNode();
                    item.put("resourceType", row.getString("resource_type"));
                    item.put("resourceTypeName", mapResourceType(row.getString("resource_type")));
                    item.put("utilizationPct", row.getInt("utilization_pct"));
                    item.put("targetPct", row.getInt("target_pct"));
                    BigDecimal cost = row.getBigDecimal("total_cost");
                    item.put("cost", cost != null ? cost.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                    resourceData.add(item);
                }

                result.set("resourceData", resourceData);

                return objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                log.error("查询资源使用情况失败", e);
                return "{\"error\":\"查询资源使用情况失败: " + e.getMessage() + "\"}";
            }
        };
    }

    // ================================ 人员统计 ================================

    @Bean
    @Description("查询人员统计数据，包括在职员工数、内外部比例、人均月成本及月度人员饱和度趋势")
    public Function<EmptyRequest, String> queryPersonnel() {
        return request -> {
            try {
                ObjectNode result = objectMapper.createObjectNode();

                BigDecimal totalHeadcount = queryBigDecimal(
                        "SELECT COUNT(*) FROM employee WHERE status = 'active'");
                BigDecimal internalStaff = queryBigDecimal(
                        "SELECT COUNT(*) FROM employee WHERE status = 'active' AND type = 'internal'");
                BigDecimal outsourcedStaff = queryBigDecimal(
                        "SELECT COUNT(*) FROM employee WHERE status = 'active' AND type = 'outsourced'");
                BigDecimal avgMonthlyCost = queryBigDecimal(
                        "SELECT COALESCE(AVG(monthly_cost),0) FROM employee WHERE status = 'active'");

                result.put("totalHeadcount", totalHeadcount.intValue());
                result.put("internalStaff", internalStaff.intValue());
                result.put("outsourcedStaff", outsourcedStaff.intValue());
                result.put("avgMonthlyCost", avgMonthlyCost.setScale(1, RoundingMode.HALF_UP));

                ArrayNode monthlyData = objectMapper.createArrayNode();
                var rows = Db.selectListBySql(
                        "SELECT month, COUNT(DISTINCT employee_id) as headcount, ROUND(AVG(allocation_rate)) as rate " +
                                "FROM project_assignment GROUP BY month ORDER BY month");

                for (Row row : rows) {
                    ObjectNode item = objectMapper.createObjectNode();
                    item.put("month", row.getString("month"));
                    item.put("headcount", row.getInt("headcount"));
                    item.put("saturationRate", row.getInt("rate"));
                    monthlyData.add(item);
                }

                result.set("monthlyData", monthlyData);

                return objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                log.error("查询人员统计失败", e);
                return "{\"error\":\"查询人员统计失败: " + e.getMessage() + "\"}";
            }
        };
    }

    // ================================ 回款率 ================================

    @Bean
    @Description("查询指定年份的回款率数据，包括年度总回款率和月度明细")
    public Function<YearRequest, String> queryCollectionRate() {
        return request -> {
            try {
                int year = request.year() != null ? request.year() : CURRENT_YEAR;

                ObjectNode result = objectMapper.createObjectNode();
                result.put("year", year);

                BigDecimal planTotal = queryBigDecimal(
                        "SELECT COALESCE(SUM(plan_amount),0) FROM contract_payment WHERE EXTRACT(YEAR FROM plan_date) = " + year);
                BigDecimal actualTotal = queryBigDecimal(
                        "SELECT COALESCE(SUM(actual_amount),0) FROM contract_payment WHERE EXTRACT(YEAR FROM plan_date) = " + year);

                result.put("totalPlanAmount", planTotal.setScale(2, RoundingMode.HALF_UP));
                result.put("totalActualAmount", actualTotal.setScale(2, RoundingMode.HALF_UP));

                BigDecimal collectionRate = planTotal.compareTo(BigDecimal.ZERO) > 0
                        ? actualTotal.multiply(BigDecimal.valueOf(100))
                        .divide(planTotal, 1, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                result.put("collectionRate", collectionRate);

                ArrayNode monthlyData = objectMapper.createArrayNode();
                var rows = Db.selectListBySql(
                        "SELECT TO_CHAR(plan_date, 'YYYY-MM') as month, " +
                                "SUM(plan_amount) as plan_amount, SUM(COALESCE(actual_amount,0)) as actual_amount " +
                                "FROM contract_payment WHERE EXTRACT(YEAR FROM plan_date) = " + year +
                                " GROUP BY TO_CHAR(plan_date, 'YYYY-MM') ORDER BY month");

                for (Row row : rows) {
                    ObjectNode item = objectMapper.createObjectNode();
                    item.put("month", row.getString("month"));
                    BigDecimal planAmt = row.getBigDecimal("plan_amount");
                    BigDecimal actualAmt = row.getBigDecimal("actual_amount");
                    item.put("planAmount", planAmt.setScale(2, RoundingMode.HALF_UP));
                    item.put("actualAmount", actualAmt.setScale(2, RoundingMode.HALF_UP));

                    BigDecimal monthlyRate = planAmt.compareTo(BigDecimal.ZERO) > 0
                            ? actualAmt.multiply(BigDecimal.valueOf(100))
                            .divide(planAmt, 1, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    item.put("monthlyCollectionRate", monthlyRate);
                    monthlyData.add(item);
                }

                result.set("monthlyData", monthlyData);

                return objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                log.error("查询回款率失败", e);
                return "{\"error\":\"查询回款率失败: " + e.getMessage() + "\"}";
            }
        };
    }

    // ================================ 资产负债 ================================

    @Bean
    @Description("查询资产负债表数据，包括总资产、总负债、资产负债率等")
    public Function<EmptyRequest, String> queryBalanceSheet() {
        return request -> {
            try {
                ObjectNode result = objectMapper.createObjectNode();

                ArrayNode monthlyData = objectMapper.createArrayNode();
                var rows = Db.selectListBySql(
                        "SELECT month, total_assets, total_liabilities, current_assets, current_liabilities " +
                                "FROM balance_sheet ORDER BY month");

                for (Row row : rows) {
                    ObjectNode item = objectMapper.createObjectNode();
                    item.put("month", row.getString("month"));
                    BigDecimal totalAssets = row.getBigDecimal("total_assets");
                    BigDecimal totalLiabilities = row.getBigDecimal("total_liabilities");
                    item.put("totalAssets", totalAssets != null ? totalAssets.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                    item.put("totalLiabilities", totalLiabilities != null ? totalLiabilities.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

                    BigDecimal currentAssets = row.getBigDecimal("current_assets");
                    BigDecimal currentLiabilities = row.getBigDecimal("current_liabilities");
                    item.put("currentAssets", currentAssets != null ? currentAssets.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                    item.put("currentLiabilities", currentLiabilities != null ? currentLiabilities.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

                    BigDecimal ratio = BigDecimal.ZERO;
                    if (totalAssets != null && totalAssets.compareTo(BigDecimal.ZERO) > 0 && totalLiabilities != null) {
                        ratio = totalLiabilities.multiply(BigDecimal.valueOf(100))
                                .divide(totalAssets, 1, RoundingMode.HALF_UP);
                    }
                    item.put("liabilityRatio", ratio);

                    monthlyData.add(item);
                }

                result.set("monthlyData", monthlyData);

                Row latestRow = Db.selectOneBySql(
                        "SELECT month, total_assets, total_liabilities FROM balance_sheet ORDER BY month DESC LIMIT 1");
                if (latestRow != null) {
                    BigDecimal latestAssets = latestRow.getBigDecimal("total_assets");
                    BigDecimal latestLiabilities = latestRow.getBigDecimal("total_liabilities");
                    result.put("latestMonth", latestRow.getString("month"));
                    result.put("latestTotalAssets", latestAssets != null ? latestAssets.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                    result.put("latestTotalLiabilities", latestLiabilities != null ? latestLiabilities.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                    if (latestAssets != null && latestAssets.compareTo(BigDecimal.ZERO) > 0 && latestLiabilities != null) {
                        result.put("latestLiabilityRatio",
                                latestLiabilities.multiply(BigDecimal.valueOf(100))
                                        .divide(latestAssets, 1, RoundingMode.HALF_UP));
                    } else {
                        result.put("latestLiabilityRatio", BigDecimal.ZERO);
                    }
                }

                return objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                log.error("查询资产负债失败", e);
                return "{\"error\":\"查询资产负债失败: " + e.getMessage() + "\"}";
            }
        };
    }

    // ================================ 成本明细 ================================

    @Bean
    @Description("查询指定年份的成本明细数据，包括月度成本和成本分类汇总")
    public Function<YearRequest, String> queryCostDetail() {
        return request -> {
            try {
                int year = request.year() != null ? request.year() : CURRENT_YEAR;

                ObjectNode result = objectMapper.createObjectNode();
                result.put("year", year);

                // 月度成本汇总
                ArrayNode monthlyData = objectMapper.createArrayNode();
                var monthRows = Db.selectListBySql(
                        "SELECT month, SUM(amount) as cost FROM cost_detail " +
                                "WHERE month LIKE '" + year + "%' GROUP BY month ORDER BY month");

                BigDecimal totalCost = BigDecimal.ZERO;
                for (Row row : monthRows) {
                    ObjectNode item = objectMapper.createObjectNode();
                    item.put("month", row.getString("month"));
                    BigDecimal cost = row.getBigDecimal("cost").setScale(2, RoundingMode.HALF_UP);
                    item.put("cost", cost);
                    monthlyData.add(item);
                    totalCost = totalCost.add(cost);
                }

                result.set("monthlyData", monthlyData);
                result.put("totalCost", totalCost.setScale(2, RoundingMode.HALF_UP));

                // 成本分类汇总
                ArrayNode categoryData = objectMapper.createArrayNode();
                var categoryRows = Db.selectListBySql(
                        "SELECT category, SUM(amount) as cost FROM cost_detail " +
                                "WHERE month LIKE '" + year + "%' GROUP BY category ORDER BY cost DESC");

                for (Row row : categoryRows) {
                    ObjectNode item = objectMapper.createObjectNode();
                    String category = row.getString("category");
                    item.put("category", category != null ? category : "其他");
                    BigDecimal cost = row.getBigDecimal("cost").setScale(2, RoundingMode.HALF_UP);
                    item.put("cost", cost);

                    if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal ratio = cost.multiply(BigDecimal.valueOf(100))
                                .divide(totalCost, 1, RoundingMode.HALF_UP);
                        item.put("ratio", ratio);
                    } else {
                        item.put("ratio", BigDecimal.ZERO);
                    }
                    categoryData.add(item);
                }

                result.set("categoryData", categoryData);

                return objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                log.error("查询成本明细失败", e);
                return "{\"error\":\"查询成本明细失败: " + e.getMessage() + "\"}";
            }
        };
    }

    // ================================ 共用 Request record ================================

    public record YearRequest(Integer year) {
    }

    public record EmptyRequest() {
    }

    // ================================ 辅助方法 ================================

    private static BigDecimal queryBigDecimal(String sql) {
        Row row = Db.selectOneBySql(sql);
        if (row == null || row.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Object value = row.values().iterator().next();
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        }
        return new BigDecimal(value.toString());
    }

    private static String mapResourceType(String type) {
        if (type == null) {
            return "未知";
        }
        return switch (type) {
            case "computing" -> "计算资源";
            case "storage" -> "存储资源";
            case "network" -> "网络资源";
            case "gpu" -> "GPU资源";
            case "memory" -> "内存资源";
            default -> type;
        };
    }
}

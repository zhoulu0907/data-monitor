package com.datamonitor.service.llm.function;

import com.datamonitor.service.llm.DataQueryFunction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 查询 KPI 摘要（合同额、营收、成本、净收入、回款率、人均产值）
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Slf4j
@Component
public class QueryKpiSummary implements DataQueryFunction {

    private static final int CURRENT_YEAR = 2026;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "query_kpi_summary";
    }

    @Override
    public String getDescription() {
        return "查询企业核心KPI指标摘要，包括年度合同额、营业收入、成本、净收入、回款率、人均产值等";
    }

    @Override
    public String getParametersJson() {
        return "{\"type\":\"object\",\"properties\":{},\"required\":[]}";
    }

    @Override
    public String execute(Map<String, Object> params) {
        try {
            ObjectNode result = objectMapper.createObjectNode();

            // 年度合同额
            BigDecimal contractTotal = queryBigDecimal(
                    "SELECT COALESCE(SUM(amount),0) FROM contract WHERE EXTRACT(YEAR FROM sign_date) = " + CURRENT_YEAR);
            result.put("annualContract", contractTotal.setScale(2, RoundingMode.HALF_UP));

            // 营业收入
            BigDecimal revenueTotal = queryBigDecimal(
                    "SELECT COALESCE(SUM(amount),0) FROM revenue_detail WHERE month LIKE '" + CURRENT_YEAR + "%'");
            result.put("annualRevenue", revenueTotal.setScale(2, RoundingMode.HALF_UP));

            // 总成本
            BigDecimal costTotal = queryBigDecimal(
                    "SELECT COALESCE(SUM(amount),0) FROM cost_detail WHERE month LIKE '" + CURRENT_YEAR + "%'");
            result.put("totalCost", costTotal.setScale(2, RoundingMode.HALF_UP));

            // 净收入
            BigDecimal netIncome = revenueTotal.subtract(costTotal);
            result.put("netIncome", netIncome.setScale(2, RoundingMode.HALF_UP));

            // 回款率
            BigDecimal collectionRate = queryBigDecimal(
                    "SELECT CASE WHEN SUM(plan_amount) = 0 THEN 0 " +
                            "ELSE ROUND(SUM(COALESCE(actual_amount,0)) / SUM(plan_amount) * 100, 1) END " +
                            "FROM contract_payment WHERE EXTRACT(YEAR FROM plan_date) = " + CURRENT_YEAR);
            result.put("collectionRate", collectionRate);

            // 人均产值
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
    }

    private BigDecimal queryBigDecimal(String sql) {
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
}

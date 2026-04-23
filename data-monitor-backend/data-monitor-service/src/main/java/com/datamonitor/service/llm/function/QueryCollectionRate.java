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
 * 查询回款率数据
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Slf4j
@Component
public class QueryCollectionRate implements DataQueryFunction {

    private static final int CURRENT_YEAR = 2026;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "query_collection_rate";
    }

    @Override
    public String getDescription() {
        return "查询指定年份的回款率数据，包括年度总回款率和月度明细";
    }

    @Override
    public String getParametersJson() {
        return "{\"type\":\"object\",\"properties\":{\"year\":{\"type\":\"integer\",\"description\":\"查询年份，默认2026\"}},\"required\":[]}";
    }

    @Override
    public String execute(Map<String, Object> params) {
        try {
            int year = CURRENT_YEAR;
            if (params != null && params.containsKey("year") && params.get("year") != null) {
                year = ((Number) params.get("year")).intValue();
            }

            ObjectNode result = objectMapper.createObjectNode();
            result.put("year", year);

            // 年度总回款率
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

            // 月度回款明细
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

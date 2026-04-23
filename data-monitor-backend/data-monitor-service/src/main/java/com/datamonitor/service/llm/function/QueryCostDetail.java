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
 * 查询成本明细数据
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Slf4j
@Component
public class QueryCostDetail implements DataQueryFunction {

    private static final int CURRENT_YEAR = 2026;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "query_cost_detail";
    }

    @Override
    public String getDescription() {
        return "查询指定年份的成本明细数据，包括月度成本和成本分类汇总";
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

                // 占比
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
    }
}

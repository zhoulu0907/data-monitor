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
 * 查询月度营收数据
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Slf4j
@Component
public class QueryRevenueMonthly implements DataQueryFunction {

    private static final int CURRENT_YEAR = 2026;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "query_revenue_monthly";
    }

    @Override
    public String getDescription() {
        return "查询指定年份的月度营收数据，返回每月营收金额";
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

            // 月度营收
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
    }
}

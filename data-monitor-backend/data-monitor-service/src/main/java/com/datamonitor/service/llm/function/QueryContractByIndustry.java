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
 * 查询合同行业分布
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Slf4j
@Component
public class QueryContractByIndustry implements DataQueryFunction {

    private static final int CURRENT_YEAR = 2026;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "query_contract_by_industry";
    }

    @Override
    public String getDescription() {
        return "查询指定年份的合同行业分布，返回各行业的合同金额";
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

            // 行业分布查询
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
    }
}

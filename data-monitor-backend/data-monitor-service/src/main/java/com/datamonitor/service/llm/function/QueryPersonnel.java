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
 * 查询人员统计数据
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Slf4j
@Component
public class QueryPersonnel implements DataQueryFunction {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "query_personnel";
    }

    @Override
    public String getDescription() {
        return "查询人员统计数据，包括在职员工数、内外部比例、人均月成本及月度人员饱和度趋势";
    }

    @Override
    public String getParametersJson() {
        return "{\"type\":\"object\",\"properties\":{},\"required\":[]}";
    }

    @Override
    public String execute(Map<String, Object> params) {
        try {
            ObjectNode result = objectMapper.createObjectNode();

            // 人员总数及分类
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

            // 月度人员饱和度
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

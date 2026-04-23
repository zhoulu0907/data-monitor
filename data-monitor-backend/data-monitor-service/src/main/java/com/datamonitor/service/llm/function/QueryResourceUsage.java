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
 * 查询资源使用情况
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Slf4j
@Component
public class QueryResourceUsage implements DataQueryFunction {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "query_resource_usage";
    }

    @Override
    public String getDescription() {
        return "查询各类资源（计算、存储、网络、GPU、内存）的使用率和成本";
    }

    @Override
    public String getParametersJson() {
        return "{\"type\":\"object\",\"properties\":{},\"required\":[]}";
    }

    @Override
    public String execute(Map<String, Object> params) {
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
    }

    private String mapResourceType(String type) {
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

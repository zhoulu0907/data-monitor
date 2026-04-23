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
 * 查询商机漏斗各阶段数据
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Slf4j
@Component
public class QueryFunnelStages implements DataQueryFunction {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "query_funnel_stages";
    }

    @Override
    public String getDescription() {
        return "查询商机漏斗各阶段数据，包括线索、商机、方案、招投标、签约各阶段的金额和数量";
    }

    @Override
    public String getParametersJson() {
        return "{\"type\":\"object\",\"properties\":{},\"required\":[]}";
    }

    @Override
    public String execute(Map<String, Object> params) {
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

                // 转化率
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

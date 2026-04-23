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
 * 查询资产负债数据
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Slf4j
@Component
public class QueryBalanceSheet implements DataQueryFunction {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "query_balance_sheet";
    }

    @Override
    public String getDescription() {
        return "查询资产负债表数据，包括总资产、总负债、资产负债率等";
    }

    @Override
    public String getParametersJson() {
        return "{\"type\":\"object\",\"properties\":{},\"required\":[]}";
    }

    @Override
    public String execute(Map<String, Object> params) {
        try {
            ObjectNode result = objectMapper.createObjectNode();

            // 月度资产负债数据
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

                // 资产负债率
                BigDecimal ratio = BigDecimal.ZERO;
                if (totalAssets != null && totalAssets.compareTo(BigDecimal.ZERO) > 0 && totalLiabilities != null) {
                    ratio = totalLiabilities.multiply(BigDecimal.valueOf(100))
                            .divide(totalAssets, 1, RoundingMode.HALF_UP);
                }
                item.put("liabilityRatio", ratio);

                monthlyData.add(item);
            }

            result.set("monthlyData", monthlyData);

            // 最新一期数据
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
    }
}

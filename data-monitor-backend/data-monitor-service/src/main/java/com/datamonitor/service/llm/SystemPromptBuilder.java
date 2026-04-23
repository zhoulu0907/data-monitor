package com.datamonitor.service.llm;

import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 系统提示词构建器
 * <p>
 * 构建 LLM 的系统提示词，包含角色定义、A2UI 指令、当前日期和数据概览上下文
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Component
public class SystemPromptBuilder {

    /** 当前统计年份 */
    private static final int CURRENT_YEAR = 2026;

    /**
     * 构建完整的系统提示词
     *
     * @return 系统提示词文本
     */
    public String build() {
        StringBuilder sb = new StringBuilder();

        // 1. 角色定义
        sb.append("你是一个企业数据分析 AI 助手，名为「智策助手」。");
        sb.append("你可以帮助企业用户分析经营数据、生成可视化报表、提供决策建议。\n\n");

        // 2. 当前日期
        sb.append("当前日期：").append(LocalDate.now()).append("\n\n");

        // 3. 数据概览上下文
        appendDataOverview(sb);

        // 4. A2UI 指令
        sb.append("\n").append(A2uiPromptTemplate.A2UI_INSTRUCTIONS);

        return sb.toString();
    }

    /**
     * 追加数据概览上下文信息
     */
    private void appendDataOverview(StringBuilder sb) {
        sb.append("【当前数据概览】\n");

        try {
            // 年度合同额
            BigDecimal contractTotal = queryBigDecimal(
                    "SELECT COALESCE(SUM(amount),0) FROM contract WHERE EXTRACT(YEAR FROM sign_date) = " + CURRENT_YEAR);
            sb.append("- 年度合同额：").append(contractTotal.setScale(2, BigDecimal.ROUND_HALF_UP)).append(" 万元\n");

            // 营业收入
            BigDecimal revenueTotal = queryBigDecimal(
                    "SELECT COALESCE(SUM(amount),0) FROM revenue_detail WHERE month LIKE '" + CURRENT_YEAR + "%'");
            sb.append("- 营业收入：").append(revenueTotal.setScale(2, BigDecimal.ROUND_HALF_UP)).append(" 万元\n");

            // 总成本
            BigDecimal costTotal = queryBigDecimal(
                    "SELECT COALESCE(SUM(amount),0) FROM cost_detail WHERE month LIKE '" + CURRENT_YEAR + "%'");
            sb.append("- 总成本：").append(costTotal.setScale(2, BigDecimal.ROUND_HALF_UP)).append(" 万元\n");

            // 净收入
            BigDecimal netIncome = revenueTotal.subtract(costTotal);
            sb.append("- 净收入：").append(netIncome.setScale(2, BigDecimal.ROUND_HALF_UP)).append(" 万元\n");

            // 在职人数
            BigDecimal headcount = queryBigDecimal("SELECT COUNT(*) FROM employee WHERE status = 'active'");
            sb.append("- 在职员工：").append(headcount.intValue()).append(" 人\n");

            // 回款率
            BigDecimal collectionRate = queryBigDecimal(
                    "SELECT CASE WHEN SUM(plan_amount) = 0 THEN 0 " +
                            "ELSE ROUND(SUM(COALESCE(actual_amount,0)) / SUM(plan_amount) * 100, 1) END " +
                            "FROM contract_payment WHERE EXTRACT(YEAR FROM plan_date) = " + CURRENT_YEAR);
            sb.append("- 回款率：").append(collectionRate).append("%\n");
        } catch (Exception e) {
            // 数据概览查询失败时不影响主流程
            sb.append("（数据概览加载失败，请通过 function calling 查询具体数据）\n");
        }
    }

    /**
     * 执行 SQL 查询返回第一个 BigDecimal 值
     */
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

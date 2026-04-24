package com.datamonitor.service.llm;

import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Spring AI 系统提示词提供者
 * <p>
 * 构建 LLM 的系统提示词，包含角色定义、A2UI 指令、当前日期和数据概览上下文。
 * 用于 ChatClient 的 defaultSystem 配置。
 *
 * @author zhoulu
 * @since 2026-04-24
 */
public final class SpringPromptProvider {

    private static final int CURRENT_YEAR = 2026;

    private SpringPromptProvider() {
    }

    /**
     * 构建完整的系统提示词
     */
    public static String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();

        // 1. 角色定义
        sb.append("你是一个企业数据分析 AI 助手，名为「智策助手」。");
        sb.append("你可以帮助企业用户分析经营数据、生成可视化报表、提供决策建议。\n\n");

        // 2. 当前日期
        sb.append("当前日期：").append(LocalDate.now()).append("\n\n");

        // 3. 数据概览上下文
        appendDataOverview(sb);

        // 4. A2UI 指令
        sb.append("\n").append(A2UI_INSTRUCTIONS);

        return sb.toString();
    }

    /**
     * 追加数据概览上下文信息
     */
    private static void appendDataOverview(StringBuilder sb) {
        sb.append("【当前数据概览】\n");

        try {
            BigDecimal contractTotal = queryBigDecimal(
                    "SELECT COALESCE(SUM(amount),0) FROM contract WHERE EXTRACT(YEAR FROM sign_date) = " + CURRENT_YEAR);
            sb.append("- 年度合同额：").append(contractTotal.setScale(2, RoundingMode.HALF_UP)).append(" 万元\n");

            BigDecimal revenueTotal = queryBigDecimal(
                    "SELECT COALESCE(SUM(amount),0) FROM revenue_detail WHERE month LIKE '" + CURRENT_YEAR + "%'");
            sb.append("- 营业收入：").append(revenueTotal.setScale(2, RoundingMode.HALF_UP)).append(" 万元\n");

            BigDecimal costTotal = queryBigDecimal(
                    "SELECT COALESCE(SUM(amount),0) FROM cost_detail WHERE month LIKE '" + CURRENT_YEAR + "%'");
            sb.append("- 总成本：").append(costTotal.setScale(2, RoundingMode.HALF_UP)).append(" 万元\n");

            BigDecimal netIncome = revenueTotal.subtract(costTotal);
            sb.append("- 净收入：").append(netIncome.setScale(2, RoundingMode.HALF_UP)).append(" 万元\n");

            BigDecimal headcount = queryBigDecimal("SELECT COUNT(*) FROM employee WHERE status = 'active'");
            sb.append("- 在职员工：").append(headcount.intValue()).append(" 人\n");

            BigDecimal collectionRate = queryBigDecimal(
                    "SELECT CASE WHEN SUM(plan_amount) = 0 THEN 0 " +
                            "ELSE ROUND(SUM(COALESCE(actual_amount,0)) / SUM(plan_amount) * 100, 1) END " +
                            "FROM contract_payment WHERE EXTRACT(YEAR FROM plan_date) = " + CURRENT_YEAR);
            sb.append("- 回款率：").append(collectionRate).append("%\n");
        } catch (Exception e) {
            sb.append("（数据概览加载失败，请通过 function calling 查询具体数据）\n");
        }
    }

    /**
     * 执行 SQL 查询返回第一个 BigDecimal 值
     */
    private static BigDecimal queryBigDecimal(String sql) {
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

    /**
     * A2UI 组件指令说明
     */
    private static final String A2UI_INSTRUCTIONS = """
            你可以在回复中嵌入 A2UI 组件指令，格式为：
            [COMPONENT]{"componentName":"组件名","props":{...}}

            可用组件：
            - RevenueTrendChart: 营收趋势图 {"title":"标题","data":[{"month":"2026-01","revenue":100},...]}
            - BarChart: 柱状图 {"title":"标题","data":[{"name":"分类","value":100},...]}
            - FunnelChart: 漏斗图 {"title":"标题","data":[{"stage":"阶段","value":100},...]}
            - PieChart: 饼图 {"title":"标题","data":[{"name":"名称","value":100,"color":"#xxx"},...]}
            - LineChart: 折线图 {"title":"标题","data":[{"month":"月份","rate":85,"headcount":100},...]}
            - KpiCard: KPI卡片 {"title":"标题","data":[{"label":"指标","value":100,"unit":"单位"},...]}
            - Table: 表格 {"title":"标题","columns":[{"key":"xxx","label":"列名"},...],"data":[{...},...]}
            - StatCard: 统计卡片 {"title":"标题","value":100,"unit":"单位","trend":5.2}

            规则：
            1. 先用文字分析数据，再嵌入组件可视化
            2. 每次回复最多嵌入 1-2 个组件
            3. 组件数据必须来自 function calling 的查询结果，不要编造数字
            """;
}

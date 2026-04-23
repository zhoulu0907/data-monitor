package com.datamonitor.service.llm;

/**
 * A2UI 组件指令模板常量
 * <p>
 * 定义 LLM 输出中可嵌入的 A2UI 组件指令格式和可用组件列表
 *
 * @author zhoulu
 * @since 2026-04-23
 */
public final class A2uiPromptTemplate {

    private A2uiPromptTemplate() {
        // 禁止实例化
    }

    /**
     * A2UI 组件指令说明模板
     */
    public static final String A2UI_INSTRUCTIONS = """
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

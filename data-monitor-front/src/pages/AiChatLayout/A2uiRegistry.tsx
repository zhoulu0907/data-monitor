import type { ComponentType } from 'react';
import ReactECharts from 'echarts-for-react';
import echarts, { getDarkTheme } from '../../utils/echarts';

// ============================================
// A2UI 组件标题容器
// ============================================
function ChartWrapper({ title, children }: { title?: string; children: React.ReactNode }) {
  return (
    <div
      style={{
        padding: '12px 16px',
        background: 'rgba(0,243,255,0.03)',
        border: '1px solid rgba(0,243,255,0.1)',
        borderRadius: '8px',
      }}
    >
      {title && (
        <div
          style={{
            fontSize: '13px',
            color: '#00f3ff',
            marginBottom: '10px',
            fontWeight: 'bold',
          }}
        >
          {title}
        </div>
      )}
      {children}
    </div>
  );
}

// ============================================
// 营收趋势折线图
// props: { title, data: [{ month, revenue }] }
// ============================================
function RevenueTrendChart({ title, data }: Record<string, unknown>) {
  const theme = getDarkTheme();
  const chartData = (data as Array<{ month: string; revenue: number }>) || [];

  const option = {
    ...theme,
    tooltip: { ...theme.tooltip, trigger: 'axis' as const },
    grid: { left: '12%', right: '5%', top: '10%', bottom: '18%' },
    xAxis: {
      ...theme.xAxis,
      type: 'category' as const,
      data: chartData.map((d) => d.month),
      axisLabel: { ...theme.xAxis.axisLabel, rotate: 30, fontSize: 10 },
    },
    yAxis: {
      ...theme.yAxis,
      type: 'value' as const,
    },
    series: [
      {
        name: '营收',
        type: 'line' as const,
        data: chartData.map((d) => d.revenue),
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        lineStyle: { color: '#1890ff', width: 2 },
        itemStyle: { color: '#1890ff' },
        areaStyle: {
          color: {
            type: 'linear' as const,
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(24,144,255,0.25)' },
              { offset: 1, color: 'rgba(24,144,255,0.02)' },
            ],
          },
        },
      },
    ],
    animationDuration: 800,
  };

  return (
    <ChartWrapper title={title as string}>
      <ReactECharts echarts={echarts} option={option} style={{ width: '100%', height: '260px' }} notMerge />
    </ChartWrapper>
  );
}

// ============================================
// 柱状图
// props: { title, data: [{ name, value }] }
// ============================================
function BarChart({ title, data }: Record<string, unknown>) {
  const theme = getDarkTheme();
  const chartData = (data as Array<{ name: string; value: number }>) || [];

  const option = {
    ...theme,
    tooltip: { ...theme.tooltip, trigger: 'axis' as const, axisPointer: { type: 'shadow' as const } },
    grid: { left: '12%', right: '5%', top: '10%', bottom: '18%' },
    xAxis: {
      ...theme.xAxis,
      type: 'category' as const,
      data: chartData.map((d) => d.name),
      axisLabel: { ...theme.xAxis.axisLabel, rotate: 30, fontSize: 10 },
    },
    yAxis: { ...theme.yAxis, type: 'value' as const },
    series: [
      {
        type: 'bar' as const,
        barWidth: '40%',
        data: chartData.map((d) => d.value),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0,243,255,0.8)' },
            { offset: 1, color: 'rgba(0,243,255,0.25)' },
          ]),
          borderRadius: [3, 3, 0, 0],
        },
      },
    ],
    animationDuration: 800,
  };

  return (
    <ChartWrapper title={title as string}>
      <ReactECharts echarts={echarts} option={option} style={{ width: '100%', height: '260px' }} notMerge />
    </ChartWrapper>
  );
}

// ============================================
// 漏斗图
// props: { title, data: [{ stage, value }] }
// ============================================
function FunnelChart({ title, data }: Record<string, unknown>) {
  const theme = getDarkTheme();
  const chartData = (data as Array<{ stage: string; value: number }>) || [];

  const gradients = [
    { from: '#00f3ff', to: '#38bdf8' },
    { from: '#38bdf8', to: '#22d3ee' },
    { from: '#22d3ee', to: '#1890ff' },
    { from: '#1890ff', to: '#1d4ed8' },
    { from: '#1d4ed8', to: '#0a4291' },
  ];

  const option = {
    ...theme,
    tooltip: { ...theme.tooltip, trigger: 'item' as const },
    series: [
      {
        type: 'funnel' as const,
        left: '10%',
        right: '20%',
        top: '5%',
        bottom: '5%',
        minSize: '20%',
        maxSize: '100%',
        sort: 'descending' as const,
        gap: 4,
        label: {
          show: true,
          position: 'inside' as const,
          formatter: '{b}\n{c}',
          fontSize: 11,
          color: '#ffffff',
          fontWeight: 'bold' as const,
        },
        itemStyle: { borderColor: 'transparent', borderWidth: 0 },
        emphasis: {
          label: { fontSize: 13, fontWeight: 'bold' as const },
          itemStyle: { shadowBlur: 15, shadowColor: 'rgba(0,243,255,0.6)' },
        },
        data: chartData.map((item, index) => ({
          name: item.stage,
          value: item.value,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: gradients[index % gradients.length].from },
              { offset: 1, color: gradients[index % gradients.length].to },
            ]),
          },
        })),
      },
    ],
    animationDuration: 800,
  };

  return (
    <ChartWrapper title={title as string}>
      <ReactECharts echarts={echarts} option={option} style={{ width: '100%', height: '280px' }} notMerge />
    </ChartWrapper>
  );
}

// ============================================
// 饼图
// props: { title, data: [{ name, value, color }] }
// ============================================
function PieChart({ title, data }: Record<string, unknown>) {
  const theme = getDarkTheme();
  const chartData = (data as Array<{ name: string; value: number; color?: string }>) || [];

  // 默认调色板
  const defaultColors = ['#00f3ff', '#38bdf8', '#1890ff', '#8b5cf6', '#10b981', '#f59e0b', '#ef4444', '#ec4899'];

  const option = {
    ...theme,
    tooltip: { ...theme.tooltip, trigger: 'item' as const, formatter: '{b}: {c} ({d}%)' },
    legend: {
      orient: 'vertical' as const,
      right: '5%',
      top: 'middle',
      textStyle: { color: '#8798af', fontSize: 11 },
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 8,
    },
    series: [
      {
        type: 'pie' as const,
        radius: ['40%', '68%'],
        center: ['40%', '50%'],
        selectedMode: 'single' as const,
        selectedOffset: 6,
        itemStyle: { borderRadius: 2, borderColor: '#040f23', borderWidth: 2 },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 13, fontWeight: 'bold' as const, color: '#ffffff' },
          itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,243,255,0.5)' },
        },
        data: chartData.map((item, i) => ({
          name: item.name,
          value: item.value,
          itemStyle: { color: item.color || defaultColors[i % defaultColors.length] },
        })),
      },
    ],
    animationDuration: 800,
  };

  return (
    <ChartWrapper title={title as string}>
      <ReactECharts echarts={echarts} option={option} style={{ width: '100%', height: '280px' }} notMerge />
    </ChartWrapper>
  );
}

// ============================================
// 折线图（双线 - 通用）
// props: { title, data: [{ month, rate, headcount }] }
// ============================================
function LineChart({ title, data }: Record<string, unknown>) {
  const theme = getDarkTheme();
  const chartData = (data as Array<{ month: string; rate?: number; headcount?: number }>) || [];

  const option = {
    ...theme,
    tooltip: { ...theme.tooltip, trigger: 'axis' as const },
    legend: {
      textStyle: { color: '#8798af', fontSize: 10 },
      top: '2%',
      right: '5%',
    },
    grid: { left: '12%', right: '12%', top: '18%', bottom: '18%' },
    xAxis: {
      ...theme.xAxis,
      type: 'category' as const,
      data: chartData.map((d) => d.month),
      axisLabel: { ...theme.xAxis.axisLabel, rotate: 30, fontSize: 10 },
    },
    yAxis: [
      {
        type: 'value' as const,
        name: '饱和度(%)',
        nameTextStyle: { color: '#8798af', fontSize: 9 },
        axisLabel: { color: '#8798af', fontSize: 9 },
        splitLine: { lineStyle: { color: 'rgba(135,152,175,0.1)', type: 'dashed' as const } },
        axisLine: { show: false },
      },
      {
        type: 'value' as const,
        name: '人数',
        nameTextStyle: { color: '#8798af', fontSize: 9 },
        axisLabel: { color: '#8798af', fontSize: 9 },
        splitLine: { show: false },
        axisLine: { show: false },
      },
    ],
    series: [
      {
        name: '饱和度',
        type: 'line' as const,
        yAxisIndex: 0,
        data: chartData.map((d) => d.rate),
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        lineStyle: { color: '#10b981', width: 2 },
        itemStyle: { color: '#10b981' },
      },
      {
        name: '人数',
        type: 'line' as const,
        yAxisIndex: 1,
        data: chartData.map((d) => d.headcount),
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        lineStyle: { color: '#f59e0b', width: 2 },
        itemStyle: { color: '#f59e0b' },
      },
    ].filter((s) => s.data.some((v: unknown) => v != null)),
    animationDuration: 800,
  };

  return (
    <ChartWrapper title={title as string}>
      <ReactECharts echarts={echarts} option={option} style={{ width: '100%', height: '260px' }} notMerge />
    </ChartWrapper>
  );
}

// ============================================
// KPI 卡片组
// props: { title, data: [{ label, value, unit }] }
// ============================================
function KpiCard({ title, data }: Record<string, unknown>) {
  const cardData = (data as Array<{ label: string; value: number; unit: string }>) || [];

  return (
    <ChartWrapper title={title as string}>
      <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
        {cardData.map((item) => (
          <div
            key={item.label}
            style={{
              flex: '1 1 120px',
              padding: '10px 14px',
              background: 'rgba(0,243,255,0.04)',
              border: '1px solid rgba(0,243,255,0.12)',
              borderRadius: '6px',
              display: 'flex',
              flexDirection: 'column',
              gap: '4px',
            }}
          >
            <span style={{ fontSize: '11px', color: '#8798af' }}>{item.label}</span>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: '3px' }}>
              <span
                className="font-number"
                style={{ fontSize: '22px', fontWeight: 'bold', color: '#00f3ff' }}
              >
                {typeof item.value === 'number' ? item.value.toLocaleString() : item.value}
              </span>
              {item.unit && (
                <span style={{ fontSize: '10px', color: '#8798af' }}>{item.unit}</span>
              )}
            </div>
          </div>
        ))}
      </div>
    </ChartWrapper>
  );
}

// ============================================
// 统计卡片（单个数值 + 趋势）
// props: { title, value, unit, trend }
// ============================================
function StatCard({ title, value, unit, trend }: Record<string, unknown>) {
  const trendNum = typeof trend === 'number' ? trend : 0;
  const isPositive = trendNum >= 0;

  return (
    <ChartWrapper title={title as string}>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px' }}>
        <span
          className="font-number"
          style={{ fontSize: '32px', fontWeight: 'bold', color: '#00f3ff' }}
        >
          {typeof value === 'number' ? value.toLocaleString() : String(value)}
        </span>
        {unit && <span style={{ fontSize: '12px', color: '#8798af' }}>{String(unit)}</span>}
        {trendNum !== 0 && (
          <span
            style={{
              fontSize: '13px',
              color: isPositive ? '#10b981' : '#ef4444',
              fontWeight: 'bold',
            }}
          >
            {isPositive ? '↑' : '↓'} {Math.abs(trendNum)}%
          </span>
        )}
      </div>
    </ChartWrapper>
  );
}

// ============================================
// 数据表格
// props: { title, columns: [{ key, label }], data: [...] }
// ============================================
function Table({ title, columns, data }: Record<string, unknown>) {
  const cols = (columns as Array<{ key: string; label: string }>) || [];
  const rows = (data as Array<Record<string, unknown>>) || [];

  return (
    <ChartWrapper title={title as string}>
      <div style={{ overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '12px' }}>
          <thead>
            <tr>
              {cols.map((col) => (
                <th
                  key={col.key}
                  style={{
                    padding: '8px 12px',
                    textAlign: 'left',
                    color: '#00f3ff',
                    borderBottom: '1px solid rgba(0,243,255,0.15)',
                    fontWeight: 'bold',
                    whiteSpace: 'nowrap',
                  }}
                >
                  {col.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((row, i) => (
              <tr
                key={i}
                style={{
                  borderBottom: '1px solid rgba(135,152,175,0.08)',
                }}
              >
                {cols.map((col) => (
                  <td
                    key={col.key}
                    style={{
                      padding: '7px 12px',
                      color: '#e0e6f0',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    {row[col.key] != null ? String(row[col.key]) : '-'}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </ChartWrapper>
  );
}

// ============================================
// 组件注册表
// ============================================
export const componentRegistry: Record<string, ComponentType<Record<string, unknown>>> = {
  RevenueTrendChart,
  BarChart,
  FunnelChart,
  PieChart,
  LineChart,
  KpiCard,
  StatCard,
  Table,
  // 兼容别名
  IndustryBreakdown: PieChart,
  GaugeChart: StatCard,
};

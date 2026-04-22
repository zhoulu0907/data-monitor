import ReactECharts from 'echarts-for-react';
import echarts, { getDarkTheme } from '../../utils/echarts';
import type { FinanceTrends } from '../../types/dashboard';

interface Props {
  data: FinanceTrends;
}

export default function DualLineChart({ data }: Props) {
  const theme = getDarkTheme();

  const option = {
    ...theme,
    tooltip: {
      ...theme.tooltip,
      trigger: 'axis' as const,
    },
    legend: {
      data: ['营收', '净收入', '成本预算', '实际成本'],
      textStyle: { color: '#8798af', fontSize: 10 },
      top: '0%',
      itemWidth: 16,
      itemHeight: 2,
    },
    grid: {
      left: '8%',
      right: '4%',
      top: '18%',
      bottom: '12%',
    },
    xAxis: {
      ...theme.xAxis,
      type: 'category' as const,
      data: data.months,
      boundaryGap: false,
    },
    yAxis: {
      ...theme.yAxis,
      type: 'value' as const,
      axisLabel: {
        ...theme.yAxis.axisLabel,
        formatter: '{value}',
      },
    },
    series: [
      {
        name: '营收',
        type: 'line' as const,
        data: data.revenue,
        smooth: true,
        symbol: 'circle',
        symbolSize: 4,
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
      {
        name: '净收入',
        type: 'line' as const,
        data: data.netIncome,
        smooth: true,
        symbol: 'circle',
        symbolSize: 4,
        lineStyle: { color: '#10b981', width: 2 },
        itemStyle: { color: '#10b981' },
        areaStyle: {
          color: {
            type: 'linear' as const,
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(16,185,129,0.2)' },
              { offset: 1, color: 'rgba(16,185,129,0.02)' },
            ],
          },
        },
      },
      {
        name: '成本预算',
        type: 'line' as const,
        data: data.costBudget,
        lineStyle: { color: '#f59e0b', width: 1.5, type: 'dashed' as const },
        itemStyle: { color: '#f59e0b' },
        symbol: 'none',
      },
      {
        name: '实际成本',
        type: 'line' as const,
        data: data.actualCost,
        smooth: true,
        symbol: 'circle',
        symbolSize: 3,
        lineStyle: { color: '#ef4444', width: 1.5 },
        itemStyle: { color: '#ef4444' },
      },
    ],
    animationDuration: 1000,
    animationEasing: 'cubicOut' as const,
  };

  return (
    <ReactECharts
      echarts={echarts}
      option={option}
      style={{ width: '100%', height: '100%' }}
      notMerge={true}
    />
  );
}

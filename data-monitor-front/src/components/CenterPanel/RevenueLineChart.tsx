import ReactECharts from 'echarts-for-react';
import echarts, { getDarkTheme } from '../../utils/echarts';
import type { FinanceTrends } from '../../types/dashboard';

interface Props {
  data: FinanceTrends;
}

export default function RevenueLineChart({ data }: Props) {
  const theme = getDarkTheme();

  const option = {
    ...theme,
    tooltip: { ...theme.tooltip, trigger: 'axis' as const },
    legend: {
      data: ['营收', '净收入'],
      textStyle: { color: '#8798af', fontSize: 10 },
      top: '2%',
      right: '5%',
      itemWidth: 14,
      itemHeight: 2,
    },
    grid: { left: '12%', right: '5%', top: '18%', bottom: '18%' },
    xAxis: {
      ...theme.xAxis,
      type: 'category' as const,
      data: data.months,
      axisLabel: { ...theme.xAxis.axisLabel, rotate: 30, fontSize: 9 },
    },
    yAxis: {
      ...theme.yAxis,
      type: 'value' as const,
    },
    series: [
      {
        name: '营收',
        type: 'line' as const,
        data: data.revenue,
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        lineStyle: { color: '#1890ff', width: 2 },
        itemStyle: { color: '#1890ff' },
        areaStyle: {
          color: { type: 'linear' as const, x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(24,144,255,0.2)' },
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
        symbolSize: 5,
        lineStyle: { color: '#10b981', width: 2 },
        itemStyle: { color: '#10b981' },
        areaStyle: {
          color: { type: 'linear' as const, x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(16,185,129,0.15)' },
              { offset: 1, color: 'rgba(16,185,129,0.02)' },
            ],
          },
        },
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

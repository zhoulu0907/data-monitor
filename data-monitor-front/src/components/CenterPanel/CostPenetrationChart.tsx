import ReactECharts from 'echarts-for-react';
import echarts, { getDarkTheme } from '../../utils/echarts';
import type { FinanceTrends } from '../../types/dashboard';

interface Props {
  data: FinanceTrends;
}

export default function CostPenetrationChart({ data }: Props) {
  const theme = getDarkTheme();

  const option = {
    ...theme,
    tooltip: { ...theme.tooltip, trigger: 'axis' as const },
    legend: {
      data: ['实际成本'],
      textStyle: { color: '#8798af', fontSize: 10 },
      top: '2%',
      right: '5%',
      itemWidth: 14,
      itemHeight: 2,
    },
    grid: { left: '12%', right: '8%', top: '18%', bottom: '18%' },
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
        name: '实际成本',
        type: 'line' as const,
        data: data.actualCost,
        smooth: true,
        symbol: 'circle',
        symbolSize: 4,
        lineStyle: { color: '#8b5cf6', width: 2 },
        itemStyle: { color: '#8b5cf6' },
        areaStyle: {
          color: { type: 'linear' as const, x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(139,92,246,0.25)' },
              { offset: 1, color: 'rgba(139,92,246,0.02)' },
            ],
          },
        },
        markLine: {
          symbol: ['none' as const, 'arrow' as const],
          lineStyle: { color: '#ef4444', width: 2, type: 'solid' as const },
          label: {
            formatter: '成本预警线',
            color: '#ef4444',
            fontSize: 10,
            position: 'insideEndTop' as const,
          },
          data: [{ yAxis: 3500 }],
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

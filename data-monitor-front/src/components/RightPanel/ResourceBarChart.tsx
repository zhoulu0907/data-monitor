import ReactECharts from 'echarts-for-react';
import echarts, { getDarkTheme } from '../../utils/echarts';
import type { ResourceChartItem } from '../../types/dashboard';

interface Props {
  data: ResourceChartItem[];
}

export default function ResourceBarChart({ data }: Props) {
  const theme = getDarkTheme();

  // 计算利用率趋势线（当前/目标的比值）
  const utilizationRate = data.map((d) => Math.round((d.current / d.target) * 100));

  const option = {
    ...theme,
    tooltip: { ...theme.tooltip, trigger: 'axis' as const, axisPointer: { type: 'shadow' as const } },
    legend: {
      data: ['当前利用率', '目标利用率', '达成率'],
      textStyle: { color: '#8798af', fontSize: 9 },
      top: '0%',
      itemWidth: 12,
      itemHeight: 8,
    },
    grid: { left: '10%', right: '10%', top: '18%', bottom: '18%' },
    xAxis: {
      ...theme.xAxis,
      type: 'category' as const,
      data: data.map((d) => d.name),
      axisLabel: { ...theme.xAxis.axisLabel, rotate: 30, fontSize: 9 },
    },
    yAxis: [
      {
        type: 'value' as const,
        name: '利用率(%)',
        nameTextStyle: { color: '#8798af', fontSize: 9 },
        axisLabel: { color: '#8798af', fontSize: 9, formatter: '{value}%' },
        splitLine: { lineStyle: { color: 'rgba(135,152,175,0.1)', type: 'dashed' as const } },
        axisLine: { show: false },
      },
      {
        type: 'value' as const,
        name: '达成率(%)',
        nameTextStyle: { color: '#8798af', fontSize: 9 },
        axisLabel: { color: '#8798af', fontSize: 9, formatter: '{value}%' },
        splitLine: { show: false },
        axisLine: { show: false },
      },
    ],
    series: [
      {
        name: '当前利用率',
        type: 'bar' as const,
        barWidth: '22%',
        yAxisIndex: 0,
        data: data.map((d) => d.current),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0,243,255,0.8)' },
            { offset: 1, color: 'rgba(0,243,255,0.3)' },
          ]),
          borderRadius: [2, 2, 0, 0],
        },
      },
      {
        name: '目标利用率',
        type: 'bar' as const,
        barWidth: '22%',
        yAxisIndex: 0,
        data: data.map((d) => d.target),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255,170,0,0.8)' },
            { offset: 1, color: 'rgba(255,170,0,0.3)' },
          ]),
          borderRadius: [2, 2, 0, 0],
        },
      },
      {
        name: '达成率',
        type: 'line' as const,
        yAxisIndex: 1,
        data: utilizationRate,
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        lineStyle: { color: '#ffffff', width: 2 },
        itemStyle: { color: '#ffffff' },
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

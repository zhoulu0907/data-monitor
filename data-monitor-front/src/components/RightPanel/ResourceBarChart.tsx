import ReactECharts from 'echarts-for-react';
import echarts, { getDarkTheme } from '../../utils/echarts';
import type { ResourceChartItem } from '../../types/dashboard';

interface Props {
  data: ResourceChartItem[];
}

export default function ResourceBarChart({ data }: Props) {
  const theme = getDarkTheme();

  const option = {
    ...theme,
    tooltip: {
      ...theme.tooltip,
      trigger: 'axis' as const,
      axisPointer: { type: 'shadow' as const },
    },
    legend: {
      data: ['当前利用率', '目标利用率'],
      textStyle: { color: '#8798af', fontSize: 10 },
      top: '0%',
      itemWidth: 12,
      itemHeight: 8,
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
      data: data.map((d) => d.name),
    },
    yAxis: {
      ...theme.yAxis,
      type: 'value' as const,
      max: 100,
      axisLabel: {
        ...theme.yAxis.axisLabel,
        formatter: '{value}%',
      },
    },
    series: [
      {
        name: '当前利用率',
        type: 'bar' as const,
        barWidth: '28%',
        data: data.map((d) => d.current),
        itemStyle: {
          color: {
            type: 'linear' as const,
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: '#00f3ff' },
              { offset: 1, color: '#1890ff' },
            ],
          },
          borderRadius: [2, 2, 0, 0],
        },
      },
      {
        name: '目标利用率',
        type: 'bar' as const,
        barWidth: '28%',
        data: data.map((d) => d.target),
        itemStyle: {
          color: {
            type: 'linear' as const,
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(24,144,255,0.5)' },
              { offset: 1, color: 'rgba(24,144,255,0.2)' },
            ],
          },
          borderRadius: [2, 2, 0, 0],
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

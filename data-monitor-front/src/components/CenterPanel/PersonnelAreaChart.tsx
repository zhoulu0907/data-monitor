import ReactECharts from 'echarts-for-react';
import echarts, { getDarkTheme } from '../../utils/echarts';
import type { MonthlySaturation } from '../../types/dashboard';

interface Props {
  data: MonthlySaturation[];
}

export default function PersonnelAreaChart({ data }: Props) {
  const theme = getDarkTheme();

  const option = {
    ...theme,
    tooltip: {
      ...theme.tooltip,
      trigger: 'axis' as const,
      formatter: (params: { name: string; value: number }[]) => {
        const item = params[0];
        const sat = data.find(d => d.month === item.name);
        return `${item.name}<br/>饱和度：${item.value}%<br/>投入人数：${sat?.headcount ?? '-'} 人`;
      },
    },
    grid: {
      left: '8%',
      right: '4%',
      top: '12%',
      bottom: '15%',
    },
    xAxis: {
      ...theme.xAxis,
      type: 'category' as const,
      data: data.map((d) => d.month),
      boundaryGap: false,
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
        type: 'line' as const,
        data: data.map((d) => d.rate),
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        lineStyle: { color: '#10b981', width: 2 },
        itemStyle: { color: '#10b981' },
        areaStyle: {
          color: {
            type: 'linear' as const,
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(16,185,129,0.35)' },
              { offset: 1, color: 'rgba(16,185,129,0.02)' },
            ],
          },
        },
        markLine: {
          silent: true,
          lineStyle: { color: '#f59e0b', type: 'dashed' as const },
          data: [{ yAxis: 80, name: '目标线' }],
          label: { color: '#f59e0b', fontSize: 10 },
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

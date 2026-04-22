import ReactECharts from 'echarts-for-react';
import echarts, { getDarkTheme } from '../../utils/echarts';
import type { IndustryItem } from '../../types/dashboard';

interface Props {
  data: IndustryItem[];
}

export default function IndustryPieChart({ data }: Props) {
  const theme = getDarkTheme();

  const option = {
    ...theme,
    tooltip: {
      ...theme.tooltip,
      trigger: 'item' as const,
      formatter: '{b}: {c}% ({d}%)',
    },
    legend: [
      {
        data: data.slice(0, 4).map((item) => item.name),
        orient: 'vertical' as const,
        left: '2%',
        top: 'middle',
        textStyle: { color: '#8798af', fontSize: 11 },
        itemWidth: 10,
        itemHeight: 10,
        itemGap: 8,
      },
      {
        data: data.slice(4).map((item) => item.name),
        orient: 'vertical' as const,
        right: '2%',
        top: 'middle',
        textStyle: { color: '#8798af', fontSize: 11 },
        itemWidth: 10,
        itemHeight: 10,
        itemGap: 8,
      },
    ],
    graphic: [
      {
        type: 'text' as const,
        left: 'center',
        top: '38%',
        style: {
          text: '行业收入',
          textAlign: 'center' as const,
          fill: '#8798af',
          fontSize: 12,
        },
      },
      {
        type: 'text' as const,
        left: 'center',
        top: '52%',
        style: {
          text: '占比分布',
          textAlign: 'center' as const,
          fill: '#8798af',
          fontSize: 10,
        },
      },
    ],
    series: [
      {
        type: 'pie' as const,
        radius: ['50%', '72%'],
        center: ['50%', '50%'],
        selectedMode: 'single' as const,
        selectedOffset: 8,
        itemStyle: {
          borderRadius: 2,
          borderColor: '#040f23',
          borderWidth: 2,
        },
        label: { show: false },
        emphasis: {
          label: {
            show: true,
            fontSize: 13,
            fontWeight: 'bold' as const,
            color: '#ffffff',
          },
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0,243,255,0.5)',
          },
        },
        data: data.map((item) => ({
          name: item.name,
          value: item.value,
          itemStyle: { color: item.color },
        })),
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

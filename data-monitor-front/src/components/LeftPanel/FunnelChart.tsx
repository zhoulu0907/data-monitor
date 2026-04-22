import ReactECharts from 'echarts-for-react';
import echarts, { getDarkTheme } from '../../utils/echarts';
import type { FunnelStage } from '../../types/dashboard';

interface Props {
  data: FunnelStage[];
}

export default function FunnelChart({ data }: Props) {
  const theme = getDarkTheme();

  // 每层渐变色：从亮青到深蓝
  const gradients = [
    { from: '#00f3ff', to: '#38bdf8' },
    { from: '#38bdf8', to: '#22d3ee' },
    { from: '#22d3ee', to: '#1890ff' },
    { from: '#1890ff', to: '#1d4ed8' },
    { from: '#1d4ed8', to: '#0a4291' },
  ];

  const option = {
    ...theme,
    tooltip: {
      ...theme.tooltip,
      trigger: 'item' as const,
      formatter: (params: { name: string; value: number }) => {
        return `${params.name}<br/>金额：${params.value.toLocaleString()} 万元<br/>转化率：${data.find(d => d.stage === params.name)?.rate ?? '-'}%`;
      },
    },
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
        gap: 6,
        label: {
          show: true,
          position: 'inside' as const,
          formatter: '{b}\n{c} 万元',
          fontSize: 12,
          color: '#ffffff',
          fontWeight: 'bold' as const,
        },
        labelLine: { show: false },
        itemStyle: {
          borderColor: 'transparent',
          borderWidth: 0,
        },
        emphasis: {
          label: { fontSize: 14, fontWeight: 'bold' as const },
          itemStyle: {
            shadowBlur: 15,
            shadowColor: 'rgba(0,243,255,0.6)',
          },
        },
        data: data.map((item, index) => ({
          name: item.stage,
          value: item.value,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: gradients[index].from },
              { offset: 1, color: gradients[index].to },
            ]),
          },
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

import ReactECharts from 'echarts-for-react';
import echarts, { getDarkTheme } from '../../utils/echarts';
import type { FunnelStage } from '../../types/dashboard';

interface Props {
  data: FunnelStage[];
}

export default function FunnelChart({ data }: Props) {
  const theme = getDarkTheme();
  const colors = ['#67e8f9', '#38bdf8', '#22d3ee', '#1890ff', '#1d4ed8'];

  const option = {
    ...theme,
    tooltip: {
      ...theme.tooltip,
      trigger: 'item' as const,
      formatter: (params: { name: string; value: number; percent: number }) => {
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
        gap: 4,
        label: {
          show: true,
          position: 'inside' as const,
          formatter: '{b}\n{c} 万元',
          fontSize: 11,
          color: '#ffffff',
        },
        labelLine: {
          show: true,
          lineStyle: { color: 'rgba(0,243,255,0.5)' },
        },
        itemStyle: {
          borderColor: 'rgba(0,243,255,0.2)',
          borderWidth: 1,
        },
        emphasis: {
          label: { fontSize: 13, fontWeight: 'bold' as const },
          itemStyle: {
            borderColor: '#00f3ff',
            borderWidth: 2,
            shadowBlur: 10,
            shadowColor: 'rgba(0,243,255,0.5)',
          },
        },
        data: data.map((item, index) => ({
          name: item.stage,
          value: item.value,
          itemStyle: { color: colors[index] },
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

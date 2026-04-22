import * as echarts from 'echarts/core';
import { BarChart, LineChart, PieChart, FunnelChart } from 'echarts/charts';
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  GraphicComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([
  BarChart,
  LineChart,
  PieChart,
  FunnelChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  GraphicComponent,
  CanvasRenderer,
]);

export default echarts;

/** 暗黑主题基础配置 */
export function getDarkTheme() {
  return {
    textStyle: {
      color: '#8798af',
      fontFamily: '-apple-system, BlinkMacSystemFont, sans-serif',
    },
    tooltip: {
      backgroundColor: 'rgba(13, 31, 66, 0.95)',
      borderColor: 'rgba(0, 243, 255, 0.3)',
      borderWidth: 1,
      textStyle: { color: '#ffffff', fontSize: 12 },
    },
    grid: {
      left: '10%',
      right: '5%',
      top: '15%',
      bottom: '15%',
    },
    xAxis: {
      axisLine: { lineStyle: { color: 'rgba(135, 152, 175, 0.3)' } },
      axisLabel: { color: '#8798af', fontSize: 10 },
      splitLine: { show: false },
    },
    yAxis: {
      axisLine: { show: false },
      axisLabel: { color: '#8798af', fontSize: 10 },
      splitLine: {
        lineStyle: { color: 'rgba(135, 152, 175, 0.1)', type: 'dashed' },
      },
    },
  };
}

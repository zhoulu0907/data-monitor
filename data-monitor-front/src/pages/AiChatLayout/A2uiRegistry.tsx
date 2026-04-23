import type { ComponentType } from 'react';

// 占位组件：先用简单的卡片渲染组件名和 props，后续替换为真实 ECharts 组件
function PlaceholderComponent({ name, ...props }: Record<string, unknown>) {
  return (
    <div
      style={{
        padding: '12px',
        background: 'rgba(0,243,255,0.05)',
        border: '1px solid rgba(0,243,255,0.15)',
        borderRadius: '8px',
      }}
    >
      <div
        style={{
          fontSize: '13px',
          color: '#00f3ff',
          marginBottom: '8px',
        }}
      >
        {'📊 '}{String(name || '组件')}
      </div>
      <pre
        style={{
          fontSize: '11px',
          color: '#8798af',
          margin: 0,
          whiteSpace: 'pre-wrap',
        }}
      >
        {JSON.stringify(props, null, 2)}
      </pre>
    </div>
  );
}

// 组件注册表 - 所有组件名映射到占位组件
export const componentRegistry: Record<string, ComponentType<Record<string, unknown>>> = {
  RevenueTrendChart: PlaceholderComponent,
  IndustryBreakdown: PlaceholderComponent,
  FunnelChart: PlaceholderComponent,
  KpiCard: PlaceholderComponent,
  BarChart: PlaceholderComponent,
  LineChart: PlaceholderComponent,
  PieChart: PlaceholderComponent,
  GaugeChart: PlaceholderComponent,
  Table: PlaceholderComponent,
  StatCard: PlaceholderComponent,
};

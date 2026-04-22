import { useDashboardStore } from '../../store/useDashboardStore';
import { useCountUp, formatNumber } from '../../hooks/useCountUp';
import type { KpiItem, Trend } from '../../types/dashboard';

/** KPI 卡片图标映射 */
const iconMap: Record<string, string> = {
  '年度合同额': '📋',
  '年度营业收入': '💰',
  '年度净收入': '📈',
  '合同回款率': '🔄',
  '人均产值': '👤',
  '资产负债率': '📊',
};

function KpiCard({ item, index }: { item: KpiItem; index: number }) {
  const animatedValue = useCountUp(item.value, 1500, item.unit === '%' ? 1 : 2);
  const displayValue = formatNumber(animatedValue, item.unit === '%' ? 1 : 2);
  const trendColor = item.trend === 'up' ? 'var(--color-positive)' : item.trend === 'down' ? 'var(--color-danger)' : 'var(--color-text-secondary)';

  return (
    <div
      className={`panel-border corner-cut-sm animate-fade-in-up delay-${index + 1}`}
      style={{
        flex: '1',
        padding: '10px 14px',
        display: 'flex',
        flexDirection: 'column',
        gap: '4px',
        cursor: 'default',
        transition: 'all 0.2s',
      }}
    >
      {/* 标题行 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
        <span style={{ fontSize: '14px' }}>{iconMap[item.title] || '📌'}</span>
        <span style={{ fontSize: '12px', color: 'var(--color-text-secondary)' }}>
          {item.title}
        </span>
      </div>

      {/* 主数值 */}
      <div style={{ display: 'flex', alignItems: 'baseline', gap: '4px' }}>
        <span
          className="font-number glow-text"
          style={{ fontSize: '24px', fontWeight: 'bold' }}
        >
          {displayValue}
        </span>
        <span style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>
          {item.unit}
        </span>
      </div>

      {/* 对比值 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
        <span style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>
          {item.comparisonLabel}
        </span>
        <span style={{ fontSize: '12px', color: trendColor, fontWeight: 'bold' }}>
          {item.trend === 'up' ? '↑' : item.trend === 'down' ? '↓' : '→'} {item.comparisonValue}
        </span>
      </div>
    </div>
  );
}

export default function KpiCards() {
  const kpiSummary = useDashboardStore((s) => s.kpiSummary);

  if (!kpiSummary) return null;

  const items: KpiItem[] = [
    kpiSummary.annualContract,
    kpiSummary.annualRevenue,
    kpiSummary.annualNetIncome,
    kpiSummary.contractCollectionRate,
    kpiSummary.perCapitaOutput,
    kpiSummary.assetLiabilityRatio,
  ];

  return (
    <div style={{ display: 'flex', gap: '12px' }}>
      {items.map((item, index) => (
        <KpiCard key={item.title} item={item} index={index} />
      ))}
    </div>
  );
}

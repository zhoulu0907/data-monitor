import { useDashboardStore } from '../../store/useDashboardStore';
import { useCountUp, formatNumber } from '../../hooks/useCountUp';
import LoadingSkeleton from '../LoadingSkeleton';
import type { KpiItem } from '../../types/dashboard';

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
      className={`panel-cyber-sm animate-fade-in-up delay-${index + 1}`}
      style={{
        flex: '1',
        padding: '8px 12px',
        display: 'flex',
        flexDirection: 'column',
        gap: '3px',
        cursor: 'default',
      }}
    >
      {/* 标题行 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
        <span style={{ fontSize: '13px' }}>{iconMap[item.title] || '📌'}</span>
        <span style={{ fontSize: '11px', color: 'var(--color-text-secondary)' }}>
          {item.title}
        </span>
      </div>

      {/* 主数值 - 荧光效果 */}
      <div style={{ display: 'flex', alignItems: 'baseline', gap: '3px' }}>
        <span
          className="font-number"
          style={{ fontSize: '26px', fontWeight: 'bold', color: 'var(--color-glow)' }}
        >
          {displayValue}
        </span>
        <span style={{ fontSize: '10px', color: 'var(--color-text-secondary)' }}>
          {item.unit}
        </span>
      </div>

      {/* 对比值 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
        <span style={{ fontSize: '10px', color: 'var(--color-text-secondary)' }}>
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

  if (!kpiSummary) {
    return (
      <div style={{ display: 'flex', gap: '10px', height: '60px' }}>
        <LoadingSkeleton />
      </div>
    );
  }

  const items: KpiItem[] = [
    kpiSummary.annualContract,
    kpiSummary.annualRevenue,
    kpiSummary.annualNetIncome,
    kpiSummary.contractCollectionRate,
    kpiSummary.perCapitaOutput,
    kpiSummary.assetLiabilityRatio,
  ];

  return (
    <div style={{ display: 'flex', gap: '10px' }}>
      {items.map((item, index) => (
        <KpiCard key={item.title} item={item} index={index} />
      ))}
    </div>
  );
}

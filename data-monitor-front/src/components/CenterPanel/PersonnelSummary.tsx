import type { PersonnelSummary as PersonnelSummaryType } from '../../types/dashboard';

interface Props {
  data: PersonnelSummaryType;
}

const items = [
  { key: 'totalHeadcount' as const, label: '总人数', unit: '人', icon: '👥' },
  { key: 'internalStaff' as const, label: '自有人员', unit: '人', icon: '👤' },
  { key: 'outsourcedStaff' as const, label: '外协人员', unit: '人', icon: '🤝' },
  { key: 'monthlyInvestment' as const, label: '本月投入', unit: '人/月', icon: '📅' },
];

export default function PersonnelSummary({ data }: Props) {
  return (
    <div style={{ display: 'flex', gap: '12px', marginBottom: '8px' }}>
      {items.map((item) => (
        <div
          key={item.key}
          style={{
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            padding: '6px 10px',
            background: 'rgba(0, 243, 255, 0.03)',
            border: '1px solid rgba(0, 243, 255, 0.1)',
            borderRadius: '2px',
          }}
        >
          <span style={{ fontSize: '16px' }}>{item.icon}</span>
          <div>
            <div style={{ fontSize: '10px', color: 'var(--color-text-secondary)' }}>
              {item.label}
            </div>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: '2px' }}>
              <span
                className="font-number glow-text"
                style={{ fontSize: '18px', fontWeight: 'bold' }}
              >
                {item.key === 'monthlyInvestment'
                  ? data[item.key].toFixed(1)
                  : data[item.key]}
              </span>
              <span style={{ fontSize: '9px', color: 'var(--color-text-secondary)' }}>
                {item.unit}
              </span>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

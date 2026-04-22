import type { ResourceSummary as ResourceSummaryType } from '../../types/dashboard';

interface Props {
  data: ResourceSummaryType;
}

export default function ResourceSummary({ data }: Props) {
  const items = [
    { label: '服务器投入', value: data.serverCount, unit: '台', icon: '🖥' },
    { label: '算力费用', value: data.computingCost, unit: '万/月', icon: '⚡' },
    { label: 'AI服务费用', value: data.aiServiceCost, unit: '万/月', icon: '🧠' },
  ];

  return (
    <div style={{ display: 'flex', gap: '8px', marginBottom: '8px' }}>
      {items.map((item) => (
        <div
          key={item.label}
          style={{
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            padding: '6px 8px',
            background: 'rgba(0, 243, 255, 0.03)',
            border: '1px solid rgba(0, 243, 255, 0.1)',
            borderRadius: '2px',
          }}
        >
          <span style={{ fontSize: '14px' }}>{item.icon}</span>
          <div>
            <div style={{ fontSize: '9px', color: 'var(--color-text-secondary)' }}>
              {item.label}
            </div>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: '2px' }}>
              <span
                className="font-number glow-text"
                style={{ fontSize: '15px', fontWeight: 'bold' }}
              >
                {typeof item.value === 'number' && item.value % 1 !== 0
                  ? item.value.toFixed(1)
                  : item.value}
              </span>
              <span style={{ fontSize: '8px', color: 'var(--color-text-secondary)' }}>
                {item.unit}
              </span>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

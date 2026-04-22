import { useDashboardStore } from '../../store/useDashboardStore';
import type { AiBriefing, AiWarning, BriefingTag, WarningLevel } from '../../types/dashboard';

const tagConfig: Record<BriefingTag, { label: string; color: string; icon: string }> = {
  positive: { label: '正面', color: '#10b981', icon: '✅' },
  neutral: { label: '中性', color: '#1890ff', icon: 'ℹ️' },
  warning: { label: '预警', color: '#f59e0b', icon: '⚠️' },
};

const levelConfig: Record<WarningLevel, { label: string; color: string; icon: string }> = {
  high: { label: '高风险', color: '#ef4444', icon: '🔴' },
  medium: { label: '中风险', color: '#f59e0b', icon: '🟡' },
  low: { label: '低风险', color: '#10b981', icon: '🟢' },
};

function BriefingItem({ item }: { item: AiBriefing }) {
  const config = tagConfig[item.tag];
  return (
    <div
      style={{
        display: 'flex',
        gap: '8px',
        padding: '8px 10px',
        borderLeft: `2px solid ${config.color}`,
        background: 'rgba(0,0,0,0.15)',
        marginBottom: '6px',
        borderRadius: '0 2px 2px 0',
      }}
    >
      <span style={{ fontSize: '12px', flexShrink: 0 }}>{config.icon}</span>
      <div style={{ minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '2px' }}>
          <span style={{ fontSize: '12px', color: '#fff', fontWeight: 500 }}>{item.title}</span>
          <span
            style={{
              fontSize: '9px',
              padding: '1px 4px',
              borderRadius: '2px',
              background: `${config.color}22`,
              color: config.color,
              border: `1px solid ${config.color}44`,
            }}
          >
            {config.label}
          </span>
        </div>
        <div
          style={{
            fontSize: '10px',
            color: 'var(--color-text-secondary)',
            lineHeight: '1.4',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
          }}
        >
          {item.summary}
        </div>
      </div>
    </div>
  );
}

function WarningItem({ item }: { item: AiWarning }) {
  const config = levelConfig[item.level];
  return (
    <div
      className={item.level === 'high' ? 'animate-pulse-danger' : undefined}
      style={{
        display: 'flex',
        gap: '8px',
        padding: '8px 10px',
        borderLeft: `2px solid ${config.color}`,
        background: 'rgba(0,0,0,0.15)',
        marginBottom: '6px',
        borderRadius: '0 2px 2px 0',
      }}
    >
      <span style={{ fontSize: '10px', flexShrink: 0, lineHeight: '16px' }}>{config.icon}</span>
      <div style={{ minWidth: 0 }}>
        <div style={{ fontSize: '12px', color: '#fff', fontWeight: 500, marginBottom: '2px' }}>
          {item.title}
        </div>
        <div style={{ fontSize: '10px', color: config.color, lineHeight: '1.4' }}>
          {item.scope}
        </div>
      </div>
    </div>
  );
}

export default function AiOperations() {
  const aiInsights = useDashboardStore((s) => s.aiInsights);

  if (!aiInsights) return null;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', overflow: 'hidden' }}>
      {/* AI 经营简报 */}
      <div style={{ flex: '0 0 42%', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <div className="panel-title">
          <span style={{ marginRight: '4px' }}>📊</span> AI 经营简报
        </div>
        <div style={{ flex: 1, overflowY: 'auto', padding: '4px 8px' }}>
          {aiInsights.briefings.map((item) => (
            <BriefingItem key={item.id} item={item} />
          ))}
        </div>
      </div>

      {/* 分隔线 */}
      <div className="divider-glow" style={{ margin: '4px 8px' }} />

      {/* AI 风险预警 */}
      <div style={{ flex: '1', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <div className="panel-title">
          <span style={{ marginRight: '4px' }}>🚨</span> AI 风险预警
        </div>
        <div style={{ flex: 1, overflowY: 'auto', padding: '4px 8px' }}>
          {aiInsights.warnings.map((item) => (
            <WarningItem key={item.id} item={item} />
          ))}
        </div>
      </div>
    </div>
  );
}

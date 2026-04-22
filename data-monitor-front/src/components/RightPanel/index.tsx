import { useDashboardStore } from '../../store/useDashboardStore';
import ResourceSummary from './ResourceSummary';
import ResourceBarChart from './ResourceBarChart';
import AiOperations from './AiOperations';

export default function RightPanel() {
  const financeTrends = useDashboardStore((s) => s.financeTrends);

  return (
    <div
      className="panel-border corner-cut"
      style={{ height: '100%', display: 'flex', flexDirection: 'column' }}
    >
      {/* 资源利用区域 */}
      <div style={{ flex: '0 0 42%', display: 'flex', flexDirection: 'column' }}>
        <div className="panel-title">资源投入情况</div>
        <div style={{ padding: '8px 12px' }}>
          {financeTrends ? (
            <ResourceSummary data={financeTrends.resource.summary} />
          ) : null}
        </div>
        <div style={{ flex: 1, padding: '0 8px 8px' }}>
          {financeTrends ? (
            <ResourceBarChart data={financeTrends.resource.charts} />
          ) : (
            <div style={{ color: 'var(--color-text-secondary)', textAlign: 'center', padding: '20px' }}>
              加载中...
            </div>
          )}
        </div>
      </div>

      {/* 分隔线 */}
      <div className="divider-glow" style={{ margin: '0 12px' }} />

      {/* AI 运营面板 */}
      <div style={{ flex: '1', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <AiOperations />
      </div>
    </div>
  );
}

import { useDashboardStore } from '../../store/useDashboardStore';
import FunnelChart from './FunnelChart';
import IndustryPieChart from './IndustryPieChart';

export default function LeftPanel() {
  const marketPipeline = useDashboardStore((s) => s.marketPipeline);

  return (
    <div
      className="panel-border corner-cut"
      style={{ height: '100%', display: 'flex', flexDirection: 'column' }}
    >
      {/* 漏斗图区域 */}
      <div style={{ flex: '0 0 48%', display: 'flex', flexDirection: 'column' }}>
        <div className="panel-title">商机转化漏斗</div>
        <div style={{ flex: 1, padding: '4px 8px' }}>
          {marketPipeline ? (
            <FunnelChart data={marketPipeline.funnel} />
          ) : (
            <div style={{ color: 'var(--color-text-secondary)', textAlign: 'center', padding: '30px' }}>
              加载中...
            </div>
          )}
        </div>
      </div>

      {/* 分隔线 */}
      <div className="divider-glow" style={{ margin: '0 12px' }} />

      {/* 环形图区域 */}
      <div style={{ flex: '1', display: 'flex', flexDirection: 'column' }}>
        <div className="panel-title">行业收入占比</div>
        <div style={{ flex: 1, padding: '4px 8px' }}>
          {marketPipeline ? (
            <IndustryPieChart data={marketPipeline.industryShare} />
          ) : (
            <div style={{ color: 'var(--color-text-secondary)', textAlign: 'center', padding: '30px' }}>
              加载中...
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

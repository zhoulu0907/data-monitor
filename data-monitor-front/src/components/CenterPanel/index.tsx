import { useDashboardStore } from '../../store/useDashboardStore';
import RevenueLineChart from './RevenueLineChart';
import CostPenetrationChart from './CostPenetrationChart';
import PersonnelSummary from './PersonnelSummary';
import PersonnelAreaChart from './PersonnelAreaChart';

export default function CenterPanel() {
  const financeTrends = useDashboardStore((s) => s.financeTrends);

  return (
    <div
      className="panel-border"
      style={{ height: '100%', display: 'flex', flexDirection: 'column' }}
    >
      {/* 中上方：左右分栏 */}
      <div style={{ flex: '0 0 58%', display: 'flex', gap: '0', minHeight: 0 }}>
        {/* 左侧：收入曲线 */}
        <div style={{ flex: '1', display: 'flex', flexDirection: 'column' }}>
          <div className="panel-title">收入曲线</div>
          <div style={{ flex: 1, padding: '4px 6px', minHeight: 0 }}>
            {financeTrends ? (
              <RevenueLineChart data={financeTrends} />
            ) : (
              <div style={{ color: 'var(--color-text-secondary)', textAlign: 'center', padding: '30px' }}>
                加载中...
              </div>
            )}
          </div>
        </div>

        {/* 竖向分隔线 */}
        <div className="divider-glow-vertical" style={{ margin: '12px 0' }} />

        {/* 右侧：成本穿透 */}
        <div style={{ flex: '1', display: 'flex', flexDirection: 'column' }}>
          <div className="panel-title">成本穿透</div>
          <div style={{ flex: 1, padding: '4px 6px', minHeight: 0 }}>
            {financeTrends ? (
              <CostPenetrationChart data={financeTrends} />
            ) : (
              <div style={{ color: 'var(--color-text-secondary)', textAlign: 'center', padding: '30px' }}>
                加载中...
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 横向分隔线 */}
      <div className="divider-glow" style={{ margin: '0 12px' }} />

      {/* 中下方：人员投入面板 */}
      <div style={{ flex: '1', display: 'flex', flexDirection: 'column', padding: '6px 12px', minHeight: 0 }}>
        <div className="panel-title">人员数量与投入</div>
        {financeTrends ? (
          <>
            <PersonnelSummary data={financeTrends.personnel.summary} />
            <div style={{ flex: 1, minHeight: 0 }}>
              <PersonnelAreaChart data={financeTrends.personnel.saturation} />
            </div>
          </>
        ) : (
          <div style={{ color: 'var(--color-text-secondary)', textAlign: 'center', padding: '30px' }}>
            加载中...
          </div>
        )}
      </div>
    </div>
  );
}

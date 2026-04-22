import { useDashboardStore } from '../../store/useDashboardStore';
import DualLineChart from './DualLineChart';
import PersonnelSummary from './PersonnelSummary';
import PersonnelAreaChart from './PersonnelAreaChart';

export default function CenterPanel() {
  const financeTrends = useDashboardStore((s) => s.financeTrends);

  return (
    <div
      className="panel-border corner-cut"
      style={{ height: '100%', display: 'flex', flexDirection: 'column' }}
    >
      {/* 收入成本双折线图 */}
      <div style={{ flex: '0 0 55%', display: 'flex', flexDirection: 'column' }}>
        <div className="panel-title">收入与成本趋势</div>
        <div style={{ flex: 1, padding: '4px 8px' }}>
          {financeTrends ? (
            <DualLineChart data={financeTrends} />
          ) : (
            <div style={{ color: 'var(--color-text-secondary)', textAlign: 'center', padding: '30px' }}>
              加载中...
            </div>
          )}
        </div>
      </div>

      {/* 分隔线 */}
      <div className="divider-glow" style={{ margin: '0 12px' }} />

      {/* 人员投入面板 */}
      <div style={{ flex: '1', display: 'flex', flexDirection: 'column', padding: '8px 12px' }}>
        <div className="panel-title">人员数量与投入</div>
        {financeTrends ? (
          <>
            <PersonnelSummary data={financeTrends.personnel.summary} />
            <div style={{ flex: 1 }}>
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

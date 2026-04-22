import { useEffect } from 'react';
import { useScreenAdapter } from '../../hooks/useScreenAdapter';
import { useDashboardStore } from '../../store/useDashboardStore';
import Header from '../../components/Header';
import KpiCards from '../../components/KpiCards';
import LeftPanel from '../../components/LeftPanel';
import CenterPanel from '../../components/CenterPanel';
import RightPanel from '../../components/RightPanel';
import AiAssistantDrawer from '../../components/AiAssistantDrawer';
import '../../styles/global.css';
import '../../styles/glow-effects.css';
import '../../styles/animations.css';

export default function DashboardLayout() {
  const containerRef = useScreenAdapter();
  const fetchAllData = useDashboardStore((s) => s.fetchAllData);

  useEffect(() => {
    fetchAllData();
  }, [fetchAllData]);

  return (
    <div
      style={{
        width: '100vw',
        height: '100vh',
        backgroundColor: 'var(--bg-primary)',
        overflow: 'hidden',
      }}
    >
      <div
        ref={containerRef}
        style={{
          width: '1920px',
          height: '1080px',
          position: 'relative',
        }}
      >
        {/* 背景渐变 */}
        <div
          style={{
            position: 'absolute',
            inset: 0,
            background:
              'radial-gradient(ellipse at 50% 0%, rgba(0,243,255,0.05) 0%, transparent 60%)',
            pointerEvents: 'none',
          }}
        />

        {/* 顶部标题栏 */}
        <Header />

        {/* KPI 指标卡片 */}
        <div style={{ padding: '8px 16px' }}>
          <KpiCards />
        </div>

        {/* 三栏主区域 */}
        <div
          style={{
            display: 'flex',
            gap: '12px',
            padding: '0 16px',
            height: 'calc(1080px - 140px - 90px)',
          }}
        >
          {/* 左侧面板 */}
          <div style={{ flex: '0 0 25%' }}>
            <LeftPanel />
          </div>

          {/* 中间面板 */}
          <div style={{ flex: '0 0 45%' }}>
            <CenterPanel />
          </div>

          {/* 右侧面板 */}
          <div style={{ flex: '1' }}>
            <RightPanel />
          </div>
        </div>

        {/* AI 悬浮按钮 */}
        <AiAssistantDrawer />
      </div>
    </div>
  );
}

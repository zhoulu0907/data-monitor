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
        overflow: 'hidden',
      }}
    >
      <div
        ref={containerRef}
        style={{
          width: '1920px',
          height: '1080px',
          position: 'relative',
          display: 'flex',
          flexDirection: 'column',
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

        {/* 顶部标题栏 - 60px */}
        <Header />

        {/* KPI 指标卡片 - 约 80px */}
        <div style={{ padding: '6px 16px', flexShrink: 0 }}>
          <KpiCards />
        </div>

        {/* 三栏主区域 - 填满剩余空间 */}
        <div
          style={{
            flex: 1,
            display: 'flex',
            gap: '12px',
            padding: '6px 16px 16px',
            minHeight: 0,
          }}
        >
          {/* 左侧面板 - 约 25% */}
          <div style={{ flex: '0 0 25%' }}>
            <LeftPanel />
          </div>

          {/* 中间面板 - 约 45% */}
          <div style={{ flex: '0 0 45%' }}>
            <CenterPanel />
          </div>

          {/* 右侧面板 - 约 30% */}
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

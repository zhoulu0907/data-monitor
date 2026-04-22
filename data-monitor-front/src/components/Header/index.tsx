import type { NavItem } from '../../types/dashboard';

const leftNavItems: NavItem[] = [
  { key: 'country', label: '国家行业', icon: '🌐' },
  { key: 'data', label: '数据资源', icon: '📊' },
  { key: 'ops', label: '运营中心', icon: '⚙' },
];

const rightNavItems: NavItem[] = [
  { key: 'ai', label: '人工智能', icon: '🤖' },
  { key: 'settings', label: '系统设置', icon: '🔧' },
  { key: 'fullscreen', label: '全屏', icon: '⛶' },
];

function NavButton({ item, onClick }: { item: NavItem; onClick?: () => void }) {
  return (
    <button
      onClick={onClick}
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: '6px',
        padding: '5px 14px',
        fontSize: '12px',
        color: 'var(--color-text-secondary)',
        background: 'transparent',
        border: 'none',
        borderRadius: '2px',
        cursor: 'pointer',
        transition: 'all 0.2s',
        whiteSpace: 'nowrap',
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.color = 'var(--color-glow)';
        e.currentTarget.style.background = 'rgba(0, 243, 255, 0.05)';
        e.currentTarget.style.boxShadow = '0 0 8px rgba(0,243,255,0.15)';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.color = 'var(--color-text-secondary)';
        e.currentTarget.style.background = 'transparent';
        e.currentTarget.style.boxShadow = 'none';
      }}
    >
      <span style={{ fontSize: '13px' }}>{item.icon}</span>
      {item.label}
    </button>
  );
}

export default function Header() {
  const handleFullscreen = () => {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen();
    } else {
      document.exitFullscreen();
    }
  };

  return (
    <div
      style={{
        height: '58px',
        flexShrink: 0,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 24px',
        position: 'relative',
      }}
    >
      {/* 左侧装饰线 + 导航 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flex: '1' }}>
        <svg width="180" height="30" style={{ opacity: 0.4 }}>
          <defs>
            <linearGradient id="lineGradL" x1="0" y1="0" x2="1" y2="0">
              <stop offset="0%" stopColor="transparent" />
              <stop offset="100%" stopColor="#00f3ff" />
            </linearGradient>
          </defs>
          <line x1="0" y1="15" x2="180" y2="15" stroke="url(#lineGradL)" strokeWidth="1" />
          <circle cx="180" cy="15" r="2.5" fill="#00f3ff" />
        </svg>
        {leftNavItems.map((item) => (
          <NavButton key={item.key} item={item} />
        ))}
      </div>

      {/* 中间标题 - 渐变发光 */}
      <div style={{ textAlign: 'center', flex: '0 0 auto' }}>
        <h1
          style={{
            fontSize: '26px',
            fontWeight: 'bold',
            letterSpacing: '8px',
            margin: '0',
            background: 'linear-gradient(90deg, #00f3ff, #1890ff, #00f3ff)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            backgroundClip: 'text',
            filter: 'drop-shadow(0 0 12px rgba(0,243,255,0.3))',
          }}
        >
          数智企业服务平台
        </h1>
      </div>

      {/* 右侧导航 + 装饰线 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flex: '1', justifyContent: 'flex-end' }}>
        {rightNavItems.map((item) => (
          <NavButton
            key={item.key}
            item={item}
            onClick={item.key === 'fullscreen' ? handleFullscreen : undefined}
          />
        ))}
        <svg width="180" height="30" style={{ opacity: 0.4 }}>
          <defs>
            <linearGradient id="lineGradR" x1="0" y1="0" x2="1" y2="0">
              <stop offset="0%" stopColor="#00f3ff" />
              <stop offset="100%" stopColor="transparent" />
            </linearGradient>
          </defs>
          <circle cx="0" cy="15" r="2.5" fill="#00f3ff" />
          <line x1="0" y1="15" x2="180" y2="15" stroke="url(#lineGradR)" strokeWidth="1" />
        </svg>
      </div>

      {/* 底部发光分隔线 - 增强 */}
      <div
        style={{
          position: 'absolute',
          bottom: 0,
          left: 0,
          right: 0,
          height: '1px',
          background: 'linear-gradient(90deg, transparent 5%, rgba(0,243,255,0.15) 15%, rgba(0,243,255,0.6) 50%, rgba(0,243,255,0.15) 85%, transparent 95%)',
          boxShadow: '0 0 8px rgba(0,243,255,0.2)',
        }}
      />
    </div>
  );
}

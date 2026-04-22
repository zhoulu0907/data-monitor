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
        padding: '6px 16px',
        fontSize: '13px',
        color: 'var(--color-text-secondary)',
        background: 'transparent',
        border: '1px solid transparent',
        borderRadius: '2px',
        cursor: 'pointer',
        transition: 'all 0.2s',
        whiteSpace: 'nowrap',
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.color = 'var(--color-glow)';
        e.currentTarget.style.borderColor = 'rgba(0, 243, 255, 0.3)';
        e.currentTarget.style.background = 'rgba(0, 243, 255, 0.05)';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.color = 'var(--color-text-secondary)';
        e.currentTarget.style.borderColor = 'transparent';
        e.currentTarget.style.background = 'transparent';
      }}
    >
      <span style={{ fontSize: '14px' }}>{item.icon}</span>
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
        height: '70px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 24px',
        position: 'relative',
      }}
    >
      {/* 左侧装饰线 + 导航 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flex: '1' }}>
        <svg width="200" height="40" style={{ opacity: 0.4 }}>
          <line x1="0" y1="20" x2="200" y2="20" stroke="#00f3ff" strokeWidth="1" />
          <circle cx="200" cy="20" r="3" fill="#00f3ff" />
        </svg>
        {leftNavItems.map((item) => (
          <NavButton key={item.key} item={item} />
        ))}
      </div>

      {/* 中间标题 */}
      <div style={{ textAlign: 'center', flex: '0 0 auto' }}>
        <h1
          className="glow-text"
          style={{
            fontSize: '28px',
            fontWeight: 'bold',
            letterSpacing: '8px',
            margin: '0',
          }}
        >
          数智企业服务平台
        </h1>
      </div>

      {/* 右侧导航 + 装饰线 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flex: '1', justifyContent: 'flex-end' }}>
        {rightNavItems.map((item) => (
          <NavButton
            key={item.key}
            item={item}
            onClick={item.key === 'fullscreen' ? handleFullscreen : undefined}
          />
        ))}
        <svg width="200" height="40" style={{ opacity: 0.4 }}>
          <circle cx="0" cy="20" r="3" fill="#00f3ff" />
          <line x1="0" y1="20" x2="200" y2="20" stroke="#00f3ff" strokeWidth="1" />
        </svg>
      </div>

      {/* 底部发光分隔线 */}
      <div
        style={{
          position: 'absolute',
          bottom: 0,
          left: 0,
          right: 0,
          height: '1px',
          background: 'linear-gradient(90deg, transparent, rgba(0,243,255,0.6), transparent)',
        }}
      />
    </div>
  );
}

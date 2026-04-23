import { useNavigate } from 'react-router-dom';

export default function ChatHeader() {
  const navigate = useNavigate();

  return (
    <div
      style={{
        height: '56px',
        flexShrink: 0,
        display: 'flex',
        alignItems: 'center',
        padding: '0 20px',
        position: 'relative',
        background: 'rgba(8,24,58,0.65)',
        borderBottom: '1px solid rgba(0,243,255,0.08)',
      }}
    >
      {/* 返回按钮 */}
      <button
        onClick={() => navigate('/')}
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '6px',
          padding: '6px 14px',
          fontSize: '13px',
          color: '#8798af',
          background: 'transparent',
          border: '1px solid rgba(0,243,255,0.12)',
          borderRadius: '4px',
          cursor: 'pointer',
          transition: 'all 0.2s',
          whiteSpace: 'nowrap',
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.color = '#00f3ff';
          e.currentTarget.style.borderColor = 'rgba(0,243,255,0.3)';
          e.currentTarget.style.boxShadow = '0 0 8px rgba(0,243,255,0.15)';
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.color = '#8798af';
          e.currentTarget.style.borderColor = 'rgba(0,243,255,0.12)';
          e.currentTarget.style.boxShadow = 'none';
        }}
      >
        ← 返回大屏
      </button>

      {/* 标题 */}
      <div style={{ flex: 1, textAlign: 'center' }}>
        <span
          style={{
            fontSize: '20px',
            fontWeight: 'bold',
            letterSpacing: '4px',
            background: 'linear-gradient(90deg, #00f3ff, #1890ff, #00f3ff)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            backgroundClip: 'text',
            filter: 'drop-shadow(0 0 10px rgba(0,243,255,0.3))',
          }}
        >
          AI 智策中枢
        </span>
      </div>

      {/* 右侧占位保持标题居中 */}
      <div style={{ width: '90px' }} />

      {/* 底部发光分隔线 */}
      <div
        style={{
          position: 'absolute',
          bottom: 0,
          left: 0,
          right: 0,
          height: '1px',
          background:
            'linear-gradient(90deg, transparent 5%, rgba(0,243,255,0.2) 15%, rgba(0,243,255,0.6) 50%, rgba(0,243,255,0.2) 85%, transparent 95%)',
          boxShadow: '0 0 8px rgba(0,243,255,0.2)',
        }}
      />
    </div>
  );
}

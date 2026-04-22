/**
 * 科技感脉冲加载骨架
 *
 * @author zhoulu
 * @since 2026-04-23
 */
export default function LoadingSkeleton({ text = '加载中...' }: { text?: string }) {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        height: '100%',
        gap: '8px',
      }}
    >
      <div className="animate-pulse" style={{
        width: '40px',
        height: '4px',
        background: 'rgba(0, 243, 255, 0.3)',
        borderRadius: '2px',
      }} />
      <span style={{ color: 'var(--color-text-secondary)', fontSize: '12px' }}>
        {text}
      </span>
    </div>
  );
}

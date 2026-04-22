export default function CenterPanel() {
  return (
    <div
      className="panel-border corner-cut"
      style={{ height: '100%', padding: '12px' }}
    >
      <div className="panel-title">财务趋势</div>
      <div style={{ color: 'var(--color-text-secondary)', textAlign: 'center', padding: '40px 0' }}>
        双折线图 / 人员面积图（待实现）
      </div>
    </div>
  );
}

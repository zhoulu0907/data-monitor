export default function Header() {
  return (
    <div
      style={{
        height: '70px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        position: 'relative',
        borderBottom: '1px solid rgba(0, 243, 255, 0.2)',
      }}
    >
      <h1 className="glow-text" style={{ fontSize: '28px', fontWeight: 'bold', letterSpacing: '8px' }}>
        数智企业服务平台
      </h1>
    </div>
  );
}

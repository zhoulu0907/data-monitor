interface Props {
  value: string | number;
  label: string;
  unit?: string;
}

export default function GlowingPedestalStat({ value, label, unit }: Props) {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        flex: '1',
        padding: '6px 4px',
      }}
    >
      {/* 数值 */}
      <span
        className="font-number"
        style={{
          fontSize: '28px',
          fontWeight: 'bold',
          color: '#00f3ff',
          lineHeight: '1.2',
        }}
      >
        {value}
        {unit && (
          <span style={{ fontSize: '11px', fontWeight: 'normal', color: 'var(--color-text-secondary)' }}>
            {unit}
          </span>
        )}
      </span>

      {/* 标签 */}
      <span
        style={{
          fontSize: '11px',
          color: 'var(--color-text-secondary)',
          marginTop: '2px',
        }}
      >
        {label}
      </span>

      {/* 发光底座 */}
      <div
        style={{
          width: '100%',
          height: '18px',
          marginTop: '4px',
          clipPath: 'polygon(15% 0%, 85% 0%, 100% 100%, 0% 100%)',
          background: 'linear-gradient(180deg, rgba(0,243,255,0.35) 0%, rgba(0,243,255,0.08) 100%)',
          boxShadow: '0 -6px 16px rgba(0, 243, 255, 0.3), 0 -2px 8px rgba(0, 243, 255, 0.2)',
        }}
      />
    </div>
  );
}

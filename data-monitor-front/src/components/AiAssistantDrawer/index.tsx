export default function AiAssistantDrawer() {
  return (
    <button
      className="animate-breathe"
      style={{
        position: 'absolute',
        bottom: '20px',
        right: '20px',
        width: '50px',
        height: '50px',
        borderRadius: '50%',
        background: 'linear-gradient(135deg, #00f3ff, #1890ff)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 100,
        fontSize: '20px',
      }}
    >
      AI
    </button>
  );
}

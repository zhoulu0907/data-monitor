import type { AiChatMessage } from '../../types/dashboard';

interface Props {
  message: AiChatMessage;
}

export default function ChatMessage({ message }: Props) {
  const isAssistant = message.role === 'assistant';

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: isAssistant ? 'row' : 'row-reverse',
        gap: '8px',
        marginBottom: '12px',
      }}
    >
      {/* 头像 */}
      <div
        style={{
          width: '28px',
          height: '28px',
          borderRadius: '50%',
          background: isAssistant
            ? 'linear-gradient(135deg, #00f3ff, #1890ff)'
            : 'linear-gradient(135deg, #8b5cf6, #ec4899)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '12px',
          flexShrink: 0,
        }}
      >
        {isAssistant ? 'AI' : 'U'}
      </div>

      {/* 消息气泡 */}
      <div
        style={{
          maxWidth: '260px',
          padding: '8px 12px',
          borderRadius: isAssistant ? '2px 8px 8px 8px' : '8px 2px 8px 8px',
          background: isAssistant
            ? 'rgba(13, 31, 66, 0.8)'
            : 'rgba(0, 243, 255, 0.1)',
          border: `1px solid ${isAssistant ? 'rgba(0, 243, 255, 0.15)' : 'rgba(0, 243, 255, 0.25)'}`,
        }}
      >
        <div
          style={{
            fontSize: '12px',
            lineHeight: '1.5',
            color: '#e0e7ff',
            whiteSpace: 'pre-wrap',
          }}
        >
          {message.content}
        </div>
        <div
          style={{
            fontSize: '9px',
            color: 'var(--color-text-secondary)',
            marginTop: '4px',
            textAlign: isAssistant ? 'left' : 'right',
          }}
        >
          {message.timestamp}
        </div>
      </div>
    </div>
  );
}

import { useEffect, useRef } from 'react';
import type { ChatMessage } from '../../types/ai-chat';
import DynamicMessageRenderer from './DynamicMessageRenderer';
import { useAiChatStore } from '../../store/useAiChatStore';

interface Props {
  messages: ChatMessage[];
}

export default function ChatArea({ messages }: Props) {
  const scrollRef = useRef<HTMLDivElement>(null);
  const retryLastMessage = useAiChatStore((s) => s.retryLastMessage);
  const isStreaming = useAiChatStore((s) => s.isStreaming);

  // 自动滚动到底部
  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

  const formatTimestamp = (isoString: string) => {
    const d = new Date(isoString);
    return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
  };

  return (
    <div
      ref={scrollRef}
      style={{
        flex: 1,
        overflowY: 'auto',
        padding: '20px 24px',
        display: 'flex',
        flexDirection: 'column',
        gap: '16px',
      }}
    >
      {messages.length === 0 && (
        <div
          style={{
            flex: 1,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '16px',
            color: '#5a6b7e',
          }}
        >
          <div style={{ fontSize: '48px', opacity: 0.3 }}>🤖</div>
          <div style={{ fontSize: '15px' }}>开始一段新的对话</div>
          <div style={{ fontSize: '12px', color: '#3d4f63' }}>
            你可以问我任何关于企业经营数据的问题
          </div>
        </div>
      )}

      {messages.map((msg) => {
        const isUser = msg.role === 'user';
        const showError = !isUser && msg.hasError;

        return (
          <div
            key={msg.id}
            style={{
              display: 'flex',
              justifyContent: isUser ? 'flex-end' : 'flex-start',
            }}
          >
            <div
              style={{
                maxWidth: '75%',
                padding: '12px 16px',
                borderRadius: isUser ? '12px 12px 2px 12px' : '12px 12px 12px 2px',
                background: isUser
                  ? 'rgba(24,144,255,0.1)'
                  : 'rgba(8,24,58,0.65)',
                border: isUser
                  ? '1px solid rgba(24,144,255,0.25)'
                  : showError
                    ? '1px solid rgba(255,140,0,0.3)'
                    : '1px solid rgba(0,243,255,0.08)',
                backdropFilter: isUser ? 'none' : 'blur(8px)',
              }}
            >
              {/* 角色标识 */}
              <div
                style={{
                  fontSize: '11px',
                  color: isUser ? '#1890ff' : '#00f3ff',
                  marginBottom: '6px',
                  fontWeight: 'bold',
                }}
              >
                {isUser ? '你' : 'AI 助手'}
              </div>

              {/* 消息内容 */}
              {isUser ? (
                <div
                  style={{
                    color: '#e0e6f0',
                    fontSize: '14px',
                    lineHeight: 1.7,
                  }}
                >
                  {msg.content}
                </div>
              ) : (
                <DynamicMessageRenderer chunks={msg.chunks} />
              )}

              {/* 时间戳 */}
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  marginTop: '6px',
                }}
              >
                <div style={{ fontSize: '10px', color: '#3d4f63' }}>
                  {formatTimestamp(msg.timestamp)}
                </div>

                {/* 错误消息的重试按钮 */}
                {showError && (
                  <button
                    onClick={() => retryLastMessage()}
                    disabled={isStreaming}
                    style={{
                      fontSize: '11px',
                      color: isStreaming ? '#5a6b7e' : '#ff8c00',
                      background: 'rgba(255,140,0,0.08)',
                      border: '1px solid rgba(255,140,0,0.25)',
                      borderRadius: '4px',
                      padding: '2px 10px',
                      cursor: isStreaming ? 'not-allowed' : 'pointer',
                      transition: 'all 0.2s ease',
                    }}
                    onMouseEnter={(e) => {
                      if (!isStreaming) {
                        (e.target as HTMLButtonElement).style.background = 'rgba(255,140,0,0.15)';
                      }
                    }}
                    onMouseLeave={(e) => {
                      (e.target as HTMLButtonElement).style.background = 'rgba(255,140,0,0.08)';
                    }}
                  >
                    重新发送
                  </button>
                )}
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}

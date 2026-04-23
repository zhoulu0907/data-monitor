import { useState, useRef, useEffect } from 'react';

interface Props {
  /** 发送消息回调 */
  onSend: (message: string) => void;
  /** 是否正在流式接收 */
  isStreaming: boolean;
  /** 停止流式接收回调 */
  onStop: () => void;
}

export default function ChatInput({ onSend, isStreaming, onStop }: Props) {
  const [value, setValue] = useState('');
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  // 自动调整 textarea 高度
  useEffect(() => {
    const el = textareaRef.current;
    if (el) {
      el.style.height = 'auto';
      el.style.height = Math.min(el.scrollHeight, 120) + 'px';
    }
  }, [value]);

  const handleSend = () => {
    const trimmed = value.trim();
    if (!trimmed || isStreaming) return;
    onSend(trimmed);
    setValue('');
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  // 判断发送按钮是否可用
  const canSend = !!value.trim() && !isStreaming;

  return (
    <div
      style={{
        padding: '12px 20px 16px',
        background: 'rgba(8,24,58,0.85)',
        backdropFilter: 'blur(12px)',
        borderTop: '1px solid rgba(0,243,255,0.08)',
      }}
    >
      <div
        style={{
          display: 'flex',
          alignItems: 'flex-end',
          gap: '10px',
          background: 'rgba(0,243,255,0.03)',
          border: '1px solid rgba(0,243,255,0.12)',
          borderRadius: '8px',
          padding: '8px 12px',
        }}
      >
        <textarea
          ref={textareaRef}
          value={value}
          onChange={(e) => {
            if (e.target.value.length <= 500) {
              setValue(e.target.value);
            }
          }}
          onKeyDown={handleKeyDown}
          placeholder="输入消息，Enter 发送，Shift+Enter 换行..."
          rows={1}
          disabled={isStreaming}
          style={{
            flex: 1,
            background: 'transparent',
            border: 'none',
            outline: 'none',
            color: isStreaming ? '#5a6b7e' : '#e0e6f0',
            fontSize: '14px',
            lineHeight: 1.6,
            resize: 'none',
            fontFamily: 'inherit',
            maxHeight: '120px',
            opacity: isStreaming ? 0.6 : 1,
          }}
        />
        <div
          style={{
            fontSize: '11px',
            color: '#5a6b7e',
            paddingBottom: '4px',
            whiteSpace: 'nowrap',
          }}
        >
          {value.length}/500
        </div>

        {/* 流式期间显示停止按钮，否则显示发送按钮 */}
        {isStreaming ? (
          <button
            onClick={onStop}
            style={{
              padding: '8px 18px',
              fontSize: '13px',
              color: '#ffffff',
              background: 'linear-gradient(135deg, #ff4d4f, #cf1322)',
              border: 'none',
              borderRadius: '6px',
              cursor: 'pointer',
              transition: 'all 0.2s',
              fontWeight: 'bold',
              letterSpacing: '1px',
              whiteSpace: 'nowrap',
              boxShadow: '0 0 8px rgba(255,77,79,0.3)',
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.boxShadow = '0 0 14px rgba(255,77,79,0.5)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.boxShadow = '0 0 8px rgba(255,77,79,0.3)';
            }}
          >
            停止
          </button>
        ) : (
          <button
            onClick={handleSend}
            disabled={!canSend}
            style={{
              padding: '8px 18px',
              fontSize: '13px',
              color: canSend ? '#081838' : '#5a6b7e',
              background: canSend
                ? 'linear-gradient(135deg, #00f3ff, #1890ff)'
                : 'rgba(0,243,255,0.1)',
              border: 'none',
              borderRadius: '6px',
              cursor: canSend ? 'pointer' : 'not-allowed',
              transition: 'all 0.2s',
              fontWeight: 'bold',
              letterSpacing: '1px',
              whiteSpace: 'nowrap',
            }}
          >
            发送
          </button>
        )}
      </div>
    </div>
  );
}

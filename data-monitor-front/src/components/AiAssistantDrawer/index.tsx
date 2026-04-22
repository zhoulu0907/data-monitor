import { useState, useRef, useEffect } from 'react';
import { useDashboardStore } from '../../store/useDashboardStore';
import ChatMessage from './ChatMessage';

export default function AiAssistantDrawer() {
  const [isOpen, setIsOpen] = useState(false);
  const [input, setInput] = useState('');
  const chatEndRef = useRef<HTMLDivElement>(null);
  const toggleAiDrawer = useDashboardStore((s) => s.toggleAiDrawer);
  const addUserMessage = useDashboardStore((s) => s.addUserMessage);
  const aiInsights = useDashboardStore((s) => s.aiInsights);

  // 自动滚动到底部
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [aiInsights?.chatHistory.length]);

  const handleSend = () => {
    if (!input.trim()) return;
    addUserMessage(input.trim());
    setInput('');
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <>
      {/* AI 悬浮按钮 */}
      <button
        className="animate-breathe"
        onClick={() => {
          setIsOpen(true);
          toggleAiDrawer();
        }}
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
          fontSize: '16px',
          fontWeight: 'bold',
          color: '#fff',
          boxShadow: '0 0 15px rgba(0,243,255,0.4)',
        }}
      >
        AI
      </button>

      {/* Drawer 遮罩 */}
      {isOpen && (
        <div
          className="animate-fade-in"
          onClick={() => setIsOpen(false)}
          style={{
            position: 'absolute',
            inset: 0,
            background: 'rgba(0,0,0,0.5)',
            zIndex: 200,
          }}
        />
      )}

      {/* Drawer 面板 */}
      {isOpen && (
        <div
          className="animate-slide-in"
          style={{
            position: 'absolute',
            top: 0,
            right: 0,
            width: '380px',
            height: '100%',
            background: 'rgba(8, 24, 58, 0.85)',
            backdropFilter: 'blur(12px)',
            WebkitBackdropFilter: 'blur(12px)',
            boxShadow: '-10px 0 30px rgba(0,0,0,0.5), inset 1px 0 0 rgba(0,243,255,0.2)',
            zIndex: 300,
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          {/* 头部 */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '16px 20px',
              borderBottom: '1px solid rgba(0,243,255,0.15)',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <div
                style={{
                  width: '8px',
                  height: '8px',
                  borderRadius: '50%',
                  background: '#00f3ff',
                  boxShadow: '0 0 6px #00f3ff',
                }}
              />
              <span style={{ fontSize: '16px', fontWeight: 'bold', color: '#fff' }}>
                AI 智策中枢
              </span>
            </div>
            <button
              onClick={() => setIsOpen(false)}
              style={{
                fontSize: '18px',
                color: 'var(--color-text-secondary)',
                padding: '4px 8px',
              }}
            >
              ✕
            </button>
          </div>

          {/* 聊天区域 */}
          <div
            style={{
              flex: 1,
              overflowY: 'auto',
              padding: '16px',
            }}
          >
            {aiInsights?.chatHistory.map((msg) => (
              <ChatMessage key={msg.id} message={msg} />
            ))}
            <div ref={chatEndRef} />
          </div>

          {/* 输入区域 */}
          <div
            style={{
              padding: '12px 16px',
              borderTop: '1px solid rgba(0,243,255,0.15)',
              display: 'flex',
              gap: '8px',
            }}
          >
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="输入您的问题..."
              style={{
                flex: 1,
                padding: '8px 12px',
                background: 'rgba(13, 31, 66, 0.6)',
                border: '1px solid rgba(0,243,255,0.2)',
                borderRadius: '4px',
                color: '#fff',
                fontSize: '13px',
                outline: 'none',
              }}
            />
            <button
              onClick={handleSend}
              style={{
                padding: '8px 16px',
                background: 'linear-gradient(135deg, #00f3ff, #1890ff)',
                borderRadius: '4px',
                color: '#fff',
                fontSize: '13px',
                fontWeight: 'bold',
              }}
            >
              发送
            </button>
          </div>
        </div>
      )}
    </>
  );
}

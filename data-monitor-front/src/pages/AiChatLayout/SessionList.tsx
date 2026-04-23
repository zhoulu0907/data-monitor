import { useState } from 'react';
import type { ChatSession } from '../../types/ai-chat';

interface Props {
  /** 会话列表（从 store 传入） */
  sessions: ChatSession[];
  /** 当前活跃的会话 ID */
  activeSessionId: string | null;
  /** 选择会话回调 */
  onSelectSession: (session: ChatSession) => void;
  /** 新建对话回调 */
  onNewChat: () => void;
  /** 删除会话回调 */
  onDeleteSession: (sessionId: string) => void;
}

export default function SessionList({
  sessions,
  activeSessionId,
  onSelectSession,
  onNewChat,
  onDeleteSession,
}: Props) {
  const [hoveredId, setHoveredId] = useState<string | null>(null);

  const handleDelete = (e: React.MouseEvent, sessionId: string) => {
    e.stopPropagation();
    onDeleteSession(sessionId);
  };

  const formatTime = (isoString: string) => {
    const d = new Date(isoString);
    return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
  };

  return (
    <div
      style={{
        width: '260px',
        flexShrink: 0,
        display: 'flex',
        flexDirection: 'column',
        background: 'rgba(8,24,58,0.65)',
        borderRight: '1px solid rgba(0,243,255,0.08)',
        overflow: 'hidden',
      }}
    >
      {/* 新对话按钮 */}
      <div style={{ padding: '16px 14px 12px' }}>
        <button
          onClick={onNewChat}
          style={{
            width: '100%',
            padding: '10px',
            fontSize: '13px',
            color: '#00f3ff',
            background: 'rgba(0,243,255,0.06)',
            border: '1px dashed rgba(0,243,255,0.25)',
            borderRadius: '6px',
            cursor: 'pointer',
            transition: 'all 0.2s',
            letterSpacing: '1px',
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.background = 'rgba(0,243,255,0.1)';
            e.currentTarget.style.boxShadow = '0 0 10px rgba(0,243,255,0.15)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.background = 'rgba(0,243,255,0.06)';
            e.currentTarget.style.boxShadow = 'none';
          }}
        >
          + 新对话
        </button>
      </div>

      {/* 分隔线 */}
      <div
        style={{
          height: '1px',
          margin: '0 14px',
          background: 'rgba(0,243,255,0.08)',
        }}
      />

      {/* 会话列表 */}
      <div
        style={{
          flex: 1,
          overflowY: 'auto',
          padding: '8px 8px',
        }}
      >
        {sessions.length === 0 && (
          <div
            style={{
              textAlign: 'center',
              color: '#3d4f63',
              fontSize: '12px',
              padding: '20px 0',
            }}
          >
            暂无会话记录
          </div>
        )}
        {sessions.map((session) => {
          const isActive = session.id === activeSessionId;
          const isHovered = session.id === hoveredId;
          return (
            <div
              key={session.id}
              onClick={() => onSelectSession(session)}
              onMouseEnter={() => setHoveredId(session.id)}
              onMouseLeave={() => setHoveredId(null)}
              style={{
                padding: '10px 12px',
                marginBottom: '4px',
                borderRadius: '6px',
                cursor: 'pointer',
                transition: 'all 0.2s',
                background: isActive
                  ? 'rgba(0,243,255,0.1)'
                  : isHovered
                    ? 'rgba(0,243,255,0.04)'
                    : 'transparent',
                borderLeft: isActive
                  ? '2px solid #00f3ff'
                  : '2px solid transparent',
                position: 'relative',
              }}
            >
              {/* 标题（截断20字） */}
              <div
                style={{
                  fontSize: '13px',
                  color: isActive ? '#e0e6f0' : '#b0bfcc',
                  marginBottom: '4px',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap',
                  paddingRight: '20px',
                }}
              >
                {session.title.length > 20
                  ? session.title.slice(0, 20) + '...'
                  : session.title}
              </div>

              {/* 时间 */}
              <div
                style={{
                  fontSize: '11px',
                  color: '#5a6b7e',
                }}
              >
                {formatTime(session.updatedAt)}
              </div>

              {/* 删除按钮（hover 显示） */}
              {(isHovered || isActive) && (
                <button
                  onClick={(e) => handleDelete(e, session.id)}
                  style={{
                    position: 'absolute',
                    right: '8px',
                    top: '50%',
                    transform: 'translateY(-50%)',
                    padding: '2px 6px',
                    fontSize: '11px',
                    color: '#8798af',
                    background: 'transparent',
                    border: 'none',
                    borderRadius: '3px',
                    cursor: 'pointer',
                    transition: 'all 0.2s',
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.color = '#ff6b6b';
                    e.currentTarget.style.background =
                      'rgba(255,100,100,0.1)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.color = '#8798af';
                    e.currentTarget.style.background = 'transparent';
                  }}
                >
                  ✕
                </button>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

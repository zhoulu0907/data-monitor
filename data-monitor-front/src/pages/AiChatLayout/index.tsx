import { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import ChatHeader from './ChatHeader';
import SessionList from './SessionList';
import ChatArea from './ChatArea';
import ChatInput from './ChatInput';
import { useAiChatStore } from '../../store/useAiChatStore';
import '../../styles/global.css';
import '../../styles/glow-effects.css';
import '../../styles/animations.css';

export default function AiChatLayout() {
  const { sessionId: urlSessionId } = useParams<{ sessionId: string }>();

  // 从 Zustand store 获取状态和操作
  const sessions = useAiChatStore((s) => s.sessions);
  const activeSessionId = useAiChatStore((s) => s.activeSessionId);
  const messagesMap = useAiChatStore((s) => s.messagesMap);
  const isStreaming = useAiChatStore((s) => s.isStreaming);
  const loadSessions = useAiChatStore((s) => s.loadSessions);
  const switchSession = useAiChatStore((s) => s.switchSession);
  const deleteSession = useAiChatStore((s) => s.deleteSession);
  const newChat = useAiChatStore((s) => s.newChat);
  const sendMessage = useAiChatStore((s) => s.sendMessage);
  const stopStreaming = useAiChatStore((s) => s.stopStreaming);

  // 初始化：加载会话列表
  useEffect(() => {
    loadSessions();
  }, [loadSessions]);

  // 如果 URL 中有 sessionId 且 store 还没有切换到该会话，则切换
  useEffect(() => {
    if (urlSessionId && urlSessionId !== activeSessionId) {
      switchSession(urlSessionId);
    }
  }, [urlSessionId, activeSessionId, switchSession]);

  // 当前会话的消息列表
  const currentMessages = activeSessionId
    ? messagesMap[activeSessionId] || []
    : [];

  return (
    <div
      style={{
        width: '100vw',
        height: '100vh',
        overflow: 'hidden',
        background:
          'radial-gradient(ellipse at 50% 0%, rgba(0,243,255,0.04) 0%, transparent 60%), linear-gradient(180deg, #081838 0%, #0a1e4a 40%, #061230 100%)',
        display: 'flex',
        flexDirection: 'column',
        fontFamily:
          "'PingFang SC', 'Microsoft YaHei', -apple-system, BlinkMacSystemFont, sans-serif",
      }}
    >
      {/* 顶栏 */}
      <ChatHeader />

      {/* 主体：左侧会话栏 + 右侧对话区 */}
      <div style={{ flex: 1, display: 'flex', overflow: 'hidden' }}>
        {/* 左侧会话列表 */}
        <SessionList
          sessions={sessions}
          activeSessionId={activeSessionId}
          onSelectSession={(session) => switchSession(session.id)}
          onNewChat={newChat}
          onDeleteSession={deleteSession}
        />

        {/* 右侧对话区 */}
        <div
          style={{
            flex: 1,
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
            background: 'rgba(4,12,36,0.4)',
          }}
        >
          <ChatArea messages={currentMessages} />
          <ChatInput
            onSend={sendMessage}
            isStreaming={isStreaming}
            onStop={stopStreaming}
          />
        </div>
      </div>
    </div>
  );
}

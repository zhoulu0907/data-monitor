/**
 * AI 聊天 Zustand 状态管理
 * 管理会话列表、消息、流式通信等状态
 */
import { create } from 'zustand';
import type { ChatSession, ChatMessage, ChatMessageChunk } from '../types/ai-chat';
import {
  listSessions as apiListSessions,
  createSession as apiCreateSession,
  deleteSession as apiDeleteSession,
  listMessages as apiListMessages,
  streamChat as apiStreamChat,
} from '../api/sessionApi';

/** Store 状态接口 */
interface AiChatState {
  /** 所有会话列表 */
  sessions: ChatSession[];
  /** 当前活跃的会话 ID */
  activeSessionId: string | null;
  /** 各会话的消息映射：sessionId → ChatMessage[] */
  messagesMap: Record<string, ChatMessage[]>;
  /** 是否正在流式接收 */
  isStreaming: boolean;
  /** 用于中断流式请求的 AbortController */
  abortController: AbortController | null;

  /** 加载所有会话 */
  loadSessions: () => Promise<void>;
  /** 创建新会话 */
  createSession: (title?: string) => Promise<ChatSession>;
  /** 切换到指定会话 */
  switchSession: (sessionId: string) => void;
  /** 删除指定会话 */
  deleteSession: (sessionId: string) => Promise<void>;
  /** 发送消息（核心：SSE 流式通信） */
  sendMessage: (message: string) => Promise<void>;
  /** 重新发送最后一条用户消息 */
  retryLastMessage: () => Promise<void>;
  /** 停止流式接收 */
  stopStreaming: () => void;
  /** 加载指定会话的消息 */
  loadMessages: (sessionId: string) => Promise<void>;
  /** 新建空白会话（清空 activeSessionId） */
  newChat: () => void;
}

export const useAiChatStore = create<AiChatState>((set, get) => ({
  sessions: [],
  activeSessionId: null,
  messagesMap: {},
  isStreaming: false,
  abortController: null,

  /**
   * 从后端加载所有会话列表
   */
  loadSessions: async () => {
    try {
      const sessions = await apiListSessions();
      // 按更新时间倒序排列
      sessions.sort(
        (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime(),
      );
      set({ sessions });
    } catch (error) {
      console.error('加载会话列表失败:', error);
    }
  },

  /**
   * 创建新会话
   * @param title 可选标题，不传则后端生成
   * @returns 新创建的会话对象
   */
  createSession: async (title?: string) => {
    const session = await apiCreateSession(title);
    set((state) => ({
      sessions: [session, ...state.sessions],
      messagesMap: { ...state.messagesMap, [session.id]: [] },
    }));
    return session;
  },

  /**
   * 切换到指定会话
   * 如果该会话还没有加载过消息，则自动加载
   */
  switchSession: (sessionId: string) => {
    set({ activeSessionId: sessionId });

    // 如果该会话消息尚未加载，则从后端加载
    const { messagesMap } = get();
    if (!messagesMap[sessionId]) {
      get().loadMessages(sessionId);
    }
  },

  /**
   * 删除指定会话
   */
  deleteSession: async (sessionId: string) => {
    try {
      await apiDeleteSession(sessionId);
      set((state) => {
        const newSessions = state.sessions.filter((s) => s.id !== sessionId);
        const newMessagesMap = { ...state.messagesMap };
        delete newMessagesMap[sessionId];
        return {
          sessions: newSessions,
          messagesMap: newMessagesMap,
          // 如果删除的是当前活跃会话，切换到第一个或置空
          activeSessionId:
            state.activeSessionId === sessionId
              ? newSessions[0]?.id || null
              : state.activeSessionId,
        };
      });
    } catch (error) {
      console.error('删除会话失败:', error);
    }
  },

  /**
   * 发送消息 - 核心流式通信逻辑
   * 1. 如果没有活跃会话，先创建新会话
   * 2. 追加用户消息到 messagesMap
   * 3. 创建空的 assistant 消息占位
   * 4. 通过 SSE 流式接收 AI 回复
   */
  sendMessage: async (message: string) => {
    const state = get();

    // 如果正在流式接收，忽略新的发送请求
    if (state.isStreaming) return;

    let sessionId = state.activeSessionId;

    // 如果没有活跃会话，先创建一个新会话
    if (!sessionId) {
      try {
        const newSession = await get().createSession(message.slice(0, 20));
        sessionId = newSession.id;
        set({ activeSessionId: sessionId });
      } catch (error) {
        console.error('创建会话失败:', error);
        return;
      }
    }

    // 构造用户消息
    const userMsg: ChatMessage = {
      id: `msg-user-${Date.now()}`,
      sessionId,
      role: 'user',
      chunks: [],
      content: message,
      timestamp: new Date().toISOString(),
    };

    // 构造 assistant 消息占位（空的 chunks，后续逐块填充）
    const assistantMsgId = `msg-assistant-${Date.now()}`;
    const assistantMsg: ChatMessage = {
      id: assistantMsgId,
      sessionId,
      role: 'assistant',
      chunks: [],
      content: '',
      timestamp: new Date().toISOString(),
    };

    // 将用户消息和空的 assistant 消息追加到 messagesMap
    set((state) => ({
      messagesMap: {
        ...state.messagesMap,
        [sessionId!]: [
          ...(state.messagesMap[sessionId!] || []),
          userMsg,
          assistantMsg,
        ],
      },
    }));

    // 创建 AbortController 用于中断请求
    const abortController = new AbortController();
    set({ isStreaming: true, abortController });

    try {
      // 调用 SSE 流式聊天
      await apiStreamChat(sessionId, message, abortController.signal, {
        // 文本块：追加到最后一个 text chunk，或创建新的 text chunk
        onTextChunk: (text: string) => {
          set((state) => {
            const messages = [...(state.messagesMap[sessionId!] || [])];
            const msgIndex = messages.findIndex((m) => m.id === assistantMsgId);
            if (msgIndex === -1) return state;

            const msg = { ...messages[msgIndex] };
            const chunks = [...msg.chunks];

            // 查找最后一个 text chunk
            const lastChunk = chunks[chunks.length - 1];
            if (lastChunk && lastChunk.type === 'text') {
              // 追加到最后一个 text chunk
              chunks[chunks.length - 1] = {
                ...lastChunk,
                content: lastChunk.content + text,
              };
            } else {
              // 创建新的 text chunk
              chunks.push({ type: 'text', content: text });
            }

            msg.chunks = chunks;
            messages[msgIndex] = msg;

            return {
              messagesMap: {
                ...state.messagesMap,
                [sessionId!]: messages,
              },
            };
          });
        },

        // 组件块：直接添加为新的 chunk
        onComponentChunk: (json: { componentName: string; props: Record<string, unknown> }) => {
          set((state) => {
            const messages = [...(state.messagesMap[sessionId!] || [])];
            const msgIndex = messages.findIndex((m) => m.id === assistantMsgId);
            if (msgIndex === -1) return state;

            const msg = { ...messages[msgIndex] };
            const newChunk: ChatMessageChunk = {
              type: 'component',
              componentName: json.componentName,
              props: json.props,
            };
            msg.chunks = [...msg.chunks, newChunk];
            messages[msgIndex] = msg;

            return {
              messagesMap: {
                ...state.messagesMap,
                [sessionId!]: messages,
              },
            };
          });
        },

        // 流正常结束
        onDone: () => {
          set((state) => {
            const messages = [...(state.messagesMap[sessionId!] || [])];
            const msgIndex = messages.findIndex((m) => m.id === assistantMsgId);
            if (msgIndex !== -1) {
              // 检查流是否异常中断（没有收到任何内容）
              const msg = { ...messages[msgIndex] };
              if (msg.chunks.length === 0) {
                // 流正常结束但没有内容，标记为异常
                msg.chunks = [
                  {
                    type: 'text',
                    content: '抱歉，未收到有效的回复内容，请重试。',
                  },
                ];
                msg.hasError = true;
                messages[msgIndex] = msg;
              }
            }

            return {
              messagesMap: {
                ...state.messagesMap,
                [sessionId!]: messages,
              },
              isStreaming: false,
              abortController: null,
            };
          });
          // 流结束后刷新会话列表（标题可能已更新）
          get().loadSessions();
        },

        // 出错处理（增强版）
        onError: (error: Error) => {
          console.error('流式通信出错:', error);

          // 根据错误类型生成更友好的提示
          let friendlyMessage: string;
          if (error.message.includes('HTTP 5')) {
            friendlyMessage = '服务端暂时异常，请稍后重试。';
          } else if (error.message.includes('HTTP 4')) {
            friendlyMessage = '请求参数有误，请修改后重试。';
          } else if (error.message.includes('超时')) {
            friendlyMessage = '请求超时，请检查网络连接后重试。';
          } else if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
            friendlyMessage = '网络连接失败，请检查网络后重试。';
          } else {
            friendlyMessage = `通信出现问题：${error.message}`;
          }

          // 在 assistant 消息中追加错误提示
          set((state) => {
            const messages = [...(state.messagesMap[sessionId!] || [])];
            const msgIndex = messages.findIndex((m) => m.id === assistantMsgId);
            if (msgIndex === -1) {
              return { isStreaming: false, abortController: null };
            }

            const msg = { ...messages[msgIndex] };
            msg.hasError = true;

            // 生成错误文本
            const errorText = `\n\n> **抱歉，${friendlyMessage}**`;

            if (msg.chunks.length === 0) {
              // 没有任何内容，直接设置错误提示
              msg.chunks = [
                {
                  type: 'text',
                  content: errorText,
                },
              ];
            } else {
              // 已有内容，追加错误提示
              const chunks = [...msg.chunks];
              const lastChunk = chunks[chunks.length - 1];
              if (lastChunk.type === 'text') {
                chunks[chunks.length - 1] = {
                  ...lastChunk,
                  content: lastChunk.content + errorText,
                };
              } else {
                chunks.push({ type: 'text', content: errorText });
              }
              msg.chunks = chunks;
            }

            messages[msgIndex] = msg;

            return {
              messagesMap: {
                ...state.messagesMap,
                [sessionId!]: messages,
              },
              isStreaming: false,
              abortController: null,
            };
          });
        },
      });
    } catch (error) {
      console.error('发送消息异常:', error);
      // 捕获非 SSE 流内的异常（如网络断开），标记 assistant 消息为错误
      set((state) => {
        const messages = [...(state.messagesMap[sessionId!] || [])];
        const msgIndex = messages.findIndex((m) => m.id === assistantMsgId);
        if (msgIndex !== -1) {
          const msg = { ...messages[msgIndex] };
          msg.hasError = true;
          if (msg.chunks.length === 0) {
            msg.chunks = [
              {
                type: 'text',
                content: '抱歉，发送消息时发生未知错误，请重试。',
              },
            ];
          }
          messages[msgIndex] = msg;
        }
        return {
          messagesMap: {
            ...state.messagesMap,
            [sessionId!]: messages,
          },
          isStreaming: false,
          abortController: null,
        };
      });
    }
  },

  /**
   * 重新发送最后一条用户消息
   * 移除最后一条 assistant 消息（错误状态），重新发送对应的用户消息
   */
  retryLastMessage: async () => {
    const state = get();
    const sessionId = state.activeSessionId;
    if (!sessionId) return;

    const messages = state.messagesMap[sessionId] || [];
    if (messages.length < 2) return;

    // 从后往前查找最后一条 assistant 错误消息及其对应的用户消息
    let lastUserMsg: ChatMessage | null = null;
    let removeCount = 0;

    for (let i = messages.length - 1; i >= 0; i--) {
      const msg = messages[i];
      if (msg.role === 'assistant' && msg.hasError && removeCount === 0) {
        // 移除这条错误的 assistant 消息
        removeCount = 1;
        continue;
      }
      if (msg.role === 'user' && removeCount === 1) {
        lastUserMsg = msg;
        break;
      }
      // 不符合条件，直接返回
      return;
    }

    if (!lastUserMsg) return;

    // 移除最后的错误 assistant 消息
    set((state) => ({
      messagesMap: {
        ...state.messagesMap,
        [sessionId]: messages.slice(0, -removeCount),
      },
    }));

    // 重新发送用户消息
    await get().sendMessage(lastUserMsg.content);
  },

  /**
   * 停止流式接收
   */
  stopStreaming: () => {
    const { abortController } = get();
    if (abortController) {
      abortController.abort();
    }
    set({ isStreaming: false, abortController: null });
  },

  /**
   * 加载指定会话的消息列表
   */
  loadMessages: async (sessionId: string) => {
    try {
      const messages = await apiListMessages(sessionId);
      set((state) => ({
        messagesMap: {
          ...state.messagesMap,
          [sessionId]: messages,
        },
      }));
    } catch (error) {
      console.error('加载消息失败:', error);
      // 加载失败时设置为空数组，避免重复请求
      set((state) => ({
        messagesMap: {
          ...state.messagesMap,
          [sessionId]: [],
        },
      }));
    }
  },

  /**
   * 新建空白会话（清空 activeSessionId，显示空白对话区）
   */
  newChat: () => {
    set({ activeSessionId: null });
  },
}));

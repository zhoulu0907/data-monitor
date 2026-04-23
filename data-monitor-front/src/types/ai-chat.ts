// SSE 消息块
export interface TextChunk {
  type: 'text';
  content: string;
}

export interface ComponentChunk {
  type: 'component';
  componentName: string;
  props: Record<string, unknown>;
}

export type ChatMessageChunk = TextChunk | ComponentChunk;

// 会话
export interface ChatSession {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;
}

// 聊天消息
export interface ChatMessage {
  id: string;
  sessionId: string;
  role: 'user' | 'assistant' | 'system';
  chunks: ChatMessageChunk[];
  content: string;         // 用户消息原文 或 assistant 的纯文本
  componentData?: string;  // assistant 的 A2UI 组件 JSON
  timestamp: string;
  /** 消息是否以错误结束，用于 UI 显示重试按钮 */
  hasError?: boolean;
}

// SSE 请求
export interface ChatRequest {
  sessionId: string | null;
  message: string;
  model?: string;
}

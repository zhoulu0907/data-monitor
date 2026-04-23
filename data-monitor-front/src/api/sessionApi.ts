/**
 * 会话与聊天消息的后端 REST API 封装
 * 所有请求通过 Vite 代理转发到后端
 */
import type { ChatSession, ChatMessage } from '../types/ai-chat';
import { fetchSSE } from './chat';
import type { SSECallbacks } from './chat';

/** API 基础路径 */
const BASE = '/api/v1/chat';

/**
 * 创建新会话
 * POST /api/v1/chat/sessions
 */
export async function createSession(title?: string): Promise<ChatSession> {
  const res = await fetch(`${BASE}/sessions`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(title ? { title } : {}),
  });

  if (!res.ok) {
    const errorText = await res.text().catch(() => '未知错误');
    throw new Error(`创建会话失败: HTTP ${res.status} ${errorText}`);
  }

  const json = await res.json();
  return json.data as ChatSession;
}

/**
 * 获取所有会话列表
 * GET /api/v1/chat/sessions
 */
export async function listSessions(): Promise<ChatSession[]> {
  const res = await fetch(`${BASE}/sessions`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
  });

  if (!res.ok) {
    const errorText = await res.text().catch(() => '未知错误');
    throw new Error(`获取会话列表失败: HTTP ${res.status} ${errorText}`);
  }

  const json = await res.json();
  return json.data as ChatSession[];
}

/**
 * 删除指定会话
 * POST /api/v1/chat/sessions/{id}/delete
 */
export async function deleteSession(id: string): Promise<void> {
  const res = await fetch(`${BASE}/sessions/${id}/delete`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
  });

  if (!res.ok) {
    const errorText = await res.text().catch(() => '未知错误');
    throw new Error(`删除会话失败: HTTP ${res.status} ${errorText}`);
  }
}

/**
 * 获取指定会话的消息列表
 * GET /api/v1/chat/sessions/{id}/messages
 */
export async function listMessages(sessionId: string): Promise<ChatMessage[]> {
  const res = await fetch(`${BASE}/sessions/${sessionId}/messages`, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
  });

  if (!res.ok) {
    const errorText = await res.text().catch(() => '未知错误');
    throw new Error(`获取消息列表失败: HTTP ${res.status} ${errorText}`);
  }

  const json = await res.json();
  return json.data as ChatMessage[];
}

/**
 * SSE 流式聊天
 * POST /api/v1/chat/completions
 * @param sessionId 会话 ID
 * @param message 用户消息
 * @param signal 中断信号
 * @param callbacks SSE 事件回调
 */
export async function streamChat(
  sessionId: string,
  message: string,
  signal: AbortSignal,
  callbacks: SSECallbacks,
): Promise<void> {
  await fetchSSE(
    `${BASE}/completions`,
    { sessionId, message },
    signal,
    callbacks,
  );
}

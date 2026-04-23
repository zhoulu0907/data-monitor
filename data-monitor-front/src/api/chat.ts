/**
 * SSE（Server-Sent Events）流式通信工具
 * 使用 fetch API + ReadableStream 逐行解析 SSE 协议
 */

/** HTTP 请求超时时间（毫秒） */
const FETCH_TIMEOUT_MS = 30_000;

/** 网络错误自动重试次数 */
const MAX_RETRY_COUNT = 1;

/** SSE 回调函数集合 */
export interface SSECallbacks {
  /** 收到文本块时调用 */
  onTextChunk: (text: string) => void;
  /** 收到组件块时调用 */
  onComponentChunk: (json: { componentName: string; props: Record<string, unknown> }) => void;
  /** 流正常结束时调用 */
  onDone: () => void;
  /** 出错时调用 */
  onError: (error: Error) => void;
}

/**
 * 带超时的 fetch 请求
 * 如果 fetch 本身超过指定时间无响应，抛出超时错误
 */
async function fetchWithTimeout(
  url: string,
  options: RequestInit,
  timeoutMs: number,
): Promise<Response> {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeoutMs);

  // 合并外部 signal 和超时 signal：任一触发都中止请求
  const externalSignal = options.signal ?? null;
  try {
    if (externalSignal) {
      // 如果外部已经中止，直接抛出
      if (externalSignal.aborted) {
        throw new DOMException('请求已被取消', 'AbortError');
      }
      // 监听外部中止信号
      externalSignal.addEventListener('abort', () => controller.abort(), { once: true });
    }

    const response = await fetch(url, {
      ...options,
      signal: controller.signal,
    });
    return response;
  } catch (error: unknown) {
    // 区分超时中止和外部中止
    if (
      error instanceof DOMException &&
      error.name === 'AbortError' &&
      externalSignal?.aborted !== true
    ) {
      throw new Error(`请求超时（${timeoutMs / 1000}秒未响应），请检查网络连接后重试`);
    }
    throw error;
  } finally {
    clearTimeout(timeoutId);
  }
}

/**
 * 发起 SSE 流式请求（带超时和自动重试）
 * @param url 请求地址
 * @param body 请求体
 * @param signal AbortController 的 signal，用于中断请求
 * @param callbacks 各类事件的回调函数
 */
export async function fetchSSE(
  url: string,
  body: Record<string, unknown>,
  signal: AbortSignal,
  callbacks: SSECallbacks,
): Promise<void> {
  const { onTextChunk, onComponentChunk, onDone, onError } = callbacks;

  /** 记录是否收到了 [DONE] 标记 */
  let receivedDone = false;

  /**
   * 内部执行函数：实际发起 SSE 请求
   * @returns true 表示成功完成，false 表示需要重试（仅网络错误）
   */
  async function execute(attempt: number): Promise<'done' | 'retry'> {
    const isRetry = attempt > 0;

    try {
      const response = await fetchWithTimeout(
        url,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Accept: 'text/event-stream',
          },
          body: JSON.stringify(body),
          signal,
        },
        FETCH_TIMEOUT_MS,
      );

      // 重试成功时输出日志
      if (isRetry) {
        console.info(`SSE 请求重试成功（第 ${attempt} 次重试）`);
      }

      // 检查 HTTP 响应状态
      if (!response.ok) {
        const errorText = await response.text().catch(() => '未知错误');
        // HTTP 错误不重试，直接抛出
        throw new Error(`服务端错误（HTTP ${response.status}）：${errorText}`);
      }

      const reader = response.body?.getReader();
      if (!reader) {
        throw new Error('响应体不可读');
      }

      const decoder = new TextDecoder('utf-8');
      let buffer = '';

      // 逐块读取流数据
      while (true) {
        const { done, value } = await reader.read();

        if (done) {
          // 流结束，处理缓冲区中剩余的数据
          if (buffer.trim()) {
            processSSELine(buffer.trim(), onTextChunk, onComponentChunk);
          }
          break;
        }

        // 将二进制数据解码为文本，追加到缓冲区
        buffer += decoder.decode(value, { stream: true });

        // 按换行符分割，逐行处理 SSE 数据
        const lines = buffer.split('\n');
        // 最后一个元素可能是不完整的行，保留在缓冲区中
        buffer = lines.pop() || '';

        for (const line of lines) {
          const trimmed = line.trim();
          if (!trimmed) continue;
          processSSELine(trimmed, onTextChunk, onComponentChunk);
        }
      }

      // 流正常读取完毕
      receivedDone = true;
      return 'done';
    } catch (error: unknown) {
      // 如果是用户主动中断，不触发错误回调
      if (error instanceof DOMException && error.name === 'AbortError') {
        receivedDone = true;
        return 'done';
      }

      // 网络错误（TypeError）且未超过重试次数，允许重试
      if (
        error instanceof TypeError &&
        attempt < MAX_RETRY_COUNT
      ) {
        console.warn(`SSE 请求网络错误，正在重试（${attempt + 1}/${MAX_RETRY_COUNT}）...`, error);
        return 'retry';
      }

      throw error;
    }
  }

  try {
    let attempt = 0;
    while (true) {
      const result = await execute(attempt);
      if (result === 'done') {
        onDone();
        return;
      }
      // 需要重试
      attempt++;
    }
  } catch (error: unknown) {
    onError(error instanceof Error ? error : new Error(String(error)));
  }
}

/**
 * 处理单行 SSE 数据
 * 协议格式：
 *   data:[TEXT]文本内容       → 文本块
 *   data:[COMPONENT]{...json} → 组件块
 *   data:[DONE]              → 流结束标记
 */
function processSSELine(
  line: string,
  onTextChunk: (text: string) => void,
  onComponentChunk: (json: { componentName: string; props: Record<string, unknown> }) => void,
): void {
  // 只处理以 "data:" 开头的行
  if (!line.startsWith('data:')) return;

  const payload = line.slice(5); // 去掉 "data:" 前缀

  // [DONE] 标记 - 流结束
  if (payload === '[DONE]') return;

  // [TEXT] 文本块
  if (payload.startsWith('[TEXT]')) {
    const text = payload.slice(6);
    onTextChunk(text);
    return;
  }

  // [COMPONENT] 组件块
  if (payload.startsWith('[COMPONENT]')) {
    const jsonStr = payload.slice(11);
    try {
      const parsed = JSON.parse(jsonStr) as {
        componentName: string;
        props: Record<string, unknown>;
      };
      onComponentChunk(parsed);
    } catch (e) {
      console.error('解析组件数据失败:', e, jsonStr);
    }
    return;
  }

  // 兼容：无前缀的纯文本数据
  if (payload.trim()) {
    onTextChunk(payload);
  }
}

package com.datamonitor.service.llm;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * LLM 客户端接口
 * <p>
 * 定义与 LLM API 的交互方式，支持流式响应和 Function Calling
 *
 * @author zhoulu
 * @since 2026-04-23
 */
public interface LlmClient {

    /**
     * 流式调用 LLM
     *
     * @param messages       OpenAI 格式的消息列表 [{"role":"system","content":"..."}, ...]
     * @param tools          OpenAI 格式的工具定义列表（Function Calling），可为 null
     * @param onContent      收到文本内容 delta 时回调
     * @param onFunctionCall 收到 function_call 时回调（返回 function name + arguments JSON）
     * @return 完整的 assistant 回复文本
     * @throws IOException IO 异常
     */
    String streamChat(List<Map<String, Object>> messages,
                      List<Map<String, Object>> tools,
                      Consumer<String> onContent,
                      Consumer<Map<String, String>> onFunctionCall) throws IOException;
}

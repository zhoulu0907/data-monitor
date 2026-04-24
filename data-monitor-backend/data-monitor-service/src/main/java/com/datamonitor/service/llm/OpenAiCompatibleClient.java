package com.datamonitor.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.BufferedSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * OpenAI 兼容 LLM 客户端
 * <p>
 * 使用 OkHttp 调用 LLM API，流式读取 SSE 响应。
 * 兼容 OpenAI Chat Completions API 格式。
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiCompatibleClient implements LlmClient {

    private final OkHttpClient okHttpClient;
    private final LlmProperties llmProperties;
    private final ObjectMapper objectMapper;

    @Override
    public String streamChat(List<Map<String, Object>> messages,
                             List<Map<String, Object>> tools,
                             Consumer<String> onContent,
                             Consumer<Map<String, String>> onFunctionCall) throws IOException {
        // 构建请求体
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", llmProperties.getModel());
        requestBody.put("stream", true);
        requestBody.put("max_tokens", llmProperties.getMaxTokens());
        requestBody.put("temperature", llmProperties.getTemperature());

        // 添加 messages
        requestBody.set("messages", objectMapper.valueToTree(messages));

        // 添加 tools（Function Calling）
        if (tools != null && !tools.isEmpty()) {
            requestBody.set("tools", objectMapper.valueToTree(tools));
        }

        String jsonBody = objectMapper.writeValueAsString(requestBody);
        log.info("LLM 请求: model={}, messages数={}, tools数={}", llmProperties.getModel(), messages.size(),
                tools != null ? tools.size() : 0);

        // 构建 HTTP 请求
        Request request = new Request.Builder()
                .url(llmProperties.getApiUrl())
                .addHeader("Authorization", "Bearer " + llmProperties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        // 发起请求并处理流式响应
        StringBuilder fullContent = new StringBuilder();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "无响应体";
                throw new IOException("LLM API 调用失败，状态码: " + response.code() + "，响应: " + errorBody);
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("LLM API 返回空响应体");
            }

            BufferedSource source = body.source();
            StringBuilder argumentsBuffer = new StringBuilder();
            String currentFunctionName = null;

            while (!source.exhausted()) {
                String line = source.readUtf8Line();
                if (line == null || line.isBlank()) {
                    continue;
                }

                // 跳过 SSE 注释行
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                // 解析 SSE data 行
                if (line.startsWith("data: ")) {
                    String data = line.substring(6).trim();

                    // 检查结束标记
                    if ("[DONE]".equals(data)) {
                        break;
                    }

                    try {
                        JsonNode chunk = objectMapper.readTree(data);
                        JsonNode choices = chunk.get("choices");
                        if (choices == null || choices.isEmpty()) {
                            continue;
                        }

                        JsonNode delta = choices.get(0).get("delta");
                        if (delta == null) {
                            continue;
                        }

                        // 处理文本内容
                        JsonNode contentNode = delta.get("content");
                        if (contentNode != null && !contentNode.isNull() && !contentNode.asText().isEmpty()) {
                            String content = contentNode.asText();
                            fullContent.append(content);
                            if (onContent != null) {
                                onContent.accept(content);
                            }
                        }

                        // 处理 reasoning 内容（思考过程，不转发给前端）
                        JsonNode reasoningNode = delta.get("reasoning");
                        if (reasoningNode != null && !reasoningNode.isNull()) {
                            // 模型思考过程，跳过不处理
                        }

                        // 处理 tool_calls（Function Calling）
                        JsonNode toolCallsNode = delta.get("tool_calls");
                        if (toolCallsNode != null && !toolCallsNode.isNull()) {
                            for (JsonNode toolCall : toolCallsNode) {
                                JsonNode functionNode = toolCall.get("function");
                                if (functionNode == null) {
                                    continue;
                                }

                                // 获取函数名
                                JsonNode nameNode = functionNode.get("name");
                                if (nameNode != null && !nameNode.isNull()) {
                                    // 新函数名：如果之前有累积的函数，先触发回调
                                    if (currentFunctionName != null && !currentFunctionName.equals(nameNode.asText())
                                            && argumentsBuffer.length() > 0 && onFunctionCall != null) {
                                        onFunctionCall.accept(Map.of(
                                                "name", currentFunctionName,
                                                "arguments", argumentsBuffer.toString()
                                        ));
                                    }
                                    // 重置缓冲区，开始新函数
                                    currentFunctionName = nameNode.asText();
                                    argumentsBuffer.setLength(0);
                                }

                                // 累积参数片段
                                JsonNode argsNode = functionNode.get("arguments");
                                if (argsNode != null && !argsNode.isNull()) {
                                    argumentsBuffer.append(argsNode.asText());
                                }
                            }
                        }

                        // 检查 finish_reason：当为 tool_calls 或 stop 时触发回调
                        // 兼容两种格式：
                        //   1. OpenAI 标准格式：tool_calls 和 finish_reason 在同一个 delta
                        //   2. minimax 格式：tool_calls 在前一个 delta，finish_reason 在单独的 delta
                        JsonNode finishReason = choices.get(0).get("finish_reason");
                        if (finishReason != null && !finishReason.isNull()
                                && ("tool_calls".equals(finishReason.asText()) || "stop".equals(finishReason.asText()))) {
                            log.info("LLM finish_reason={}, functionName={}, argsLength={}",
                                    finishReason.asText(), currentFunctionName, argumentsBuffer.length());
                            // 回调 function call
                            if (currentFunctionName != null && onFunctionCall != null) {
                                onFunctionCall.accept(Map.of(
                                        "name", currentFunctionName,
                                        "arguments", argumentsBuffer.toString()
                                ));
                            }
                            // 重置缓冲区
                            argumentsBuffer.setLength(0);
                            currentFunctionName = null;
                        }
                    } catch (Exception e) {
                        log.warn("解析 SSE chunk 失败: {}", line, e);
                    }
                }
            }
        }

        return fullContent.toString();
    }
}

package com.datamonitor.service.impl;

import com.datamonitor.entity.ChatMessageEntity;
import com.datamonitor.entity.ChatSessionEntity;
import com.datamonitor.mapper.ChatMessageMapper;
import com.datamonitor.mapper.ChatSessionMapper;
import com.datamonitor.service.ChatService;
import com.datamonitor.service.llm.DataQueryFunction;
import com.datamonitor.service.llm.LlmClient;
import com.datamonitor.service.llm.SystemPromptBuilder;
import com.datamonitor.vo.ChatMessageVO;
import com.datamonitor.vo.ChatSessionVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.datamonitor.entity.table.ChatMessageEntityTableDef.CHAT_MESSAGE_ENTITY;
import static com.datamonitor.entity.table.ChatSessionEntityTableDef.CHAT_SESSION_ENTITY;

/**
 * 聊天会话服务实现
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final LlmClient llmClient;
    private final SystemPromptBuilder promptBuilder;
    private final List<DataQueryFunction> queryFunctions;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ChatSessionVO createSession(String title) {
        ChatSessionEntity entity = new ChatSessionEntity();
        entity.setId(UUID.randomUUID().toString().replace("-", ""));
        entity.setTitle(title != null ? title : "新对话");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        chatSessionMapper.insert(entity);
        return toSessionVO(entity);
    }

    @Override
    public List<ChatSessionVO> listSessions() {
        QueryWrapper query = QueryWrapper.create()
                .from(CHAT_SESSION_ENTITY)
                .orderBy(CHAT_SESSION_ENTITY.UPDATED_AT.desc());
        List<ChatSessionEntity> entities = chatSessionMapper.selectListByQuery(query);
        return entities.stream().map(this::toSessionVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSession(String sessionId) {
        // 先删消息
        QueryWrapper messageQuery = QueryWrapper.create()
                .from(CHAT_MESSAGE_ENTITY)
                .where(CHAT_MESSAGE_ENTITY.SESSION_ID.eq(sessionId));
        chatMessageMapper.deleteByQuery(messageQuery);
        // 再删会话
        chatSessionMapper.deleteById(sessionId);
    }

    @Override
    public List<ChatMessageVO> listMessages(String sessionId) {
        QueryWrapper query = QueryWrapper.create()
                .from(CHAT_MESSAGE_ENTITY)
                .where(CHAT_MESSAGE_ENTITY.SESSION_ID.eq(sessionId))
                .orderBy(CHAT_MESSAGE_ENTITY.CREATED_AT.asc());
        List<ChatMessageEntity> entities = chatMessageMapper.selectListByQuery(query);
        return entities.stream().map(this::toMessageVO).collect(Collectors.toList());
    }

    @Override
    public void saveMessage(String sessionId, String role, String content, String componentData) {
        // 插入消息
        ChatMessageEntity message = new ChatMessageEntity();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setComponentData(componentData);
        message.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(message);

        // 更新会话的 updatedAt
        ChatSessionEntity session = chatSessionMapper.selectOneById(sessionId);
        if (session != null) {
            session.setUpdatedAt(LocalDateTime.now());
            chatSessionMapper.update(session);
        }
    }

    // ================================ SSE 流式处理 ================================

    @Override
    public void processChatStream(SseEmitter emitter, String sessionId, String message) {
        CompletableFuture.runAsync(() -> {
            StringBuilder fullContent = new StringBuilder();
            StringBuilder fullComponentData = new StringBuilder();

            try {
                // 1. 查找或创建会话
                String actualSessionId = ensureSession(sessionId, message);

                // 2. 保存用户消息
                saveMessage(actualSessionId, "user", message, null);

                // 3. 构建 messages 数组
                List<Map<String, Object>> messages = new ArrayList<>();

                // 系统提示词
                Map<String, Object> systemMsg = new HashMap<>();
                systemMsg.put("role", "system");
                systemMsg.put("content", promptBuilder.build());
                messages.add(systemMsg);

                // 加载历史消息（最近10条，避免 token 过多）
                List<ChatMessageVO> historyMessages = listMessages(actualSessionId);
                int startIndex = Math.max(0, historyMessages.size() - 10);
                for (int i = startIndex; i < historyMessages.size() - 1; i++) {
                    // 排除最后一条（即刚保存的用户消息）
                    ChatMessageVO historyMsg = historyMessages.get(i);
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("role", historyMsg.getRole());
                    msg.put("content", historyMsg.getContent());
                    messages.add(msg);
                }

                // 当前用户消息
                Map<String, Object> userMsg = new HashMap<>();
                userMsg.put("role", "user");
                userMsg.put("content", message);
                messages.add(userMsg);

                // 4. 构建 tools 数组
                List<Map<String, Object>> tools = buildToolDefinitions();

                // 5. 循环调用 LLM（支持多轮 Function Calling）
                int maxRounds = 3;
                // 累积本轮 LLM 输出的文本，用于在流结束后统一解析 [COMPONENT] 标记
                StringBuilder roundContent = new StringBuilder();
                for (int round = 0; round < maxRounds; round++) {
                    log.info("LLM 调用轮次 {}/{}, messages数={}", round + 1, maxRounds, messages.size());
                    // 记录本轮是否有 function call
                    boolean[] hasFunctionCall = {false};
                    roundContent.setLength(0);

                    String assistantReply = llmClient.streamChat(
                            messages,
                            round == 0 ? tools : null, // 仅第一轮发送 tools 定义
                            // onContent 回调：累积文本，直接转发纯文本给前端（打字机效果）
                            content -> {
                                roundContent.append(content);
                                fullContent.append(content);
                                // 直接作为文本转发给前端
                                try {
                                    emitter.send(SseEmitter.event().data("[TEXT]" + content));
                                } catch (IOException e) {
                                    log.warn("发送 SSE 文本事件失败", e);
                                }
                            },
                            // onFunctionCall 回调：执行函数查询
                            functionCall -> {
                                hasFunctionCall[0] = true;
                                String funcName = functionCall.get("name");
                                String funcArgs = functionCall.get("arguments");

                                log.info("Function Calling: name={}, arguments={}", funcName, funcArgs);

                                // 查找并执行对应的函数
                                DataQueryFunction func = findFunction(funcName);
                                if (func != null) {
                                    try {
                                        // 解析参数
                                        Map<String, Object> params = parseArguments(funcArgs);

                                        // 执行查询
                                        String result = func.execute(params);
                                        log.debug("Function {} 执行结果: {}", funcName, result);

                                        // 将 function call 和结果追加到 messages
                                        Map<String, Object> toolCallMsg = new HashMap<>();
                                        toolCallMsg.put("role", "assistant");
                                        toolCallMsg.put("content", null);

                                        List<Map<String, Object>> toolCalls = new ArrayList<>();
                                        Map<String, Object> toolCall = new HashMap<>();
                                        toolCall.put("id", "call_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
                                        toolCall.put("type", "function");
                                        Map<String, Object> function = new HashMap<>();
                                        function.put("name", funcName);
                                        function.put("arguments", funcArgs);
                                        toolCall.put("function", function);
                                        toolCalls.add(toolCall);
                                        toolCallMsg.put("tool_calls", toolCalls);
                                        messages.add(toolCallMsg);

                                        // 添加 tool 结果消息
                                        Map<String, Object> toolResultMsg = new HashMap<>();
                                        toolResultMsg.put("role", "tool");
                                        toolResultMsg.put("tool_call_id", toolCall.get("id"));
                                        toolResultMsg.put("content", result);
                                        messages.add(toolResultMsg);
                                    } catch (Exception e) {
                                        log.error("执行 Function {} 失败", funcName, e);
                                    }
                                } else {
                                    log.warn("未找到 Function: {}", funcName);
                                }
                            }
                    );

                    // 如果没有 function call，跳出循环
                    if (!hasFunctionCall[0]) {
                        break;
                    }
                }

                // 6. 从累积的文本中解析 [COMPONENT] 标记，发送组件事件
                parseComponentTags(roundContent.toString(), emitter, fullContent, fullComponentData);

                // 6. 发送结束标记
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();

                // 7. 保存 AI 消息到数据库
                saveMessage(actualSessionId, "assistant",
                        fullContent.toString(),
                        fullComponentData.length() > 0 ? fullComponentData.toString() : null);

            } catch (Exception e) {
                log.error("LLM SSE 流式处理异常", e);
                try {
                    emitter.send(SseEmitter.event().data("[TEXT]抱歉，AI 服务暂时不可用，请稍后再试。"));
                    emitter.send(SseEmitter.event().data("[DONE]"));
                    emitter.complete();
                } catch (IOException ioException) {
                    emitter.completeWithError(ioException);
                }
            }
        });
    }

    // ================================ LLM 辅助方法 ================================

    /**
     * 从 queryFunctions 列表生成 OpenAI tools JSON 定义
     */
    private List<Map<String, Object>> buildToolDefinitions() {
        List<Map<String, Object>> tools = new ArrayList<>();
        for (DataQueryFunction func : queryFunctions) {
            Map<String, Object> tool = new HashMap<>();
            tool.put("type", "function");

            Map<String, Object> function = new HashMap<>();
            function.put("name", func.getName());
            function.put("description", func.getDescription());

            // 解析参数 JSON Schema
            try {
                Map<String, Object> params = objectMapper.readValue(
                        func.getParametersJson(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                function.put("parameters", params);
            } catch (Exception e) {
                log.warn("解析 Function {} 的参数定义失败", func.getName(), e);
                function.put("parameters", Map.of("type", "object", "properties", Map.of(), "required", List.of()));
            }

            tool.put("function", function);
            tools.add(tool);
        }
        return tools;
    }

    /**
     * 按名称查找 DataQueryFunction
     */
    private DataQueryFunction findFunction(String name) {
        if (name == null) {
            return null;
        }
        return queryFunctions.stream()
                .filter(f -> name.equals(f.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 解析 function calling 的 arguments JSON 字符串
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseArguments(String argumentsJson) {
        if (argumentsJson == null || argumentsJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(argumentsJson, Map.class);
        } catch (Exception e) {
            log.warn("解析 function arguments 失败: {}", argumentsJson, e);
            return Map.of();
        }
    }

    /**
     * 从 LLM 完整输出中解析 [COMPONENT] 标记，发送组件 SSE 事件
     * <p>
     * 由于 LLM 的 [COMPONENT]{...} 标记会被分片到多个 delta 中，
     * 无法在流式传输时实时解析，因此改为在流结束后统一解析。
     * <p>
     * 解析完成后，发送 [COMPONENT] SSE 事件给前端，并从 fullContent 中移除组件 JSON。
     */
    private void parseComponentTags(String content, SseEmitter emitter,
                                     StringBuilder fullContent, StringBuilder fullComponentData) {
        if (content == null || content.isEmpty()) {
            return;
        }

        String remaining = content;
        while (!remaining.isEmpty()) {
            int componentIdx = remaining.indexOf("[COMPONENT]");

            if (componentIdx == -1) {
                // 没有更多组件标记
                break;
            }

            // 提取 [COMPONENT] 后面的 JSON
            String afterComponent = remaining.substring(componentIdx + "[COMPONENT]".length());

            // 查找 JSON 边界
            int jsonEnd = findJsonEnd(afterComponent);
            if (jsonEnd > 0) {
                String componentJson = afterComponent.substring(0, jsonEnd);
                try {
                    emitter.send(SseEmitter.event().data("[COMPONENT]" + componentJson));
                    fullComponentData.append(componentJson);
                    log.info("发送 A2UI 组件: {}", componentJson.substring(0, Math.min(100, componentJson.length())));
                } catch (IOException e) {
                    log.warn("发送 SSE 组件事件失败", e);
                }
                // 从 fullContent 中移除 [COMPONENT]{...} 标记
                int startInFull = fullContent.indexOf("[COMPONENT]");
                if (startInFull >= 0) {
                    int endInFull = startInFull + "[COMPONENT]".length() + jsonEnd;
                    fullContent.delete(startInFull, endInFull);
                }
                remaining = afterComponent.substring(jsonEnd);
            } else {
                break;
            }
        }
    }

    /**
     * 查找 JSON 字符串的结束位置（匹配大括号）
     *
     * @param jsonStr 以 JSON 开头的字符串
     * @return JSON 结束位置的下一个索引，未找到完整 JSON 则返回 -1
     */
    private int findJsonEnd(String jsonStr) {
        if (jsonStr.isEmpty() || jsonStr.charAt(0) != '{') {
            return -1;
        }

        int depth = 0;
        boolean inString = false;
        char prevChar = 0;

        for (int i = 0; i < jsonStr.length(); i++) {
            char c = jsonStr.charAt(i);

            if (inString) {
                if (c == '"' && prevChar != '\\') {
                    inString = false;
                }
            } else {
                if (c == '"') {
                    inString = true;
                } else if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        return i + 1;
                    }
                }
            }
            prevChar = c;
        }

        return -1;
    }

    // ================================ 辅助方法 ================================

    /**
     * 确保会话存在，不存在则创建
     *
     * @param sessionId 会话ID，可为空
     * @param message   用户消息（用于自动设置标题）
     * @return 实际的会话ID
     */
    private String ensureSession(String sessionId, String message) {
        if (sessionId != null && !sessionId.isBlank()) {
            // 检查会话是否存在
            ChatSessionEntity existing = chatSessionMapper.selectOneById(sessionId);
            if (existing != null) {
                return sessionId;
            }
        }

        // 创建新会话
        String title = (message != null && message.length() > 20)
                ? message.substring(0, 20)
                : (message != null ? message : "新对话");
        ChatSessionVO session = createSession(title);
        return session.getId();
    }

    // ================================ Entity -> VO ================================

    private ChatSessionVO toSessionVO(ChatSessionEntity entity) {
        ChatSessionVO vo = new ChatSessionVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().format(FORMATTER) : null);
        vo.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().format(FORMATTER) : null);
        return vo;
    }

    private ChatMessageVO toMessageVO(ChatMessageEntity entity) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(entity.getId());
        vo.setSessionId(entity.getSessionId());
        vo.setRole(entity.getRole());
        vo.setContent(entity.getContent());
        vo.setComponentData(entity.getComponentData());
        vo.setTimestamp(entity.getCreatedAt() != null ? entity.getCreatedAt().format(FORMATTER) : null);
        return vo;
    }
}

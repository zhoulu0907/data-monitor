package com.datamonitor.service.impl;

import com.datamonitor.entity.ChatMessageEntity;
import com.datamonitor.entity.ChatSessionEntity;
import com.datamonitor.mapper.ChatMessageMapper;
import com.datamonitor.mapper.ChatSessionMapper;
import com.datamonitor.service.ChatService;
import com.datamonitor.vo.ChatMessageVO;
import com.datamonitor.vo.ChatSessionVO;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.datamonitor.entity.table.ChatMessageEntityTableDef.CHAT_MESSAGE_ENTITY;
import static com.datamonitor.entity.table.ChatSessionEntityTableDef.CHAT_SESSION_ENTITY;

/**
 * 聊天会话服务实现（基于 Spring AI Alibaba）
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
    private final ChatClient chatClient;

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
        QueryWrapper messageQuery = QueryWrapper.create()
                .from(CHAT_MESSAGE_ENTITY)
                .where(CHAT_MESSAGE_ENTITY.SESSION_ID.eq(sessionId));
        chatMessageMapper.deleteByQuery(messageQuery);
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
        ChatMessageEntity message = new ChatMessageEntity();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setComponentData(componentData);
        message.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(message);

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

                // 3. 构建历史消息上下文
                List<Message> chatMessages = buildMessageHistory(actualSessionId, message);

                // 4. 调用 Spring AI ChatClient（流式）
                Flux<ChatResponse> flux = chatClient.prompt()
                        .messages(chatMessages)
                        .toolNames(
                                "queryKpiSummary",
                                "queryRevenueMonthly",
                                "queryContractByIndustry",
                                "queryFunnelStages",
                                "queryResourceUsage",
                                "queryPersonnel",
                                "queryCollectionRate",
                                "queryBalanceSheet",
                                "queryCostDetail"
                        )
                        .stream()
                        .chatResponse();

                // 5. 订阅 Flux，将内容逐块发送给前端
                flux.subscribe(
                        response -> {
                            try {
                                if (response.getResult() != null
                                        && response.getResult().getOutput() != null
                                        && response.getResult().getOutput().getText() != null) {
                                    String content = response.getResult().getOutput().getText();
                                    fullContent.append(content);
                                    emitter.send(SseEmitter.event().data("[TEXT]" + content));
                                }
                            } catch (IOException e) {
                                log.warn("发送 SSE 文本事件失败", e);
                            }
                        },
                        error -> {
                            log.error("LLM 流式响应异常", error);
                            try {
                                emitter.send(SseEmitter.event().data("[TEXT]抱歉，AI 服务暂时不可用，请稍后再试。"));
                                emitter.send(SseEmitter.event().data("[DONE]"));
                                emitter.complete();
                            } catch (IOException ioException) {
                                emitter.completeWithError(ioException);
                            }
                        },
                        () -> {
                            try {
                                // 6. 流结束后，解析 [COMPONENT] 标记
                                parseComponentTags(fullContent.toString(), emitter, fullContent, fullComponentData);

                                // 7. 发送结束标记
                                emitter.send(SseEmitter.event().data("[DONE]"));
                                emitter.complete();

                                // 8. 保存 AI 消息到数据库
                                saveMessage(actualSessionId, "assistant",
                                        fullContent.toString(),
                                        fullComponentData.length() > 0 ? fullComponentData.toString() : null);
                            } catch (Exception e) {
                                log.error("SSE 流结束处理异常", e);
                                emitter.completeWithError(e);
                            }
                        }
                );

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

    // ================================ 辅助方法 ================================

    /**
     * 构建消息历史（最近10条 + 当前用户消息）
     */
    private List<Message> buildMessageHistory(String sessionId, String currentMessage) {
        List<Message> messages = new ArrayList<>();

        List<ChatMessageVO> historyMessages = listMessages(sessionId);
        int startIndex = Math.max(0, historyMessages.size() - 11); // 最近10条（排除刚保存的）
        for (int i = startIndex; i < historyMessages.size() - 1; i++) {
            ChatMessageVO historyMsg = historyMessages.get(i);
            if ("user".equals(historyMsg.getRole())) {
                messages.add(new UserMessage(historyMsg.getContent()));
            } else if ("assistant".equals(historyMsg.getRole())) {
                messages.add(new AssistantMessage(historyMsg.getContent()));
            }
        }

        // 当前用户消息
        messages.add(new UserMessage(currentMessage));

        return messages;
    }

    /**
     * 确保会话存在，不存在则创建
     */
    private String ensureSession(String sessionId, String message) {
        if (sessionId != null && !sessionId.isBlank()) {
            ChatSessionEntity existing = chatSessionMapper.selectOneById(sessionId);
            if (existing != null) {
                return sessionId;
            }
        }

        String title = (message != null && message.length() > 20)
                ? message.substring(0, 20)
                : (message != null ? message : "新对话");
        ChatSessionVO session = createSession(title);
        return session.getId();
    }

    /**
     * 从 LLM 完整输出中解析 [COMPONENT] 标记，发送组件 SSE 事件
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
                break;
            }

            String afterComponent = remaining.substring(componentIdx + "[COMPONENT]".length());
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

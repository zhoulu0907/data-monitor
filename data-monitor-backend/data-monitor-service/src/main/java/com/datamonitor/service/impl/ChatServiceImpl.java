package com.datamonitor.service.impl;

import com.datamonitor.entity.ChatMessageEntity;
import com.datamonitor.entity.ChatSessionEntity;
import com.datamonitor.mapper.ChatMessageMapper;
import com.datamonitor.mapper.ChatSessionMapper;
import com.datamonitor.service.ChatService;
import com.datamonitor.service.llm.DataQueryFunction;
import com.datamonitor.service.llm.LlmClient;
import com.datamonitor.service.llm.LlmProperties;
import com.datamonitor.service.llm.SystemPromptBuilder;
import com.datamonitor.vo.ChatMessageVO;
import com.datamonitor.vo.ChatSessionVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
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
    private final LlmProperties llmProperties;
    private final LlmClient llmClient;
    private final SystemPromptBuilder promptBuilder;
    private final List<DataQueryFunction> queryFunctions;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 当前统计年份 */
    private static final int CURRENT_YEAR = 2026;

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
        if (llmProperties.isEnabled()) {
            processLlmStream(emitter, sessionId, message);
        } else {
            processMockStream(emitter, sessionId, message);
        }
    }

    /**
     * Mock 模式 SSE 流式处理（原有逻辑）
     */
    private void processMockStream(SseEmitter emitter, String sessionId, String message) {
        CompletableFuture.runAsync(() -> {
            // 用于收集完整的 AI 回复文本和组件数据，便于持久化
            StringBuilder fullContent = new StringBuilder();
            StringBuilder fullComponentData = new StringBuilder();

            try {
                // 1. 查找或创建会话
                String actualSessionId = ensureSession(sessionId, message);

                // 2. 保存用户消息
                saveMessage(actualSessionId, "user", message, null);

                // 3. 根据关键词匹配构建 Mock 响应
                List<SseChunk> chunks = buildMockResponse(message);

                // 4. 逐块发送 SSE 事件（模拟打字机效果）
                for (SseChunk chunk : chunks) {
                    // 随机延迟 50~150ms，模拟打字效果
                    Thread.sleep(ThreadLocalRandom.current().nextLong(50, 150));

                    String sseData = chunk.toSseData();
                    emitter.send(SseEmitter.event().data(sseData));

                    // 收集完整回复用于持久化
                    if (chunk.type == SseChunkType.TEXT) {
                        fullContent.append(chunk.payload);
                    } else if (chunk.type == SseChunkType.COMPONENT) {
                        fullComponentData.append(chunk.payload);
                    }
                }

                // 5. 发送结束标记
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();

                // 6. 保存 AI 消息到数据库
                saveMessage(actualSessionId, "assistant",
                        fullContent.toString(),
                        fullComponentData.length() > 0 ? fullComponentData.toString() : null);

            } catch (Exception e) {
                log.error("Mock SSE 流式处理异常", e);
                emitter.completeWithError(e);
            }
        });
    }

    /**
     * LLM 模式 SSE 流式处理
     * <p>
     * 调用真实 LLM API，支持 Function Calling 和 A2UI 组件指令解析
     */
    private void processLlmStream(SseEmitter emitter, String sessionId, String message) {
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
                int maxRounds = llmProperties.getMaxFunctionCalls();
                for (int round = 0; round < maxRounds; round++) {
                    // 记录本轮是否有 function call
                    boolean[] hasFunctionCall = {false};

                    String assistantReply = llmClient.streamChat(
                            messages,
                            round == 0 ? tools : null, // 仅第一轮发送 tools 定义
                            // onContent 回调：解析并转发文本和组件标记
                            content -> {
                                parseAndForwardContent(emitter, content, fullContent, fullComponentData);
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
     * 解析 LLM 输出内容中的 [COMPONENT] 标记，转发 SSE 事件
     * <p>
     * LLM 输出格式：
     * - 普通文本直接以 [TEXT] 前缀发送
     * - [COMPONENT]{...} 以 [COMPONENT] 前缀发送
     */
    private void parseAndForwardContent(SseEmitter emitter, String content,
                                         StringBuilder fullContent, StringBuilder fullComponentData) {
        if (content == null || content.isEmpty()) {
            return;
        }

        // 处理内容中的 [COMPONENT] 标记
        String remaining = content;
        while (!remaining.isEmpty()) {
            int componentIdx = remaining.indexOf("[COMPONENT]");

            if (componentIdx == -1) {
                // 没有组件标记，整段作为文本发送
                try {
                    emitter.send(SseEmitter.event().data("[TEXT]" + remaining));
                    fullContent.append(remaining);
                } catch (IOException e) {
                    log.warn("发送 SSE 文本事件失败", e);
                }
                break;
            }

            // [COMPONENT] 之前有文本
            if (componentIdx > 0) {
                String textBefore = remaining.substring(0, componentIdx);
                try {
                    emitter.send(SseEmitter.event().data("[TEXT]" + textBefore));
                    fullContent.append(textBefore);
                } catch (IOException e) {
                    log.warn("发送 SSE 文本事件失败", e);
                }
            }

            // 提取 [COMPONENT] 后面的 JSON
            String afterComponent = remaining.substring(componentIdx + "[COMPONENT]".length());

            // 查找 JSON 边界：匹配第一个完整的 { }
            int jsonEnd = findJsonEnd(afterComponent);
            if (jsonEnd > 0) {
                String componentJson = afterComponent.substring(0, jsonEnd);
                try {
                    emitter.send(SseEmitter.event().data("[COMPONENT]" + componentJson));
                    fullComponentData.append(componentJson);
                } catch (IOException e) {
                    log.warn("发送 SSE 组件事件失败", e);
                }
                remaining = afterComponent.substring(jsonEnd);
            } else {
                // JSON 不完整，暂时保留标记等待后续内容
                // 将 [COMPONENT] 作为文本发送
                try {
                    emitter.send(SseEmitter.event().data("[TEXT]" + remaining));
                    fullContent.append(remaining);
                } catch (IOException e) {
                    log.warn("发送 SSE 文本事件失败", e);
                }
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

    // ================================ Mock 响应构建 ================================

    /**
     * 根据用户消息关键词匹配，构建 Mock SSE 响应块列表
     */
    private List<SseChunk> buildMockResponse(String userMessage) {
        if (userMessage == null) {
            userMessage = "";
        }
        String msg = userMessage.toLowerCase();

        if (msg.contains("营收") || msg.contains("收入")) {
            return buildRevenueResponse();
        } else if (msg.contains("成本")) {
            return buildCostResponse();
        } else if (msg.contains("漏斗") || msg.contains("商机")) {
            return buildFunnelResponse();
        } else if (msg.contains("行业")) {
            return buildIndustryResponse();
        } else if (msg.contains("资源")) {
            return buildResourceResponse();
        } else if (msg.contains("人员") || msg.contains("员工")) {
            return buildPersonnelResponse();
        } else if (msg.contains("kpi") || msg.contains("指标")) {
            return buildKpiResponse();
        } else if (msg.contains("合同")) {
            return buildContractResponse();
        } else {
            return buildDefaultResponse();
        }
    }

    /**
     * 营收趋势 Mock 响应
     */
    private List<SseChunk> buildRevenueResponse() {
        List<SseChunk> chunks = new ArrayList<>();

        // 查询月度营收数据
        List<Row> rows = Db.selectListBySql(
                "SELECT month, SUM(amount) as revenue FROM revenue_detail " +
                        "WHERE month LIKE '" + CURRENT_YEAR + "%' GROUP BY month ORDER BY month");

        // 查询年度总额
        BigDecimal annualTotal = queryBigDecimal(
                "SELECT COALESCE(SUM(amount),0) FROM revenue_detail WHERE month LIKE '" + CURRENT_YEAR + "%'");

        // 查询回款率
        BigDecimal collectionRate = queryBigDecimal(
                "SELECT CASE WHEN SUM(plan_amount) = 0 THEN 0 " +
                        "ELSE ROUND(SUM(COALESCE(actual_amount,0)) / SUM(plan_amount) * 100, 1) END " +
                        "FROM contract_payment WHERE EXTRACT(YEAR FROM plan_date) = " + CURRENT_YEAR);

        chunks.add(SseChunk.text("以下是" + (CURRENT_YEAR - 2) + "-" + CURRENT_YEAR + "年营收趋势分析：\n\n"));
        chunks.add(SseChunk.text(CURRENT_YEAR + "年年度合同额目标" + annualTotal.setScale(2, RoundingMode.HALF_UP) +
                "万元，截至目前回款率" + collectionRate + "%。\n\n"));

        // 构建 RevenueTrendChart 组件数据
        StringBuilder dataJson = new StringBuilder();
        dataJson.append("{\"componentName\":\"RevenueTrendChart\",\"props\":{\"title\":\"营收月度趋势\",\"data\":[");
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (i > 0) {
                dataJson.append(",");
            }
            String month = row.getString("month");
            BigDecimal revenue = row.getBigDecimal("revenue");
            dataJson.append("{\"month\":\"").append(month).append("\"")
                    .append(",\"revenue\":").append(revenue.setScale(2, RoundingMode.HALF_UP))
                    .append("}");
        }
        dataJson.append("]}}");

        chunks.add(SseChunk.component(dataJson.toString()));
        chunks.add(SseChunk.text("\n\n以上数据来源于 revenue_detail 表的月度汇总。"));

        return chunks;
    }

    /**
     * 成本分析 Mock 响应
     */
    private List<SseChunk> buildCostResponse() {
        List<SseChunk> chunks = new ArrayList<>();

        // 查询月度成本明细
        List<Row> rows = Db.selectListBySql(
                "SELECT month, SUM(amount) as cost FROM cost_detail " +
                        "WHERE month LIKE '" + CURRENT_YEAR + "%' GROUP BY month ORDER BY month");

        BigDecimal totalCost = queryBigDecimal(
                "SELECT COALESCE(SUM(amount),0) FROM cost_detail WHERE month LIKE '" + CURRENT_YEAR + "%'");

        chunks.add(SseChunk.text("以下是" + CURRENT_YEAR + "年成本穿透分析：\n\n"));
        chunks.add(SseChunk.text("年度总成本为" + totalCost.setScale(2, RoundingMode.HALF_UP) + "万元。\n\n"));

        // 构建 BarChart 组件数据（成本趋势）
        StringBuilder dataJson = new StringBuilder();
        dataJson.append("{\"componentName\":\"BarChart\",\"props\":{\"title\":\"月度成本趋势\",\"data\":[");
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (i > 0) {
                dataJson.append(",");
            }
            String month = row.getString("month");
            BigDecimal cost = row.getBigDecimal("cost");
            dataJson.append("{\"month\":\"").append(month).append("\"")
                    .append(",\"value\":").append(cost.setScale(2, RoundingMode.HALF_UP))
                    .append("}");
        }
        dataJson.append("]}}");

        chunks.add(SseChunk.component(dataJson.toString()));
        chunks.add(SseChunk.text("\n\n以上数据来源于 cost_detail 表的月度汇总。"));

        return chunks;
    }

    /**
     * 商机漏斗 Mock 响应
     */
    private List<SseChunk> buildFunnelResponse() {
        List<SseChunk> chunks = new ArrayList<>();

        String[] stages = {"线索", "商机", "方案", "招投标", "签约"};
        String[] stageKeys = {"lead", "opportunity", "proposal", "bidding", "signed"};

        BigDecimal totalPipeline = queryBigDecimal(
                "SELECT COALESCE(SUM(amount),0) FROM opportunity WHERE status != 'lost'");

        chunks.add(SseChunk.text("以下是当前商机漏斗分析：\n\n"));
        chunks.add(SseChunk.text("总管线金额为" + totalPipeline.setScale(2, RoundingMode.HALF_UP) + "万元。\n\n"));

        // 构建 FunnelChart 组件数据
        StringBuilder dataJson = new StringBuilder();
        dataJson.append("{\"componentName\":\"FunnelChart\",\"props\":{\"title\":\"商机漏斗\",\"data\":[");
        for (int i = 0; i < stages.length; i++) {
            BigDecimal stageTotal = queryBigDecimal(
                    "SELECT COALESCE(SUM(amount),0) FROM opportunity WHERE stage = '" +
                            stageKeys[i] + "' AND status != 'lost'");
            if (i > 0) {
                dataJson.append(",");
            }
            dataJson.append("{\"stage\":\"").append(stages[i]).append("\"")
                    .append(",\"value\":").append(stageTotal.setScale(2, RoundingMode.HALF_UP))
                    .append("}");
        }
        dataJson.append("]}}");

        chunks.add(SseChunk.component(dataJson.toString()));
        chunks.add(SseChunk.text("\n\n以上数据来源于 opportunity 表的阶段汇总。"));

        return chunks;
    }

    /**
     * 行业分布 Mock 响应
     */
    private List<SseChunk> buildIndustryResponse() {
        List<SseChunk> chunks = new ArrayList<>();

        // 查询行业分布数据
        List<Row> rows = Db.selectListBySql(
                "SELECT industry, SUM(amount) as total FROM contract " +
                        "WHERE EXTRACT(YEAR FROM sign_date) = " + CURRENT_YEAR +
                        " GROUP BY industry ORDER BY total DESC");

        chunks.add(SseChunk.text("以下是" + CURRENT_YEAR + "年行业分布分析：\n\n"));

        // 构建 PieChart 组件数据
        String[] colors = {"#1890ff", "#00f3ff", "#10b981", "#f59e0b", "#8b5cf6", "#ec4899", "#06b6d4", "#6b7280"};

        StringBuilder dataJson = new StringBuilder();
        dataJson.append("{\"componentName\":\"PieChart\",\"props\":{\"title\":\"行业分布\",\"data\":[");
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (i > 0) {
                dataJson.append(",");
            }
            String industry = row.getString("industry");
            BigDecimal total = row.getBigDecimal("total");
            dataJson.append("{\"name\":\"").append(industry != null ? industry : "未知").append("\"")
                    .append(",\"value\":").append(total.setScale(2, RoundingMode.HALF_UP))
                    .append(",\"color\":\"").append(colors[i % colors.length]).append("\"")
                    .append("}");
        }
        dataJson.append("]}}");

        chunks.add(SseChunk.component(dataJson.toString()));
        chunks.add(SseChunk.text("\n\n以上数据来源于 contract 表的行业汇总。"));

        return chunks;
    }

    /**
     * 资源使用 Mock 响应
     */
    private List<SseChunk> buildResourceResponse() {
        List<SseChunk> chunks = new ArrayList<>();

        // 查询最新月份的资源使用数据
        List<Row> rows = Db.selectListBySql(
                "SELECT resource_type, utilization_pct, target_pct, SUM(cost) as total_cost " +
                        "FROM resource_usage GROUP BY resource_type, utilization_pct, target_pct");

        chunks.add(SseChunk.text("以下是当前资源使用情况分析：\n\n"));

        // 构建 BarChart 组件数据
        StringBuilder dataJson = new StringBuilder();
        dataJson.append("{\"componentName\":\"BarChart\",\"props\":{\"title\":\"资源利用率\",\"data\":[");

        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (i > 0) {
                dataJson.append(",");
            }
            String type = mapResourceType(row.getString("resource_type"));
            int utilization = row.getInt("utilization_pct");
            int target = row.getInt("target_pct");
            dataJson.append("{\"name\":\"").append(type).append("\"")
                    .append(",\"current\":").append(utilization)
                    .append(",\"target\":").append(target)
                    .append("}");
        }
        dataJson.append("]}}");

        chunks.add(SseChunk.component(dataJson.toString()));
        chunks.add(SseChunk.text("\n\n以上数据来源于 resource_usage 表。"));

        return chunks;
    }

    /**
     * 人员数据 Mock 响应
     */
    private List<SseChunk> buildPersonnelResponse() {
        List<SseChunk> chunks = new ArrayList<>();

        // 查询人员总数及分类
        BigDecimal totalHeadcount = queryBigDecimal(
                "SELECT COUNT(*) FROM employee WHERE status = 'active'");
        BigDecimal internalStaff = queryBigDecimal(
                "SELECT COUNT(*) FROM employee WHERE status = 'active' AND type = 'internal'");
        BigDecimal outsourcedStaff = queryBigDecimal(
                "SELECT COUNT(*) FROM employee WHERE status = 'active' AND type = 'outsourced'");
        BigDecimal avgMonthlyCost = queryBigDecimal(
                "SELECT COALESCE(AVG(monthly_cost),0) FROM employee WHERE status = 'active'");

        // 查询月度人员饱和度
        List<Row> rows = Db.selectListBySql(
                "SELECT month, COUNT(DISTINCT employee_id) as headcount, ROUND(AVG(allocation_rate)) as rate " +
                        "FROM project_assignment GROUP BY month ORDER BY month");

        chunks.add(SseChunk.text("以下是当前人员配置分析：\n\n"));
        chunks.add(SseChunk.text("在职员工" + totalHeadcount.intValue() + "人（内部" +
                internalStaff.intValue() + "人，外包" + outsourcedStaff.intValue() +
                "人），人均月成本" + avgMonthlyCost.setScale(1, RoundingMode.HALF_UP) + "万元。\n\n"));

        // 构建 LineChart 组件数据
        StringBuilder dataJson = new StringBuilder();
        dataJson.append("{\"componentName\":\"LineChart\",\"props\":{\"title\":\"人员饱和度趋势\",\"data\":[");
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (i > 0) {
                dataJson.append(",");
            }
            String month = row.getString("month");
            int rate = row.getInt("rate");
            int headcount = row.getInt("headcount");
            dataJson.append("{\"month\":\"").append(month).append("\"")
                    .append(",\"rate\":").append(rate)
                    .append(",\"headcount\":").append(headcount)
                    .append("}");
        }
        dataJson.append("]}}");

        chunks.add(SseChunk.component(dataJson.toString()));
        chunks.add(SseChunk.text("\n\n以上数据来源于 employee 和 project_assignment 表。"));

        return chunks;
    }

    /**
     * KPI 指标 Mock 响应
     */
    private List<SseChunk> buildKpiResponse() {
        List<SseChunk> chunks = new ArrayList<>();

        // 年度合同额
        BigDecimal contractCur = queryBigDecimal(
                "SELECT COALESCE(SUM(amount),0) FROM contract WHERE EXTRACT(YEAR FROM sign_date) = " + CURRENT_YEAR);
        // 营业收入
        BigDecimal revenueCur = queryBigDecimal(
                "SELECT COALESCE(SUM(amount),0) FROM revenue_detail WHERE month LIKE '" + CURRENT_YEAR + "%'");
        // 总成本
        BigDecimal costCur = queryBigDecimal(
                "SELECT COALESCE(SUM(amount),0) FROM cost_detail WHERE month LIKE '" + CURRENT_YEAR + "%'");
        // 净收入
        BigDecimal netIncome = revenueCur.subtract(costCur);
        // 回款率
        BigDecimal collectionRate = queryBigDecimal(
                "SELECT CASE WHEN SUM(plan_amount) = 0 THEN 0 " +
                        "ELSE ROUND(SUM(COALESCE(actual_amount,0)) / SUM(plan_amount) * 100, 1) END " +
                        "FROM contract_payment WHERE EXTRACT(YEAR FROM plan_date) = " + CURRENT_YEAR);
        // 人均产值
        BigDecimal headcount = queryBigDecimal(
                "SELECT COUNT(*) FROM employee WHERE status = 'active'");
        BigDecimal perCapita = headcount.intValue() > 0
                ? revenueCur.divide(headcount, 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        chunks.add(SseChunk.text("以下是" + CURRENT_YEAR + "年核心经营指标：\n\n"));

        // 构建 KpiCard 组件数据
        StringBuilder dataJson = new StringBuilder();
        dataJson.append("{\"componentName\":\"KpiCard\",\"props\":{\"title\":\"核心KPI指标\",\"data\":[");
        dataJson.append("{\"label\":\"年度合同额\",\"value\":").append(contractCur.setScale(2, RoundingMode.HALF_UP)).append(",\"unit\":\"万元\"},");
        dataJson.append("{\"label\":\"营业收入\",\"value\":").append(revenueCur.setScale(2, RoundingMode.HALF_UP)).append(",\"unit\":\"万元\"},");
        dataJson.append("{\"label\":\"净收入\",\"value\":").append(netIncome.setScale(2, RoundingMode.HALF_UP)).append(",\"unit\":\"万元\"},");
        dataJson.append("{\"label\":\"回款率\",\"value\":").append(collectionRate).append(",\"unit\":\"%\"},");
        dataJson.append("{\"label\":\"人均产值\",\"value\":").append(perCapita).append(",\"unit\":\"万元/人\"}");
        dataJson.append("]}}");

        chunks.add(SseChunk.component(dataJson.toString()));
        chunks.add(SseChunk.text("\n\n以上数据来源于合同、营收、成本、回款等多表聚合。"));

        return chunks;
    }

    /**
     * 合同数据 Mock 响应
     */
    private List<SseChunk> buildContractResponse() {
        List<SseChunk> chunks = new ArrayList<>();

        // 查询合同列表
        List<Row> rows = Db.selectListBySql(
                "SELECT c.contract_name, c.customer_name, c.amount, c.sign_date, c.status, " +
                        "COALESCE(SUM(cp.actual_amount),0) as paid_amount " +
                        "FROM contract c " +
                        "LEFT JOIN contract_payment cp ON cp.contract_id = c.id " +
                        "WHERE EXTRACT(YEAR FROM c.sign_date) = " + CURRENT_YEAR + " " +
                        "GROUP BY c.id, c.contract_name, c.customer_name, c.amount, c.sign_date, c.status " +
                        "ORDER BY c.sign_date DESC");

        BigDecimal totalAmount = queryBigDecimal(
                "SELECT COALESCE(SUM(amount),0) FROM contract WHERE EXTRACT(YEAR FROM sign_date) = " + CURRENT_YEAR);

        chunks.add(SseChunk.text("以下是" + CURRENT_YEAR + "年合同数据：\n\n"));
        chunks.add(SseChunk.text("年度合同总额" + totalAmount.setScale(2, RoundingMode.HALF_UP) + "万元，共" + rows.size() + "份合同。\n\n"));

        // 构建 Table 组件数据
        StringBuilder dataJson = new StringBuilder();
        dataJson.append("{\"componentName\":\"Table\",\"props\":{\"title\":\"合同明细\",\"columns\":[" +
                "{\"key\":\"contractName\",\"label\":\"合同名称\"}," +
                "{\"key\":\"customerName\",\"label\":\"客户\"}," +
                "{\"key\":\"amount\",\"label\":\"金额(万元)\"}," +
                "{\"key\":\"paidAmount\",\"label\":\"已回款(万元)\"}," +
                "{\"key\":\"signDate\",\"label\":\"签订日期\"}," +
                "{\"key\":\"status\",\"label\":\"状态\"}" +
                "],\"data\":[");

        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (i > 0) {
                dataJson.append(",");
            }
            dataJson.append("{\"contractName\":\"").append(escapeJson(row.getString("contract_name"))).append("\"")
                    .append(",\"customerName\":\"").append(escapeJson(row.getString("customer_name"))).append("\"")
                    .append(",\"amount\":\"").append(row.getBigDecimal("amount").setScale(2, RoundingMode.HALF_UP)).append("\"")
                    .append(",\"paidAmount\":\"").append(row.getBigDecimal("paid_amount").setScale(2, RoundingMode.HALF_UP)).append("\"")
                    .append(",\"signDate\":\"").append(row.getString("sign_date")).append("\"")
                    .append(",\"status\":\"").append(escapeJson(row.getString("status"))).append("\"")
                    .append("}");
        }
        dataJson.append("]}}");

        chunks.add(SseChunk.component(dataJson.toString()));
        chunks.add(SseChunk.text("\n\n以上数据来源于 contract 和 contract_payment 表。"));

        return chunks;
    }

    /**
     * 默认 Mock 响应（纯文本）
     */
    private List<SseChunk> buildDefaultResponse() {
        List<SseChunk> chunks = new ArrayList<>();
        chunks.add(SseChunk.text("您好！我是 AI 智策助手，可以帮您分析企业经营数据。\n\n"));
        chunks.add(SseChunk.text("您可以尝试以下问题：\n"));
        chunks.add(SseChunk.text("- 查询营收趋势\n"));
        chunks.add(SseChunk.text("- 分析成本构成\n"));
        chunks.add(SseChunk.text("- 查看商机漏斗\n"));
        chunks.add(SseChunk.text("- 分析行业分布\n"));
        chunks.add(SseChunk.text("- 查看资源使用情况\n"));
        chunks.add(SseChunk.text("- 查询人员数据\n"));
        chunks.add(SseChunk.text("- 查看KPI指标\n"));
        chunks.add(SseChunk.text("- 查询合同明细\n"));
        return chunks;
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

    /**
     * 执行 SQL 查询返回第一个 BigDecimal 值
     */
    private BigDecimal queryBigDecimal(String sql) {
        Row row = Db.selectOneBySql(sql);
        if (row == null || row.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Object value = row.values().iterator().next();
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        }
        return new BigDecimal(value.toString());
    }

    /**
     * JSON 字符串转义
     */
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 资源类型映射
     */
    private String mapResourceType(String type) {
        if (type == null) {
            return "未知";
        }
        return switch (type) {
            case "computing" -> "计算资源";
            case "storage" -> "存储资源";
            case "network" -> "网络资源";
            case "gpu" -> "GPU资源";
            case "memory" -> "内存资源";
            default -> type;
        };
    }

    // ================================ SSE 内部数据结构 ================================

    /**
     * SSE 数据块类型
     */
    private enum SseChunkType {
        TEXT, COMPONENT
    }

    /**
     * SSE 数据块
     */
    private static class SseChunk {
        final SseChunkType type;
        final String payload;

        private SseChunk(SseChunkType type, String payload) {
            this.type = type;
            this.payload = payload;
        }

        static SseChunk text(String text) {
            return new SseChunk(SseChunkType.TEXT, text);
        }

        static SseChunk component(String json) {
            return new SseChunk(SseChunkType.COMPONENT, json);
        }

        String toSseData() {
            return "[" + type.name() + "]" + payload;
        }
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

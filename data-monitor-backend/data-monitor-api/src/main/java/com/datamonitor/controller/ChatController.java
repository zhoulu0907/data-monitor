package com.datamonitor.controller;

import com.datamonitor.common.ApiResponse;
import com.datamonitor.service.ChatService;
import com.datamonitor.vo.ChatMessageVO;
import com.datamonitor.vo.ChatRequestVO;
import com.datamonitor.vo.ChatSessionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI 聊天控制器
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /** SSE 超时时间：5 分钟 */
    private static final long SSE_TIMEOUT_MINUTES = 5;

    /** 创建会话 */
    @PostMapping("/sessions")
    public ApiResponse<ChatSessionVO> createSession(@RequestBody(required = false) Map<String, String> body) {
        String title = (body != null) ? body.get("title") : null;
        return ApiResponse.success(chatService.createSession(title));
    }

    /** 查询会话列表 */
    @GetMapping("/sessions")
    public ApiResponse<List<ChatSessionVO>> listSessions() {
        return ApiResponse.success(chatService.listSessions());
    }

    /** 删除会话 */
    @PostMapping("/sessions/{id}/delete")
    public ApiResponse<Void> deleteSession(@PathVariable("id") String sessionId) {
        chatService.deleteSession(sessionId);
        return ApiResponse.success(null);
    }

    /** 查询消息 */
    @GetMapping("/sessions/{id}/messages")
    public ApiResponse<List<ChatMessageVO>> listMessages(@PathVariable("id") String sessionId) {
        return ApiResponse.success(chatService.listMessages(sessionId));
    }

    /**
     * SSE 流式对话接口
     * <p>
     * 接收用户消息，调用 LLM 通过 SSE 逐块返回 AI 回复。
     * 支持 Function Calling 和 A2UI 组件指令。
     * SSE 数据格式：
     * - data:[TEXT]文本内容
     * - data:[COMPONENT]{"componentName":"xxx","props":{...}}
     * - data:[DONE]
     */
    @PostMapping(value = "/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter completions(@RequestBody ChatRequestVO request) {
        // 参数校验
        request.validate();

        // 创建 SseEmitter，设置超时时间
        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(SSE_TIMEOUT_MINUTES));

        // 设置连接生命周期回调
        emitter.onCompletion(() -> log.debug("SSE 连接正常关闭"));
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时");
            emitter.complete();
        });
        emitter.onError(e -> {
            log.warn("SSE 连接异常: {}", e.getMessage());
            emitter.completeWithError(e);
        });

        // 异步处理 SSE 流
        chatService.processChatStream(emitter, request.getSessionId(), request.getMessage());

        return emitter;
    }
}

package com.datamonitor.service;

import com.datamonitor.vo.ChatMessageVO;
import com.datamonitor.vo.ChatSessionVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 聊天会话服务接口
 *
 * @author zhoulu
 * @since 2026-04-23
 */
public interface ChatService {

    /** 创建会话 */
    ChatSessionVO createSession(String title);

    /** 查询所有会话，按 updatedAt DESC */
    List<ChatSessionVO> listSessions();

    /** 删除会话及其消息 */
    void deleteSession(String sessionId);

    /** 查询会话消息 */
    List<ChatMessageVO> listMessages(String sessionId);

    /** 保存消息 */
    void saveMessage(String sessionId, String role, String content, String componentData);

    /** Mock 模式 SSE 流式处理 */
    void processChatStream(SseEmitter emitter, String sessionId, String message);
}

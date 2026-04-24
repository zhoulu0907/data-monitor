package com.datamonitor.service.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 聊天配置
 * <p>
 * 配置 ChatClient Bean，注入系统提示词。
 * 使用 Spring AI 框架替代自定义 OkHttp 客户端。
 *
 * @author zhoulu
 * @since 2026-04-24
 */
@Configuration
public class SpringAiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem(SpringPromptProvider.buildSystemPrompt())
                .build();
    }
}

package com.datamonitor.service.llm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LLM 配置属性
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.llm")
public class LlmProperties {

    /** 是否启用 LLM（true=调用真实LLM, false=Mock模式） */
    private boolean enabled = false;

    /** LLM API 地址 */
    private String apiUrl = "https://api.openai.com/v1/chat/completions";

    /** API Key */
    private String apiKey = "sk-xxx";

    /** 模型名称 */
    private String model = "gpt-4o";

    /** 最大 token 数 */
    private int maxTokens = 2048;

    /** 温度参数 */
    private double temperature = 0.7;

    /** Function Calling 最大调用轮次 */
    private int maxFunctionCalls = 3;
}

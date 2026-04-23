package com.datamonitor.service.llm;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * LLM 相关配置
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Configuration
public class LlmConfig {

    /**
     * 创建 OkHttpClient Bean
     * 设置较长的超时时间，以适应 LLM 流式响应场景
     */
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
}

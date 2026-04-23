package com.datamonitor.service.llm;

import java.util.Map;

/**
 * 数据查询函数接口
 * <p>
 * 用于定义 LLM Function Calling 中的工具函数。
 * 每个实现对应一个数据查询能力。
 *
 * @author zhoulu
 * @since 2026-04-23
 */
public interface DataQueryFunction {

    /**
     * 函数名（对应 LLM tools 中的 function name）
     *
     * @return 函数名称
     */
    String getName();

    /**
     * 函数描述（供 LLM 理解函数用途）
     *
     * @return 函数描述
     */
    String getDescription();

    /**
     * JSON Schema 格式的参数定义
     * <p>
     * 返回 OpenAI tools 格式的 parameters JSON，例如：
     * {"type":"object","properties":{"year":{"type":"integer","description":"查询年份"}},"required":[]}
     *
     * @return JSON Schema 字符串
     */
    String getParametersJson();

    /**
     * 执行查询
     *
     * @param params 函数参数
     * @return 查询结果 JSON 字符串
     */
    String execute(Map<String, Object> params);
}

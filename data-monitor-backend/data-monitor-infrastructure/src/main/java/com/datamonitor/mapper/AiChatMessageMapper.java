package com.datamonitor.mapper;

import com.mybatisflex.core.BaseMapper;
import com.datamonitor.entity.AiChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 聊天消息 Mapper
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessageEntity> {
}

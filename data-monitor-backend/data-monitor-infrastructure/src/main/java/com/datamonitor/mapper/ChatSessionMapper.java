package com.datamonitor.mapper;

import com.datamonitor.entity.ChatSessionEntity;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天会话 Mapper
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionEntity> {
}

package com.datamonitor.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天会话实体
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_session")
public class ChatSessionEntity {

    @Id(keyType = KeyType.None)
    private String id;

    private String title;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

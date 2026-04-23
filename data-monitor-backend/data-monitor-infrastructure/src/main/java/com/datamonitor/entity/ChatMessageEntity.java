package com.datamonitor.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息实体
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_message")
public class ChatMessageEntity {

    @Id(keyType = KeyType.Auto)
    private Integer id;

    private String sessionId;

    private String role;

    private String content;

    private String componentData;

    private LocalDateTime createdAt;
}

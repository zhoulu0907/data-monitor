package com.datamonitor.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 预警实体
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("ai_warning")
public class AiWarningEntity {

    @Id(keyType = KeyType.Auto)
    private String id;

    private String level;

    private String title;

    private String scope;

    private String timestamp;
}

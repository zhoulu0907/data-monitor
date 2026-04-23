package com.datamonitor.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 简报实体
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("ai_briefing")
public class AiBriefingEntity {

    @Id(keyType = KeyType.Auto)
    private String id;

    private String tag;

    private String title;

    private String summary;

    private String timestamp;
}

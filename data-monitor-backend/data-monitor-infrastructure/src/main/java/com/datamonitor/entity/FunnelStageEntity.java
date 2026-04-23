package com.datamonitor.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 漏斗阶段实体
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("funnel_stage")
public class FunnelStageEntity {

    @Id(keyType = KeyType.Auto)
    private Integer id;

    private String stage;

    private Double value;

    private Double rate;

    private Integer sortOrder;
}

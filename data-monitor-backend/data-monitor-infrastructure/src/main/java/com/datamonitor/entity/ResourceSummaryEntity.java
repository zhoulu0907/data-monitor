package com.datamonitor.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源汇总实体
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("resource_summary")
public class ResourceSummaryEntity {

    @Id(keyType = KeyType.Auto)
    private Integer id;

    private Integer serverCount;

    private Double computingCost;

    private Double aiServiceCost;
}

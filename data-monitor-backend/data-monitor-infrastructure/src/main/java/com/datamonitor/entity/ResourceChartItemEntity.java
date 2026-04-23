package com.datamonitor.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源图表项实体
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("resource_chart_item")
public class ResourceChartItemEntity {

    @Id(keyType = KeyType.Auto)
    private Integer id;

    private String name;

    private Integer currentValue;

    private Integer targetValue;

    private Integer sortOrder;
}

package com.datamonitor.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * KPI 指标实体
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("kpi_item")
public class KpiItemEntity {

    @Id(keyType = KeyType.Auto)
    private Integer id;

    private String metricKey;

    private String title;

    private Double value;

    private String unit;

    private String comparisonLabel;

    private String comparisonValue;

    private String trend;
}

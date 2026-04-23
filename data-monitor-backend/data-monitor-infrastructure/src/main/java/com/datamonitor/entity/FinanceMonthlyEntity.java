package com.datamonitor.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 月度财务数据实体
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("finance_monthly")
public class FinanceMonthlyEntity {

    @Id(keyType = KeyType.Auto)
    private Integer id;

    private String month;

    private Double revenue;

    private Double netIncome;

    private Double costBudget;

    private Double actualCost;

    private Integer sortOrder;
}

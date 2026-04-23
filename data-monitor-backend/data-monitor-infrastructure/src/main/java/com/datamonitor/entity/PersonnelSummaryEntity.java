package com.datamonitor.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 人员汇总实体
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("personnel_summary")
public class PersonnelSummaryEntity {

    @Id(keyType = KeyType.Auto)
    private Integer id;

    private Integer totalHeadcount;

    private Integer internalStaff;

    private Integer outsourcedStaff;

    private Double monthlyInvestment;
}

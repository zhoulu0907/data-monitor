package com.datamonitor.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 月度人员饱和度实体
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("personnel_saturation")
public class PersonnelSaturationEntity {

    @Id(keyType = KeyType.Auto)
    private Integer id;

    private String month;

    private Integer rate;

    private Integer headcount;

    private Integer sortOrder;
}

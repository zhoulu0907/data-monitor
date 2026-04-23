package com.datamonitor.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行业收入分布实体
 *
 * @author zhoulu
 * @since 2026-04-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("industry_share")
public class IndustryShareEntity {

    @Id(keyType = KeyType.Auto)
    private Integer id;

    private String name;

    private Integer value;

    private String color;

    private Integer sortOrder;
}

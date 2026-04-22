package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 人员汇总
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "人员汇总")
public class PersonnelSummaryVO {

    @Schema(description = "总人数")
    private Integer totalHeadcount;

    @Schema(description = "内部员工数")
    private Integer internalStaff;

    @Schema(description = "外包人员数")
    private Integer outsourcedStaff;

    @Schema(description = "月度人力投入")
    private Double monthlyInvestment;
}

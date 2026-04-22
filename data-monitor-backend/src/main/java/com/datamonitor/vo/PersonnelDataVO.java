package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 人员数据
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "人员数据")
public class PersonnelDataVO {

    @Schema(description = "人员汇总")
    private PersonnelSummaryVO summary;

    @Schema(description = "月度饱和度列表")
    private List<MonthlySaturationVO> saturation;
}

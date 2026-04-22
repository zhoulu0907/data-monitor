package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 月度饱和度
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "月度饱和度")
public class MonthlySaturationVO {

    @Schema(description = "月份")
    private String month;

    @Schema(description = "饱和率")
    private Integer rate;

    @Schema(description = "人数")
    private Integer headcount;
}

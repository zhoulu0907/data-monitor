package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行业收入项
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "行业收入项")
public class IndustryItemVO {

    @Schema(description = "行业名称")
    private String name;

    @Schema(description = "行业收入值")
    private Integer value;

    @Schema(description = "颜色")
    private String color;
}

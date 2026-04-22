package com.datamonitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源图表项
 *
 * @author zhoulu
 * @since 2026-04-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "资源图表项")
public class ResourceChartItemVO {

    @Schema(description = "名称")
    private String name;

    @Schema(description = "当前值")
    private Integer current;

    @Schema(description = "目标值")
    private Integer target;
}

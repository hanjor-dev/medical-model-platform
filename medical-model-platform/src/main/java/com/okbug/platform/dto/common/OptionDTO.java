package com.okbug.platform.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用下拉选项 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "下拉选项")
public class OptionDTO {

    @Schema(description = "值")
    private String value;

    @Schema(description = "显示标签")
    private String label;
}



/**
 * 导入结果统计DTO
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 15:30:00
 */
package com.okbug.platform.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "导入结果统计DTO")
public class ImportResultDTO {
    
    @Schema(description = "总记录数")
    private Integer totalCount;
    
    @Schema(description = "成功导入数")
    private Integer successCount;
    
    @Schema(description = "失败记录数")
    private Integer failedCount;
    
    @Schema(description = "跳过记录数")
    private Integer skippedCount;
    
    @Schema(description = "失败记录详情")
    private List<ImportErrorDTO> errors;
    
    @Schema(description = "导入消息")
    private String message;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "导入错误详情")
    public static class ImportErrorDTO {
        
        @Schema(description = "行号")
        private Integer rowNumber;
        
        @Schema(description = "字典编码")
        private String dictCode;
        
        @Schema(description = "错误信息")
        private String errorMessage;
    }
}

/**
 * 字典模块选项DTO：用于前端下拉选择的模块选项
 * 
 * 包含字段：
 * - value: 模块代码（英文），如 "SYSTEM"
 * - label: 模块标签（中文），如 "系统模块"
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.dto.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典模块选项DTO
 * 用于前端下拉选择器的数据格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictModuleOptionDTO {
    
    /**
     * 模块代码（英文）
     * 用作选择器的value值，如：SYSTEM、USER、FILE等
     */
    private String value;
    
    /**
     * 模块标签（中文）
     * 用作选择器的显示文本，如：系统模块、用户模块、文件模块等
     */
    private String label;
}

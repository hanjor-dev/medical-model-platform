/**
 * 字典数据DTO：字典数据项数据传输对象
 * 
 * 核心功能：
 * 1. 用于传输字典项的具体数据
 * 2. 支持前端下拉选择、单选、多选等组件
 * 3. 包含字典项的核心信息
 * 4. 支持字典项的排序和状态管理
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.dto.system;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DictDataDTO {
    
    /**
     * 字典编码
     * 用于标识具体的字典项，如：DICT_1.1、DICT_1.2
     */
    private String code;
    
    /**
     * 字典标签
     * 用于前端显示的文本，如：启用、禁用
     */
    private String label;
    
    /**
     * 字典描述
     * 用于说明字典项的用途和含义
     */
    private String description;
    
    /**
     * 字典状态，0:禁用，1:启用
     */
    private Integer status;
    
    /**
     * 排序，数值越小排序越靠前
     */
    private Integer sortOrder;

    /**
     * 业务值（建议用于前端实际取值），如 inbox/email/system/task 等
     */
    private String value;
} 

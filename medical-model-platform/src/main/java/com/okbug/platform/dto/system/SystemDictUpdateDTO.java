/**
 * 系统字典更新DTO：系统字典更新请求参数封装
 * 
 * 核心功能：
 * 1. 封装更新系统字典所需的参数
 * 2. 提供参数验证规则
 * 3. 支持部分字段更新
 * 4. 确保字典数据更新的安全性
 * 5. 支持字典状态和排序更新
 * 
 * @author hanjor
 * @version 2.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.dto.system;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;

@Data
public class SystemDictUpdateDTO {
    
    /**
     * 字典名称，可选
     * 更新后的字典名称，用于显示和识别
     * 如果为空，则保持原有名称不变
     */
    private String dictName;
    
    /**
     * 字典标签，可选
     * 更新后的字典标签，用于前端显示
     * 如果为空，则保持原有标签不变
     */
    private String dictLabel;
    
    /**
     * 字典描述，可选
     * 更新后的字典描述，用于说明字典项的用途和含义
     * 如果为空，则保持原有描述不变
     */
    private String description;
    
    /**
     * 父级字典ID，可选
     * 更新后的父级字典ID，用于调整字典层级关系
     * 如果为空，则保持原有父级关系不变
     * 注意：修改父级关系会影响字典的层级和路径
     */
    private Long parentId;
    
    /**
     * 模块名称，可选
     * 更新后的模块名称，用于调整字典分组
     * 如果为空，则保持原有模块不变
     */
    @Pattern(regexp = "^(SYSTEM|USER|FILE|TASK)$", message = "模块名称必须是SYSTEM、USER、FILE或TASK")
    private String module;
    
    /**
     * 字典状态，可选
     * 0: 禁用，1: 启用
     * 如果为空，则保持原有状态不变
     */
    private Integer status;
    
    /**
     * 排序，可选
     * 数值越小，排序越靠前
     * 如果为空，则保持原有排序不变
     */
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sortOrder;
} 

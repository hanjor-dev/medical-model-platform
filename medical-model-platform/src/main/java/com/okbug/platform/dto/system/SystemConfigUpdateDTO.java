/**
 * 系统配置更新DTO：系统配置更新请求参数封装
 * 
 * 核心功能：
 * 1. 封装更新系统配置所需的参数
 * 2. 提供参数验证规则
 * 3. 支持部分字段更新
 * 4. 确保配置数据更新的安全性
 * 5. 支持配置状态和排序更新
 * 6. 记录操作人信息用于审计日志
 * 
 * @author hanjor
 * @version 2.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.dto.system;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

@Data
public class SystemConfigUpdateDTO {
    
    /**
     * 配置值，必填
     * 更新后的配置值，会根据配置类型进行相应的验证：
     * - STRING类型：支持任意字符串
     * - NUMBER类型：必须是有效的数字字符串
     * - BOOLEAN类型：必须是"true"或"false"
     * - JSON类型：必须是有效的JSON格式字符串
     * 
     * 注意：配置键、配置类型、配置分类等核心属性不允许修改，
     * 只能修改配置值、描述、状态、排序等字段，确保配置的一致性和稳定性
     */
    @NotBlank(message = "配置值不能为空")
    private String configValue;
    
    /**
     * 配置描述，可选
     * 更新后的配置描述，用于说明配置项的用途和含义
     * 如果为空，则保持原有描述不变
     * 建议填写详细描述，便于后续维护和理解
     */
    private String description;
    
    /**
     * 配置状态，可选
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

    /**
     * 配置分类字典编码（仅当允许调整分类时使用；默认更新不修改分类）
     */
    private String configCategoryCode;

    /**
     * 配置类型字典编码（仅当允许调整类型时使用；默认更新不修改类型）
     */
    private String configTypeCode;
    
    /**
     * 操作人ID，用于审计日志记录
     */
    private Long operatorId;
    
    /**
     * 操作人姓名，用于审计日志记录
     */
    private String operatorName;
    
    /**
     * 操作IP地址，用于审计日志记录
     */
    private String ipAddress;
} 

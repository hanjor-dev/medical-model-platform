/**
 * 系统配置创建DTO：系统配置创建请求参数封装
 * 
 * 核心功能：
 * 1. 封装创建系统配置所需的参数
 * 2. 提供参数验证规则
 * 3. 确保配置数据的完整性和有效性
 * 4. 支持前端表单提交的配置创建
 * 5. 支持配置状态和排序设置
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
public class SystemConfigCreateDTO {
    
    /**
     * 配置键，唯一标识，必填
     * 格式要求：使用点号分隔的小写字母，如：user.operation.fail.max.count
     * 建议遵循命名规范：{模块}.{功能}.{属性}
     */
    @NotBlank(message = "配置键不能为空")
    private String configKey;
    
    /**
     * 配置值，必填
     * 根据配置类型存储相应的值：
     * - STRING类型：存储字符串
     * - NUMBER类型：存储数字字符串
     * - BOOLEAN类型：存储"true"或"false"
     * - JSON类型：存储JSON格式字符串
     */
    @NotBlank(message = "配置值不能为空")
    private String configValue;
    
    /**
     * 配置类型，必填
     * 限制配置值的格式和验证规则
     * STRING: 字符串类型，支持任意文本
     * NUMBER: 数字类型，支持整数和小数
     * BOOLEAN: 布尔类型，只支持true/false
     * JSON: JSON类型，支持JSON格式字符串
     */
    @NotBlank(message = "配置类型不能为空")
    private String configType;
    
    /**
     * 配置类型字典编码（可选，优先于configType）
     * 父级字典：DICT_1 的子项（如：DICT_1.2 字符串、DICT_1.3 数字）
     */
    private String configTypeCode;
    
    /**
     * 配置分类，必填
     * 用于对配置进行分组管理，便于维护和查找
     * SYSTEM: 系统配置（维护模式、缓存设置等）
     * USER: 用户配置（登录限制、会话设置等）
     * FILE: 文件配置（上传限制、存储设置等）
     * TASK: 任务配置（超时设置、重试次数等）
     * SECURITY: 安全配置（加密算法、访问控制等）
     * CACHE: 缓存配置（过期时间、淘汰策略等）
     */
    @NotBlank(message = "配置分类不能为空")
    private String configCategory;
    
    /**
     * 配置分类字典编码（可选，优先于configCategory）
     * 父级字典：DICT_3 的子项（如：DICT_3.7 用户配置、DICT_3.8 系统配置）
     */
    private String configCategoryCode;
    
    /**
     * 配置描述，可选
     * 说明配置项的用途、含义和使用场景
     * 建议填写详细描述，便于后续维护和理解
     */
    private String description;
    
    /**
     * 配置状态，可选，默认为1（启用）
     * 0: 禁用，1: 启用
     */
    private Integer status = 1;
    
    /**
     * 排序，可选，默认为0
     * 数值越小，排序越靠前
     */
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sortOrder = 0;
    
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

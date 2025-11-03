/**
 * 配置验证服务接口：系统配置值验证服务
 * 
 * 核心功能：
 * 1. 提供配置值类型验证（字符串、数字、布尔、JSON）
 * 2. 支持配置值范围验证和格式验证
 * 3. 支持自定义验证规则扩展
 * 4. 提供配置值类型转换功能
 * 5. 支持配置项依赖关系验证
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.service.system;

import com.okbug.platform.dto.system.SystemConfigCreateDTO;
import com.okbug.platform.dto.system.SystemConfigUpdateDTO;
import com.okbug.platform.entity.system.SystemConfig;

/**
 * 配置验证服务接口
 * 定义配置值验证的所有业务方法
 * 包括类型验证、范围验证、格式验证、依赖验证等核心功能
 */
public interface ConfigValidationService {
    
    /**
     * 验证配置创建请求
     * 
     * 验证内容：
     * 1. 配置键格式验证
     * 2. 配置值类型验证
     * 3. 配置分类验证
     * 4. 配置值范围验证
     * 5. 配置键唯一性验证
     * 
     * @param createDto 配置创建请求
     * @throws RuntimeException 当验证失败时抛出
     */
    void validateCreateRequest(SystemConfigCreateDTO createDto);
    
    /**
     * 验证配置更新请求
     * 
     * 验证内容：
     * 1. 配置值类型验证
     * 2. 配置值范围验证
     * 3. 配置状态验证
     * 4. 配置排序验证
     * 
     * @param updateDto 配置更新请求
     * @param currentConfig 当前配置信息
     * @throws RuntimeException 当验证失败时抛出
     */
    void validateUpdateRequest(SystemConfigUpdateDTO updateDto, SystemConfig currentConfig);
    
    /**
     * 验证配置值类型
     * 
     * 验证逻辑：
     * 1. STRING类型：允许任意字符串，支持长度限制
     * 2. NUMBER类型：必须是有效的数字，支持范围验证
     * 3. BOOLEAN类型：必须是true或false
     * 4. JSON类型：必须是有效的JSON格式
     * 
     * @param configValue 配置值
     * @param configType 配置类型
     * @throws RuntimeException 当类型验证失败时抛出
     */
    void validateConfigValueType(String configValue, String configType);
    
    /**
     * 验证配置值范围
     * 
     * 验证逻辑：
     * 1. 数字类型：验证数值范围
     * 2. 字符串类型：验证长度范围
     * 3. 支持自定义范围规则
     * 
     * @param configValue 配置值
     * @param configType 配置类型
     * @param configKey 配置键（用于获取范围规则）
     * @throws RuntimeException 当范围验证失败时抛出
     */
    void validateConfigValueRange(String configValue, String configType, String configKey);
    
    /**
     * 验证配置值格式
     * 
     * 验证逻辑：
     * 1. 邮箱格式验证
     * 2. URL格式验证
     * 3. 日期格式验证
     * 4. 正则表达式验证
     * 
     * @param configValue 配置值
     * @param configKey 配置键（用于获取格式规则）
     * @throws RuntimeException 当格式验证失败时抛出
     */
    void validateConfigValueFormat(String configValue, String configKey);
    
    /**
     * 验证配置项依赖关系
     * 
     * 验证逻辑：
     * 1. 检查配置项之间的依赖关系
     * 2. 验证依赖配置项是否存在且启用
     * 3. 验证依赖配置项的值是否满足要求
     * 
     * @param configKey 配置键
     * @param configValue 配置值
     * @throws RuntimeException 当依赖验证失败时抛出
     */
    void validateConfigDependencies(String configKey, String configValue);
    
    /**
     * 验证配置键格式
     * 
     * 验证逻辑：
     * 1. 配置键不能为空
     * 2. 配置键只能包含字母、数字、下划线、点号
     * 3. 配置键必须以字母开头
     * 4. 配置键长度限制
     * 
     * @param configKey 配置键
     * @throws RuntimeException 当格式验证失败时抛出
     */
    void validateConfigKeyFormat(String configKey);
    
    /**
     * 验证配置分类
     * 
     * 验证逻辑：
     * 1. 配置分类不能为空
     * 2. 配置分类必须是预定义的有效分类
     * 3. 配置分类与配置键的匹配性验证
     * 
     * @param configCategory 配置分类
     * @param configKey 配置键
     * @throws RuntimeException 当分类验证失败时抛出
     */
    void validateConfigCategory(String configCategory, String configKey);
    
    /**
     * 将配置值转换为指定类型
     * 
     * @param configValue 配置值
     * @param configType 目标类型
     * @return 转换后的值
     * @throws RuntimeException 当转换失败时抛出
     */
    Object convertConfigValue(String configValue, String configType);
    
    /**
     * 获取配置值的默认值
     * 
     * @param configKey 配置键
     * @param configType 配置类型
     * @return 默认值
     */
    String getDefaultValue(String configKey, String configType);
} 
/**
 * 系统配置DTO：系统配置数据传输对象
 * 
 * 核心功能：
 * 1. 在Controller和Service层之间传输系统配置数据
 * 2. 隐藏敏感字段，只暴露必要的配置信息
 * 3. 提供Builder模式构建配置对象
 * 4. 支持配置的前端展示和编辑
 * 5. 包含配置状态和排序信息
 * 
 * @author hanjor
 * @version 2.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.dto.system;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class SystemConfigDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 配置ID
     * 使用字符串序列化，避免JavaScript精度丢失问题
     */
    private Long id;
    
    /**
     * 配置键，唯一标识
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置类型（STRING/NUMBER/BOOLEAN/JSON）
     */
    private String configType;
    
    /**
     * 配置类型字典编码（DICT_1.* 子项）
     */
    private String configTypeCode;
    
    /**
     * 配置分类（SYSTEM/USER/FILE/TASK/SECURITY/CACHE）
     */
    private String configCategory;
    
    /**
     * 配置分类字典编码（DICT_3.* 子项）
     */
    private String configCategoryCode;
    
    /**
     * 配置描述
     */
    private String description;
    
    /**
     * 配置状态，0:禁用，1:启用
     */
    private Integer status;
    
    /**
     * 排序，数值越小排序越靠前
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 

/**
 * 系统配置实体类：管理系统配置信息
 * 
 * 核心功能：
 * 1. 存储系统各种配置项的键值对
 * 2. 支持配置分类管理（系统、用户、文件、任务等）
 * 3. 支持多种配置类型（字符串、数字、布尔值）
 * 4. 提供配置的增删改查基础数据结构
 * 5. 支持配置状态控制和排序管理
 * 
 * @author hanjor
 * @version 2.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("system_configs")
public class SystemConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 配置ID（主键）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 配置键，唯一标识，如：user.operation.fail.max.count
     */
    private String configKey;
    
    /**
     * 配置值，存储具体的配置内容
     */
    private String configValue;
    
    /**
     * 配置类型，限制配置值的格式
     * STRING: 字符串类型
     * NUMBER: 数字类型
     * BOOLEAN: 布尔类型
     * JSON: JSON对象类型
     */
    private String configType;

    /**
     * 配置类型字典编码（DICT_3.* 子项）
     * 对应 configType 的字典编码表示
     */
    private String configTypeCode;
    
    /**
     * 配置分类，用于对配置进行分组管理
     * SYSTEM: 系统配置（维护模式、缓存设置等）
     * USER: 用户配置（登录限制、会话设置等）
     * FILE: 文件配置（上传限制、存储设置等）
     * TASK: 任务配置（超时设置、重试次数等）
     * SECURITY: 安全配置（加密算法、访问控制等）
     * CACHE: 缓存配置（过期时间、淘汰策略等）
     */
    private String configCategory;

    /**
     * 配置分类字典编码（DICT_1.* 子项）
     * 使用字典编码作为权威标识，便于前后端统一与统计聚合
     */
    private String configCategoryCode;
    
    /**
     * 配置描述，说明配置项的用途和含义
     */
    private String description;
    
    /**
     * 配置状态，0:禁用，1:启用
     * 禁用的配置项不会在业务中使用
     */
    private Integer status;
    
    /**
     * 排序，用于控制配置项的显示顺序
     * 数值越小，排序越靠前
     */
    private Integer sortOrder;
    
    /**
     * 删除标记，0:正常，1:已删除
     */
    @TableLogic
    private Integer isDeleted;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 
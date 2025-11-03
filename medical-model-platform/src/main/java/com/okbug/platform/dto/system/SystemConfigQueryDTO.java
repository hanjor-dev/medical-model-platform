/**
 * 系统配置查询DTO：系统配置查询条件封装
 * 
 * 核心功能：
 * 1. 封装系统配置查询的各种条件参数
 * 2. 支持按分类、关键词、类型、状态等条件筛选
 * 3. 提供分页查询参数
 * 4. 支持模糊搜索和精确匹配
 * 5. 支持按状态和排序字段筛选
 * 
 * @author hanjor
 * @version 2.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.dto.system;

import lombok.Data;

import jakarta.validation.constraints.Min;

@Data
public class SystemConfigQueryDTO {
    
    /**
     * 配置分类，用于按分类筛选配置
     * 可选值：SYSTEM（系统配置）、USER（用户配置）、FILE（文件配置）、TASK（任务配置）、SECURITY（安全配置）、CACHE（缓存配置）
     */
    private String configCategory;
    
    /**
     * 配置分类编码（字典码），用于按分类筛选配置
     * 建议优先使用该字段进行筛选，例如：DICT_1.7（用户配置分类）、DICT_1.8（系统配置分类）
     */
    private String configCategoryCode;
    
    /**
     * 配置键，支持模糊搜索
     * 例如：输入"login"可以搜索到"user.login.fail.max.count"等配置
     */
    private String configKey;
    
    /**
     * 配置描述关键词，支持模糊搜索
     * 例如：输入"登录"可以搜索到描述中包含"登录"的配置
     */
    private String description;
    
    /**
     * 配置类型，用于按类型筛选配置
     * 可选值：STRING（字符串）、NUMBER（数字）、BOOLEAN（布尔值）、JSON（JSON对象）
     */
    private String configType;
    
    /**
     * 页码，从1开始，默认为1
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;
    
    /**
     * 页大小，每页显示的记录数，默认为10
     */
    @Min(value = 1, message = "页大小必须大于0")
    private Integer pageSize = 12;
    
    /**
     * 配置状态，用于按状态筛选配置
     * 可选值：0（禁用）、1（启用）、null（全部）
     */
    private Integer status;
    
    /**
     * 排序字段，用于指定排序方式
     * 可选值：configKey（按配置键排序）、configCategory（按分类排序）、sortOrder（按排序字段排序）、createTime（按创建时间排序）、updateTime（按更新时间排序）
     */
    private String sortField = "sortOrder";
    
    /**
     * 排序方向，用于指定排序方向
     * 可选值：asc（升序）、desc（降序）
     */
    private String sortDirection = "asc";
} 

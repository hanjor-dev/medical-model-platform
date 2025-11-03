/**
 * 字典查询DTO：字典查询条件封装
 * 
 * 核心功能：
 * 1. 封装字典查询的各种条件参数
 * 2. 支持按编码、关键词、模块、状态等条件筛选
 * 3. 提供分页查询参数
 * 4. 支持模糊搜索和精确匹配
 * 5. 支持按排序字段和方向排序
 * 
 * @author hanjor
 * @version 2.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.dto.system;

import lombok.Data;

import jakarta.validation.constraints.Min;

@Data
public class SystemDictQueryDTO {
    
    /**
     * 字典编码，支持模糊搜索
     */
    private String dictCode;
    
    /**
     * 关键词搜索，同时搜索字典名称和标签
     */
    private String keyword;
    
    /**
     * 所属模块，用于按模块筛选
     */
    private String module;
    
    /**
     * 字典状态，0:禁用，1:启用
     */
    private Integer status;
    
    /**
     * 父级字典ID，用于查询特定父级下的字典项
     */
    private Long parentId;
    
    /**
     * 字典层级，用于查询特定层级的字典
     */
    private Integer level;
    
    /**
     * 排序字段，用于指定排序方式
     * 可选值：dictCode（按字典编码排序）、module（按模块排序）、level（按层级排序）、createTime（按创建时间排序）、updateTime（按更新时间排序）
     */
    private String sortField = "createTime";
    
    /**
     * 排序方向，用于指定排序方向
     * 可选值：asc（升序）、desc（降序）
     */
    private String sortDirection = "desc";
    
    /**
     * 页码，从1开始，默认为1
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;
    
    /**
     * 页大小，每页显示的记录数，默认为10
     */
    @Min(value = 1, message = "页大小必须大于0")
    private Integer pageSize = 10;
} 

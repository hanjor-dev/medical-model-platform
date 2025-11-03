/**
 * 字典树形结构DTO：字典树形结构数据传输对象
 * 
 * 核心功能：
 * 1. 支持多级字典的树形结构展示
 * 2. 提供树形节点的完整信息
 * 3. 支持前端树形组件的渲染
 * 4. 包含节点的层级和父子关系信息
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.dto.system;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DictTreeDTO {
    
    /**
     * 字典ID
     */
    private Long id;
    
    /**
     * 字典编码，唯一标识
     * 使用DICT_path格式，如：DICT_1、DICT_1.2
     */
    private String dictCode;
    
    /**
     * 字典名称
     */
    private String dictName;
    
    /**
     * 字典标签
     */
    private String dictLabel;
    
    /**
     * 字典描述
     */
    private String description;
    
    /**
     * 父级字典ID
     * 0表示顶级字典，其他值表示父级字典的ID
     */
    private Long parentId;
    
    /**
     * 字典层级
     * 1表示顶级字典，2表示二级字典，以此类推
     */
    private Integer level;
    
    /**
     * 字典路径
     * 使用点号分隔的ID路径，如：1.2.3
     */
    private String path;
    
    /**
     * 模块名称
     * SYSTEM: 系统字典
     * USER: 用户字典
     * FILE: 文件字典
     * TASK: 任务字典
     */
    private String module;
    
    /**
     * 字典状态，0:禁用，1:启用
     */
    private Integer status;
    
    /**
     * 排序，数值越小排序越靠前
     */
    private Integer sortOrder;
    
    /**
     * 是否为根节点
     * true: 根节点，false: 非根节点
     */
    private Boolean isRoot;
    
    /**
     * 是否为叶子节点
     * true: 叶子节点，false: 非叶子节点
     */
    private Boolean isLeaf;
    
    /**
     * 子字典列表
     * 用于构建树形结构，只有非叶子节点才有此字段
     */
    private List<DictTreeDTO> children;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 

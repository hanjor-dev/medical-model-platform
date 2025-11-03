/**
 * 系统字典DTO：系统字典数据传输对象
 * 
 * 核心功能：
 * 1. 在Controller和Service层之间传输系统字典数据
 * 2. 隐藏敏感字段，只暴露必要的字典信息
 * 3. 提供Builder模式构建字典对象
 * 4. 支持字典的前端展示和编辑
 * 5. 包含字典状态和排序信息
 * 
 * @author hanjor
 * @version 2.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.dto.system;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SystemDictDTO {
    
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
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 

/**
 * 通用字典实体类：支持多级字典结构管理
 * 
 * 核心功能：
 * 1. 存储所有字典数据，支持无限级字典结构
 * 2. 使用path作为dictCode，确保每个字典项都有唯一编码
 * 3. 自动维护字典层级和路径关系
 * 4. 支持模块化字典数据管理
 * 5. 支持字典状态控制和排序管理
 * 
 * @author hanjor
 * @version 2.1
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("system_dict")
public class SystemDict {
    
    /**
     * 字典ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 字典编码(使用path值，如：DICT_1.2)
     * 确保每个字典项都有唯一的编码，解决重复问题
     */
    private String dictCode;
    
    /**
     * 字典名称，用于显示字典项名称
     * 例如：字符串类型、数字类型、用户配置分类
     */
    private String dictName;
    
    /**
     * 字典标签，字典项的显示标签
     * 例如：字符串、数字、用户配置、系统配置等
     */
    private String dictLabel;
    
    /**
     * 字典描述，说明字典的用途和含义
     */
    private String description;
    
    /**
     * 父级字典ID，0表示顶级字典
     * 用于构建多级字典结构
     */
    private Long parentId;
    
    /**
     * 字典层级，1表示顶级，2表示二级，以此类推
     * 用于快速判断字典的层级关系
     */
    private Integer level;
    
    /**
     * 字典路径，表示从根到当前节点的完整路径
     * 格式：1.2.3，表示ID为1的字典下的ID为2的字典下的ID为3的字典
     * 用于快速构建树形结构和查询子节点
     */
    private String path;
    
    /**
     * 所属模块，用于对字典进行分类管理
     * SYSTEM: 系统模块
     * USER: 用户模块
     * FILE: 文件模块
     * TASK: 任务模块
     * 等
     */
    private String module;
    
    /**
     * 状态，0:禁用，1:启用
     * 禁用的字典项不会在业务中使用
     */
    private Integer status;
    
    /**
     * 排序，用于控制字典项的显示顺序
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
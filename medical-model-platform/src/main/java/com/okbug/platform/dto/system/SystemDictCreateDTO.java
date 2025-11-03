/**
 * 系统字典创建DTO：系统字典创建请求参数封装
 * 
 * 核心功能：
 * 1. 封装创建系统字典所需的参数
 * 2. 提供参数验证规则
 * 3. 确保字典数据的完整性和有效性
 * 4. 支持前端表单提交的字典创建
 * 5. 支持字典状态和排序设置
 * 
 * @author hanjor
 * @version 2.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.dto.system;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;

@Data
public class SystemDictCreateDTO {
    
    /**
     * 字典编码，唯一标识，系统自动生成
     * 格式：DICT_{module}_{path}，如：DICT_SYSTEM_1、DICT_SYSTEM_1.2
     * 根据所属模块和层级路径自动生成，确保唯一性
     */
    private String dictCode;
    
    /**
     * 字典名称，必填
     * 用于显示和识别的字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    private String dictName;
    
    /**
     * 字典标签，必填
     * 用于前端显示的标签文本
     */
    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;
    
    /**
     * 字典描述，可选
     * 说明字典项的用途、含义和使用场景
     */
    private String description;
    
    /**
     * 父级字典ID，可选
     * 0表示顶级字典，其他值表示父级字典的ID
     */
    private Long parentId = 0L;
    
    /**
     * 模块名称，必填
     * 用于对字典进行分组管理，便于维护和查找
     * SYSTEM: 系统字典（配置类型、状态等）
     * USER: 用户字典（用户类型、角色等）
     * FILE: 文件字典（文件类型、状态等）
     * TASK: 任务字典（任务类型、状态等）
     */
    @NotBlank(message = "模块名称不能为空")
    private String module;
    
    /**
     * 字典状态，可选，默认为1（启用）
     * 0: 禁用，1: 启用
     */
    private Integer status = 1;
    
    /**
     * 排序，可选，默认为0
     * 数值越小，排序越靠前
     */
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sortOrder = 0;
} 

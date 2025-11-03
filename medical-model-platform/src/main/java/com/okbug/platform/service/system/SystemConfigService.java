/**
 * 系统配置服务接口：系统配置业务逻辑层
 * 
 * 核心功能：
 * 1. 提供系统配置的完整CRUD操作
 * 2. 管理配置缓存机制，提高配置访问性能
 * 3. 支持配置分类管理和条件查询
 * 4. 提供配置值获取的便捷方法
 * 5. 集成Redis缓存，支持配置热更新
 * 6. 支持配置状态控制和排序管理
 * 7. 提供配置变更审计日志功能
 * 
 * @author hanjor
 * @version 2.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.service.system;

import com.okbug.platform.dto.system.SystemConfigDTO;
import com.okbug.platform.dto.system.SystemConfigQueryDTO;
import com.okbug.platform.dto.system.SystemConfigCreateDTO;
import com.okbug.platform.dto.system.SystemConfigUpdateDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 系统配置服务接口
 * 定义系统配置管理的所有业务方法
 * 包括基础CRUD、配置值获取、缓存管理、状态控制、排序管理、审计日志等核心功能
 */
public interface SystemConfigService {
    
    /**
     * 分页查询配置列表
     * 
     * 查询功能：
     * 1. 支持按配置分类筛选
     * 2. 支持配置键和描述的模糊搜索
     * 3. 支持按配置类型筛选
     * 4. 支持按配置状态筛选
     * 5. 提供分页查询能力
     * 6. 结果按分类、排序字段、配置键排序
     * 
     * @param queryDto 查询条件DTO，包含筛选条件和分页参数
     * @return 分页结果，包含配置列表和分页信息
     */
    IPage<SystemConfigDTO> getConfigs(SystemConfigQueryDTO queryDto);
    
    /**
     * 根据ID获取配置
     * 
     * @param id 配置ID
     * @return 系统配置DTO，如果不存在则返回null
     */
    SystemConfigDTO getConfigById(Long id);
    
    /**
     * 根据配置键获取配置
     * 
     * @param configKey 配置键，如：user.operation.fail.max.count
     * @return 系统配置DTO，如果不存在则返回null
     */
    SystemConfigDTO getConfigByKey(String configKey);
    
    /**
     * 创建新配置
     * 
     * 创建逻辑：
     * 1. 验证配置键的唯一性
     * 2. 验证配置类型和分类的有效性
     * 3. 保存配置到数据库
     * 4. 更新Redis缓存
     * 5. 记录操作日志
     * 6. 记录配置变更审计日志
     * 
     * @param createDto 创建配置的请求参数
     * @return 创建成功的配置DTO
     * @throws ServiceException 当配置键已存在或参数无效时抛出
     */
    SystemConfigDTO createConfig(SystemConfigCreateDTO createDto);
    
    /**
     * 更新配置
     * 
     * 更新逻辑：
     * 1. 验证配置是否存在
     * 2. 只允许更新配置值、描述、状态、排序等字段
     * 3. 不允许修改配置键、类型、分类等核心属性
     * 4. 更新数据库记录
     * 5. 刷新Redis缓存
     * 6. 记录操作日志
     * 7. 记录配置变更审计日志
     * 
     * @param id 配置ID
     * @param updateDto 更新配置的请求参数
     * @return 更新后的配置DTO
     * @throws ServiceException 当配置不存在或参数无效时抛出
     */
    SystemConfigDTO updateConfig(Long id, SystemConfigUpdateDTO updateDto);
    
    /**
     * 删除配置
     * 
     * 删除逻辑：
     * 1. 执行逻辑删除（设置isDeleted = 1）
     * 2. 不清除Redis缓存，等待自然过期
     * 3. 记录操作日志
     * 4. 记录配置变更审计日志
     * 
     * @param id 配置ID
     * @return 删除操作是否成功
     * @throws ServiceException 当配置不存在时抛出
     */
    Boolean deleteConfig(Long id);
    
    /**
     * 批量删除配置（逻辑删除）
     * 
     * 删除逻辑：
     * 1. 将指定ID的配置 isDeleted 置为 1
     * 2. 清除对应缓存
     * 3. 记录审计日志
     * 
     * @param ids 配置ID列表
     * @return 成功删除的数量
     */
    Integer batchDelete(java.util.List<Long> ids);
    
    /**
     * 批量更新配置状态
     * 
     * @param ids 配置ID列表
     * @param status 目标状态，0:禁用，1:启用
     * @return 更新成功的数量
     */
    Integer batchUpdateStatus(List<Long> ids, Integer status);
    
    /**
     * 批量更新配置排序
     * 
     * @param idSortMap 配置ID和排序值的映射关系
     * @return 更新成功的数量
     */
    Integer batchUpdateSortOrder(java.util.Map<Long, Integer> idSortMap);
    
    /**
     * 根据配置键获取配置值（字符串）
     * 
     * 获取逻辑：
     * 1. 优先从Redis缓存获取
     * 2. 缓存未命中时从数据库查询
     * 3. 查询结果放入缓存
     * 4. 如果配置不存在或已禁用则返回null
     * 
     * @param configKey 配置键
     * @return 配置值，如果不存在或已禁用则返回null
     */
    String getConfigValue(String configKey);
    
    /**
     * 根据配置键获取配置值（字符串，带默认值）
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值，当配置不存在或已禁用时返回
     * @return 配置值或默认值
     */
    String getConfigValue(String configKey, String defaultValue);
    
    /**
     * 根据配置键获取配置值（整数）
     * 
     * @param configKey 配置键
     * @return 配置值转换为整数，如果不存在、已禁用或转换失败则返回null
     */
    Integer getConfigValueAsInt(String configKey);
    
    /**
     * 根据配置键获取配置值（整数，带默认值）
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值，当配置不存在、已禁用或转换失败时返回
     * @return 配置值或默认值
     */
    Integer getConfigValueAsInt(String configKey, Integer defaultValue);
    
    /**
     * 根据配置键获取配置值（布尔值）
     * 
     * @param configKey 配置键
     * @return 配置值转换为布尔值，如果不存在或已禁用则返回null
     */
    Boolean getConfigValueAsBoolean(String configKey);
    
    /**
     * 根据配置键获取配置值（布尔值，带默认值）
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值，当配置不存在或已禁用时返回
     * @return 配置值或默认值
     */
    Boolean getConfigValueAsBoolean(String configKey, Boolean defaultValue);
    
    /**
     * 根据配置键获取配置值（JSON对象）
     * 
     * @param configKey 配置键
     * @return 配置值转换为JSON对象，如果不存在、已禁用或转换失败则返回null
     */
    Object getConfigValueAsJson(String configKey);
    
    /**
     * 获取所有配置分类
     * 
     * @return 配置分类列表，如：["SYSTEM", "USER", "FILE", "TASK", "SECURITY", "CACHE"]
     */
    List<String> getConfigCategories();
    
    /**
     * 根据配置分类获取配置列表
     * 
     * @param category 配置分类
     * @return 该分类下的所有配置列表
     */
    List<SystemConfigDTO> getConfigsByCategory(String category);
    
    /**
     * 根据配置分类编码获取配置列表
     * 
     * @param categoryCode 配置分类编码（字典码，如 DICT_1.8）
     * @return 该分类编码下的所有启用配置列表
     */
    List<SystemConfigDTO> getConfigsByCategoryCode(String categoryCode);
    
    /**
     * 刷新配置缓存
     * 
     * 刷新逻辑：
     * 1. 清除所有配置相关的Redis缓存
     * 2. 下次访问时重新从数据库加载并缓存
     * 3. 用于配置变更后的缓存同步
     */
    void refreshConfigCache();
    
    /**
     * 清除配置缓存
     * 
     * 清除逻辑：
     * 1. 清除指定配置键的Redis缓存
     * 2. 下次访问时重新从数据库加载并缓存
     * 3. 用于单个配置变更后的缓存同步
     * 
     * @param configKey 配置键，如果为null则清除所有配置缓存
     */
    void clearConfigCache(String configKey);
} 
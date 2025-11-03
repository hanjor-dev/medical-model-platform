/**
 * 系统配置服务实现类：系统配置业务逻辑实现
 * 
 * 核心功能：
 * 1. 实现系统配置的完整CRUD操作
 * 2. 管理Redis缓存机制，提高配置访问性能
 * 3. 支持配置分类管理和条件查询
 * 4. 提供配置值获取的便捷方法
 * 5. 集成缓存更新，支持配置热更新
 * 6. 支持配置状态控制和排序管理
 * 7. 集成配置验证和审计日志功能
 * 
 * @author hanjor
 * @version 2.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.dto.system.SystemConfigDTO;
import com.okbug.platform.dto.system.SystemConfigQueryDTO;
import com.okbug.platform.dto.system.SystemConfigCreateDTO;
import com.okbug.platform.dto.system.SystemConfigUpdateDTO;
import com.okbug.platform.entity.system.SystemConfig;
import com.okbug.platform.mapper.system.SystemConfigMapper;
import com.okbug.platform.service.system.SystemConfigService;
import com.okbug.platform.service.system.ConfigValidationService;
import com.okbug.platform.service.system.ConfigAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 系统配置服务实现类
 * 实现SystemConfigService接口定义的所有业务方法
 * 使用Redis缓存提高配置访问性能，集成配置验证和审计功能
 */
@Service
@Slf4j
public class SystemConfigServiceImpl implements SystemConfigService {
    
    @Autowired
    private SystemConfigMapper systemConfigMapper;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private ConfigValidationService configValidationService;
    
    @Autowired
    private ConfigAuditService configAuditService;
    
    /**
     * Redis缓存配置
     */
    private static final String CONFIG_CACHE_PREFIX = "system:config:";
    
    @Override
    public IPage<SystemConfigDTO> getConfigs(SystemConfigQueryDTO queryDto) {
        log.debug("开始查询系统配置，查询条件: {}", queryDto);
        
        // 构建查询条件
        LambdaQueryWrapper<SystemConfig> query = new LambdaQueryWrapper<>();
        query.eq(SystemConfig::getIsDeleted, 0);
        
        // 配置分类筛选（优先使用分类编码，其次使用分类名称）
        if (StringUtils.hasText(queryDto.getConfigCategoryCode())) {
            query.eq(SystemConfig::getConfigCategoryCode, queryDto.getConfigCategoryCode());
        } else if (StringUtils.hasText(queryDto.getConfigCategory())) {
            query.eq(SystemConfig::getConfigCategory, queryDto.getConfigCategory());
        }
        
        // 关键词搜索：当同时提供configKey与description时，采用 (key LIKE ? OR description LIKE ?) 组合
        boolean hasKeyKeyword = StringUtils.hasText(queryDto.getConfigKey());
        boolean hasDescKeyword = StringUtils.hasText(queryDto.getDescription());
        if (hasKeyKeyword && hasDescKeyword) {
            query.and(q -> q.like(SystemConfig::getConfigKey, queryDto.getConfigKey())
                            .or()
                            .like(SystemConfig::getDescription, queryDto.getDescription()));
        } else if (hasKeyKeyword) {
            query.like(SystemConfig::getConfigKey, queryDto.getConfigKey());
        } else if (hasDescKeyword) {
            query.like(SystemConfig::getDescription, queryDto.getDescription());
        }
        
        // 配置类型筛选
        if (StringUtils.hasText(queryDto.getConfigType())) {
            query.eq(SystemConfig::getConfigType, queryDto.getConfigType());
        }
        
        // 配置状态筛选
        if (queryDto.getStatus() != null) {
            query.eq(SystemConfig::getStatus, queryDto.getStatus());
        }
        
        // 排序处理
        if (StringUtils.hasText(queryDto.getSortField())) {
            String sortField = queryDto.getSortField();
            if ("configKey".equals(sortField)) {
                query.orderBy(true, "asc".equals(queryDto.getSortDirection()), SystemConfig::getConfigKey);
            } else if ("configCategory".equals(sortField)) {
                query.orderBy(true, "asc".equals(queryDto.getSortDirection()), SystemConfig::getConfigCategory);
            } else if ("configCategoryCode".equals(sortField)) {
                query.orderBy(true, "asc".equals(queryDto.getSortDirection()), SystemConfig::getConfigCategoryCode);
            } else if ("sortOrder".equals(sortField)) {
                query.orderBy(true, "asc".equals(queryDto.getSortDirection()), SystemConfig::getSortOrder);
            } else if ("createTime".equals(sortField)) {
                query.orderBy(true, "asc".equals(queryDto.getSortDirection()), SystemConfig::getCreateTime);
            } else if ("updateTime".equals(sortField)) {
                query.orderBy(true, "asc".equals(queryDto.getSortDirection()), SystemConfig::getUpdateTime);
            } else {
                // 默认排序：先按分类编码，再按分类名，再按排序字段，最后按配置键
                query.orderByAsc(SystemConfig::getConfigCategoryCode, SystemConfig::getConfigCategory, SystemConfig::getSortOrder, SystemConfig::getConfigKey);
            }
        } else {
            // 默认排序：先按分类编码，再按分类名，再按排序字段，最后按配置键
            query.orderByAsc(SystemConfig::getConfigCategoryCode, SystemConfig::getConfigCategory, SystemConfig::getSortOrder, SystemConfig::getConfigKey);
        }
        
        // 分页查询
        Page<SystemConfig> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        IPage<SystemConfig> result = systemConfigMapper.selectPage(page, query);
        
        // 转换为DTO
        List<SystemConfigDTO> configs = result.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        // 构建返回结果
        Page<SystemConfigDTO> dtoPage = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        dtoPage.setRecords(configs);
        dtoPage.setTotal(result.getTotal());
        dtoPage.setCurrent(result.getCurrent());
        dtoPage.setSize(result.getSize());
        
        log.debug("系统配置查询完成，共查询到 {} 条记录", result.getTotal());
        return dtoPage;
    }
    
    @Override
    public SystemConfigDTO getConfigById(Long id) {
        log.debug("根据ID查询系统配置，ID: {}", id);
        
        SystemConfig config = systemConfigMapper.selectById(id);
        if (config == null || config.getIsDeleted() == 1) {
            log.warn("系统配置不存在或已删除，ID: {}", id);
            throw new ServiceException(ErrorCode.DATA_NOT_FOUND, "系统配置不存在");
        }
        
        return convertToDTO(config);
    }
    
    @Override
    public SystemConfigDTO getConfigByKey(String configKey) {
        log.debug("根据配置键查询系统配置，配置键: {}", configKey);
        
        SystemConfig config = systemConfigMapper.selectByConfigKey(configKey);
        if (config == null) {
            log.warn("系统配置不存在，配置键: {}", configKey);
            throw new ServiceException(ErrorCode.DATA_NOT_FOUND, "系统配置不存在");
        }
        
        return convertToDTO(config);
    }
    
    @Override
    public SystemConfigDTO createConfig(SystemConfigCreateDTO createDto) {
        log.info("开始创建系统配置，配置键: {}, 分类: {}", createDto.getConfigKey(), createDto.getConfigCategory());
        
        // 配置验证
        configValidationService.validateCreateRequest(createDto);
        
        // 检查配置键是否已存在
        if (systemConfigMapper.countByConfigKey(createDto.getConfigKey()) > 0) {
            log.error("系统配置键已存在，配置键: {}", createDto.getConfigKey());
            throw new ServiceException(ErrorCode.DATA_ALREADY_EXISTS, "配置键已存在");
        }
        
        // 创建配置对象
        SystemConfig config = new SystemConfig();
        config.setConfigKey(createDto.getConfigKey());
        config.setConfigValue(createDto.getConfigValue());
        config.setConfigType(createDto.getConfigType());
        // 保存类型与分类编码（若传入）
        if (org.springframework.util.StringUtils.hasText(createDto.getConfigTypeCode())) {
            config.setConfigTypeCode(createDto.getConfigTypeCode());
        }
        config.setConfigCategory(createDto.getConfigCategory());
        if (org.springframework.util.StringUtils.hasText(createDto.getConfigCategoryCode())) {
            config.setConfigCategoryCode(createDto.getConfigCategoryCode());
        }
        config.setDescription(createDto.getDescription());
        config.setStatus(createDto.getStatus() != null ? createDto.getStatus() : 1); // 默认启用
        config.setSortOrder(createDto.getSortOrder() != null ? createDto.getSortOrder() : 0); // 默认排序
        config.setIsDeleted(0);
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        
        // 保存到数据库
        systemConfigMapper.insert(config);
        
        // 更新缓存
        updateConfigCache(config.getConfigKey(), config.getConfigValue());
        
        // 记录审计日志
        configAuditService.logConfigCreate(config.getId(), config.getConfigKey(), config.getConfigValue(), 
                                         createDto.getOperatorId(), createDto.getOperatorName(), createDto.getIpAddress());
        
        log.info("系统配置创建成功，配置键: {}, 分类: {}, ID: {}", 
            config.getConfigKey(), config.getConfigCategory(), config.getId());
        
        return convertToDTO(config);
    }
    
    @Override
    public SystemConfigDTO updateConfig(Long id, SystemConfigUpdateDTO updateDto) {
        log.info("开始更新系统配置，ID: {}, 新值: {}", id, updateDto.getConfigValue());
        
        // 获取配置
        SystemConfig config = systemConfigMapper.selectById(id);
        if (config == null || config.getIsDeleted() == 1) {
            log.error("系统配置不存在或已删除，ID: {}", id);
            throw new ServiceException(ErrorCode.DATA_NOT_FOUND, "配置不存在");
        }
        
        // 配置验证
        configValidationService.validateUpdateRequest(updateDto, config);
        
        // 保存旧值用于审计日志
        String oldValue = config.getConfigValue();
        
        // 更新配置
        if (updateDto.getConfigValue() != null) {
            config.setConfigValue(updateDto.getConfigValue());
        }
        if (updateDto.getDescription() != null) {
            config.setDescription(updateDto.getDescription());
        }
        if (updateDto.getStatus() != null) {
            config.setStatus(updateDto.getStatus());
        }
        if (updateDto.getSortOrder() != null) {
            config.setSortOrder(updateDto.getSortOrder());
        }
        config.setUpdateTime(LocalDateTime.now());
        
        // 更新数据库
        systemConfigMapper.updateById(config);
        
        // 更新缓存
        updateConfigCache(config.getConfigKey(), config.getConfigValue());
        
        // 记录审计日志
        configAuditService.logConfigUpdate(config.getId(), config.getConfigKey(), oldValue, config.getConfigValue(),
                                         updateDto.getOperatorId(), updateDto.getOperatorName(), updateDto.getIpAddress());
        
        log.info("系统配置更新成功，配置键: {}, 新值: {}, ID: {}", 
            config.getConfigKey(), config.getConfigValue(), config.getId());
        
        return convertToDTO(config);
    }
    
    @Override
    public Boolean deleteConfig(Long id) {
        log.info("开始删除系统配置，ID: {}", id);
        
        // 逻辑删除配置
        SystemConfig config = systemConfigMapper.selectById(id);
        if (config == null || config.getIsDeleted() == 1) {
            log.error("系统配置不存在或已删除，ID: {}", id);
            throw new ServiceException(ErrorCode.DATA_NOT_FOUND, "配置不存在");
        }
        
        String oldValue = config.getConfigValue();
        config.setIsDeleted(1);
        config.setUpdateTime(LocalDateTime.now());
        systemConfigMapper.updateById(config);
        
        // 清除缓存
        clearConfigCache(config.getConfigKey());
        
        // 记录审计日志
        configAuditService.logConfigDelete(config.getId(), config.getConfigKey(), oldValue,
                                         null, null, null); // TODO: 获取操作人信息
        
        log.info("系统配置删除成功，配置键: {}, ID: {}", config.getConfigKey(), config.getId());
        
        return true;
    }

    @Override
    public Integer batchDelete(List<Long> ids) {
        log.info("开始批量删除系统配置，ID数量: {}", ids == null ? 0 : ids.size());
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        int deleteCount = 0;
        for (Long id : ids) {
            SystemConfig config = systemConfigMapper.selectById(id);
            if (config == null || config.getIsDeleted() == 1) {
                continue;
            }
            String oldValue = config.getConfigValue();
            config.setIsDeleted(1);
            config.setUpdateTime(LocalDateTime.now());
            systemConfigMapper.updateById(config);

            // 清除缓存
            clearConfigCache(config.getConfigKey());

            // 审计日志（操作人信息待集成）
            configAuditService.logConfigDelete(config.getId(), config.getConfigKey(), oldValue,
                    null, null, null);
            deleteCount++;
        }

        log.info("批量删除系统配置完成，成功删除: {} 条", deleteCount);
        return deleteCount;
    }
    
    @Override
    public Integer batchUpdateStatus(List<Long> ids, Integer status) {
        log.info("开始批量更新配置状态，配置ID: {}, 目标状态: {}", ids, status);
        
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        // 验证状态值
        if (status != null && status != 0 && status != 1) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "状态值无效，只能是0或1");
        }
        
        // 批量更新状态
        int updateCount = systemConfigMapper.batchUpdateStatus(ids, status);
        
        // 清除相关缓存
        for (Long id : ids) {
            SystemConfig config = systemConfigMapper.selectById(id);
            if (config != null) {
                clearConfigCache(config.getConfigKey());
            }
        }
        
        log.info("批量更新配置状态完成，更新数量: {}", updateCount);
        return updateCount;
    }
    
    @Override
    public Integer batchUpdateSortOrder(java.util.Map<Long, Integer> idSortMap) {
        log.info("开始批量更新配置排序，配置数量: {}", idSortMap.size());
        
        if (idSortMap == null || idSortMap.isEmpty()) {
            return 0;
        }
        
        // 批量更新排序
        int updateCount = systemConfigMapper.batchUpdateSortOrder(idSortMap);
        
        // 清除相关缓存
        for (Long id : idSortMap.keySet()) {
            SystemConfig config = systemConfigMapper.selectById(id);
            if (config != null) {
                clearConfigCache(config.getConfigKey());
            }
        }
        
        log.info("批量更新配置排序完成，更新数量: {}", updateCount);
        return updateCount;
    }
    
    @Override
    public String getConfigValue(String configKey) {
        return getConfigValue(configKey, null);
    }
    
    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        // 先从缓存获取
        String cachedValue = redisTemplate.opsForValue().get(CONFIG_CACHE_PREFIX + configKey);
        if (cachedValue != null) {
            log.debug("从缓存获取配置值，配置键: {}, 值: {}", configKey, cachedValue);
            return cachedValue;
        }
        
        // 缓存未命中，从数据库获取
        SystemConfig config = systemConfigMapper.selectByConfigKey(configKey);
        if (config != null && config.getStatus() == 1) { // 只返回启用的配置
            // 放入缓存
            updateConfigCache(configKey, config.getConfigValue());
            log.debug("从数据库获取配置值并缓存，配置键: {}, 值: {}", configKey, config.getConfigValue());
            return config.getConfigValue();
        }
        
        log.debug("配置不存在或已禁用，配置键: {}, 返回默认值: {}", configKey, defaultValue);
        return defaultValue;
    }
    
    @Override
    public Integer getConfigValueAsInt(String configKey) {
        return getConfigValueAsInt(configKey, null);
    }
    
    @Override
    public Integer getConfigValueAsInt(String configKey, Integer defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("配置值转换整数失败，配置键: {}, 值: {}", configKey, value);
            return defaultValue;
        }
    }
    
    @Override
    public Boolean getConfigValueAsBoolean(String configKey) {
        return getConfigValueAsBoolean(configKey, null);
    }
    
    @Override
    public Boolean getConfigValueAsBoolean(String configKey, Boolean defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        
        return Boolean.parseBoolean(value);
    }
    
    @Override
    public Object getConfigValueAsJson(String configKey) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return null;
        }
        
        try {
            // 使用Jackson或其他JSON库解析
            // 这里简化处理，实际项目中应该使用专门的JSON解析库
            return value;
        } catch (Exception e) {
            log.warn("配置值转换JSON失败，配置键: {}, 值: {}", configKey, value);
            return null;
        }
    }
    
    @Override
    public List<String> getConfigCategories() {
        log.debug("开始获取配置分类列表");
        
        // 查询所有配置分类
        LambdaQueryWrapper<SystemConfig> query = new LambdaQueryWrapper<>();
        query.select(SystemConfig::getConfigCategory)
             .eq(SystemConfig::getIsDeleted, 0)
             .eq(SystemConfig::getStatus, 1) // 只统计启用的配置
             .groupBy(SystemConfig::getConfigCategory)
             .orderByAsc(SystemConfig::getConfigCategory);
        
        List<SystemConfig> configs = systemConfigMapper.selectList(query);
        List<String> categories = configs.stream()
            .map(SystemConfig::getConfigCategory)
            .collect(Collectors.toList());
        
        log.debug("配置分类列表获取完成，共 {} 个分类: {}", categories.size(), categories);
        return categories;
    }
    
    @Override
    public List<SystemConfigDTO> getConfigsByCategory(String category) {
        log.debug("根据分类获取配置列表，分类: {}", category);
        
        LambdaQueryWrapper<SystemConfig> query = new LambdaQueryWrapper<>();
        query.eq(SystemConfig::getIsDeleted, 0)
             .eq(SystemConfig::getStatus, 1) // 只返回启用的配置
             .eq(SystemConfig::getConfigCategory, category)
             .orderByAsc(SystemConfig::getSortOrder, SystemConfig::getConfigKey);
        
        List<SystemConfig> configs = systemConfigMapper.selectList(query);
        List<SystemConfigDTO> configDtos = configs.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        log.debug("分类配置列表获取完成，分类: {}, 数量: {}", category, configDtos.size());
        return configDtos;
    }

    @Override
    public List<SystemConfigDTO> getConfigsByCategoryCode(String categoryCode) {
        log.debug("根据分类编码获取配置列表，分类编码: {}", categoryCode);

        if (!StringUtils.hasText(categoryCode)) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "分类编码不能为空");
        }

        LambdaQueryWrapper<SystemConfig> query = new LambdaQueryWrapper<>();
        query.eq(SystemConfig::getIsDeleted, 0)
             .eq(SystemConfig::getStatus, 1)
             .eq(SystemConfig::getConfigCategoryCode, categoryCode)
             .orderByAsc(SystemConfig::getSortOrder, SystemConfig::getConfigKey);

        List<SystemConfig> configs = systemConfigMapper.selectList(query);
        List<SystemConfigDTO> configDtos = configs.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        log.debug("分类编码配置列表获取完成，分类编码: {}, 数量: {}", categoryCode, configDtos.size());
        return configDtos;
    }
    
    @Override
    public void refreshConfigCache() {
        log.info("开始刷新系统配置缓存");
        
        // 清除所有配置相关的Redis缓存
        Set<String> keys = redisTemplate.keys(CONFIG_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("系统配置缓存刷新完成，清除 {} 个缓存键", keys.size());
        } else {
            log.info("系统配置缓存刷新完成，无缓存需要清除");
        }
    }
    
    @Override
    public void clearConfigCache(String configKey) {
        if (configKey != null) {
            // 清除指定配置的缓存
            String cacheKey = CONFIG_CACHE_PREFIX + configKey;
            redisTemplate.delete(cacheKey);
            log.debug("清除配置缓存，配置键: {}", configKey);
        } else {
            // 清除所有配置缓存
            refreshConfigCache();
        }
    }
    
    /**
     * 更新配置缓存：写入 Redis，默认 TTL 1 小时。
     *
     * @param configKey   配置键
     * @param configValue 配置值
     */
    private void updateConfigCache(String configKey, String configValue) {
        try {
            String cacheKey = CONFIG_CACHE_PREFIX + configKey;
            // 使用默认缓存TTL 1小时，避免循环依赖
            int cacheTtl = 3600;
            redisTemplate.opsForValue().set(cacheKey, configValue, cacheTtl, TimeUnit.SECONDS);
            log.debug("更新配置缓存成功，配置键: {}, 值: {}, TTL: {}秒", configKey, configValue, cacheTtl);
        } catch (Exception e) {
            log.error("更新配置缓存失败，配置键: {}, 错误: {}", configKey, e.getMessage(), e);
        }
    }
    
    /**
     * 实体转 DTO（私有方法）。
     *
     * @param config 系统配置实体
     * @return 系统配置DTO
     */
    private SystemConfigDTO convertToDTO(SystemConfig config) {
        return SystemConfigDTO.builder()
            .id(config.getId())
            .configKey(config.getConfigKey())
            .configValue(config.getConfigValue())
            .configType(config.getConfigType())
            .configTypeCode(config.getConfigTypeCode())
            .configCategoryCode(config.getConfigCategoryCode())
            .configCategory(config.getConfigCategory())
            .description(config.getDescription())
            .status(config.getStatus())
            .sortOrder(config.getSortOrder())
            .createTime(config.getCreateTime())
            .updateTime(config.getUpdateTime())
            .build();
    }
} 
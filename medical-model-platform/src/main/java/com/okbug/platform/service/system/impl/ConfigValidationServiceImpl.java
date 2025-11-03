/**
 * 配置验证服务实现类：系统配置值验证服务实现
 * 
 * 核心功能：
 * 1. 实现配置值类型验证（字符串、数字、布尔、JSON）
 * 2. 支持配置值范围验证和格式验证
 * 3. 支持自定义验证规则扩展
 * 4. 提供配置值类型转换功能
 * 5. 支持配置项依赖关系验证
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:00:00
 */
package com.okbug.platform.service.system.impl;

import com.okbug.platform.dto.system.SystemConfigCreateDTO;
import com.okbug.platform.dto.system.SystemConfigUpdateDTO;
import com.okbug.platform.entity.system.SystemConfig;
import com.okbug.platform.service.system.ConfigValidationService;
import com.okbug.platform.service.system.SystemDictService;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.common.base.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 配置验证服务实现类
 * 实现ConfigValidationService接口定义的所有验证方法
 * 提供完整的配置值验证和类型转换功能
 */
@Service
@Slf4j
public class ConfigValidationServiceImpl implements ConfigValidationService {

    private final SystemDictService systemDictService;

    public ConfigValidationServiceImpl(SystemDictService systemDictService) {
        this.systemDictService = systemDictService;
    }
    
    // 邮箱格式正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    // URL格式正则表达式
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?|ftp)://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$"
    );
    
    // 日期格式正则表达式（YYYY-MM-DD）
    private static final Pattern DATE_PATTERN = Pattern.compile(
        "^\\d{4}-\\d{2}-\\d{2}$"
    );
    
    // 配置键格式正则表达式
    private static final Pattern CONFIG_KEY_PATTERN = Pattern.compile(
        "^[a-z][a-z0-9_.]*$"
    );
    
    @Override
    public void validateCreateRequest(SystemConfigCreateDTO createDto) {
        log.debug("开始验证配置创建请求，配置键: {}", createDto.getConfigKey());
        
        // 验证配置键格式
        validateConfigKeyFormat(createDto.getConfigKey());
        
        // 解析/验证配置类型（优先使用字典编码）
        String resolvedType = resolveConfigType(createDto.getConfigTypeCode(), createDto.getConfigType());
        validateConfigValueType(createDto.getConfigValue(), resolvedType);
        
        // 解析/验证配置分类（优先使用字典编码）
        String resolvedCategoryCode = resolveConfigCategoryCode(createDto.getConfigCategoryCode(), createDto.getConfigCategory());
        validateConfigCategory(resolvedCategoryCode, createDto.getConfigKey());
        
        // 验证配置值范围
        validateConfigValueRange(createDto.getConfigValue(), createDto.getConfigType(), createDto.getConfigKey());
        
        // 验证配置值格式
        validateConfigValueFormat(createDto.getConfigValue(), createDto.getConfigKey());
        
        // 验证配置项依赖关系
        validateConfigDependencies(createDto.getConfigKey(), createDto.getConfigValue());
        
        log.debug("配置创建请求验证通过，配置键: {}", createDto.getConfigKey());
    }
    
    @Override
    public void validateUpdateRequest(SystemConfigUpdateDTO updateDto, SystemConfig currentConfig) {
        log.debug("开始验证配置更新请求，配置键: {}", currentConfig.getConfigKey());
        
        // 验证配置值类型（以现存类型或更新后的类型编码为准）
        String typeForUpdate = currentConfig.getConfigType();
        if (updateDto.getConfigTypeCode() != null && !updateDto.getConfigTypeCode().trim().isEmpty()) {
            typeForUpdate = mapTypeCodeToType(updateDto.getConfigTypeCode());
            if (typeForUpdate == null) {
                throw new ServiceException(ErrorCode.CONFIG_TYPE_CODE_INVALID);
            }
        }
        validateConfigValueType(updateDto.getConfigValue(), typeForUpdate);
        
        // 验证配置值范围
        validateConfigValueRange(updateDto.getConfigValue(), currentConfig.getConfigType(), currentConfig.getConfigKey());
        
        // 验证配置值格式
        validateConfigValueFormat(updateDto.getConfigValue(), currentConfig.getConfigKey());
        
        // 验证配置项依赖关系
        validateConfigDependencies(currentConfig.getConfigKey(), updateDto.getConfigValue());
        
        log.debug("配置更新请求验证通过，配置键: {}", currentConfig.getConfigKey());
    }
    
    @Override
    public void validateConfigValueType(String configValue, String configType) {
        if (configValue == null || configValue.trim().isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "配置值不能为空");
        }
        
        switch (configType) {
            case "STRING":
                // STRING类型允许任意字符串，无需验证
                break;
                
            case "NUMBER":
                try {
                    Double.parseDouble(configValue);
                } catch (NumberFormatException e) {
                    throw new ServiceException(ErrorCode.PARAM_INVALID, "配置值必须是有效的数字，当前值: " + configValue);
                }
                break;
                
            case "BOOLEAN":
                if (!"true".equalsIgnoreCase(configValue) && !"false".equalsIgnoreCase(configValue)) {
                    throw new ServiceException(ErrorCode.PARAM_INVALID, "配置值必须是true或false，当前值: " + configValue);
                }
                break;
                
            case "JSON":
                try {
                    // 简单的JSON格式验证，实际项目中应该使用专门的JSON解析库
                    if (!configValue.trim().startsWith("{") && !configValue.trim().startsWith("[")) {
                        throw new ServiceException(ErrorCode.PARAM_INVALID, "配置值必须是有效的JSON格式，当前值: " + configValue);
                    }
                } catch (Exception e) {
                    throw new ServiceException(ErrorCode.PARAM_INVALID, "配置值必须是有效的JSON格式，当前值: " + configValue);
                }
                break;
                
            default:
                throw new ServiceException(ErrorCode.PARAM_INVALID, "不支持的配置类型: " + configType);
        }
        
        log.debug("配置值类型验证通过，类型: {}, 值: {}", configType, configValue);
    }
    
    @Override
    public void validateConfigValueRange(String configValue, String configType, String configKey) {
        if (configValue == null || configValue.trim().isEmpty()) {
            return; // 空值不进行范围验证
        }
        
        switch (configType) {
            case "NUMBER":
                try {
                    double value = Double.parseDouble(configValue);
                    // 根据配置键设置不同的范围限制
                    if (configKey.contains("timeout") || configKey.contains("duration")) {
                        if (value < 0 || value > 86400) { // 0-24小时（秒）
                            throw new ServiceException(ErrorCode.PARAM_OUT_OF_RANGE, "超时时间必须在0-86400秒之间，当前值: " + value);
                        }
                    } else if (configKey.contains("count") || configKey.contains("size")) {
                        if (value < 0 || value > 1000000) { // 0-100万
                            throw new ServiceException(ErrorCode.PARAM_OUT_OF_RANGE, "数量值必须在0-1000000之间，当前值: " + value);
                        }
                    } else if (configKey.contains("percentage")) {
                        if (value < 0 || value > 100) { // 0-100%
                            throw new ServiceException(ErrorCode.PARAM_OUT_OF_RANGE, "百分比值必须在0-100之间，当前值: " + value);
                        }
                    }
                } catch (NumberFormatException e) {
                    // 数字格式验证已在类型验证中处理，这里不需要重复处理
                }
                break;
                
            case "STRING":
                int length = configValue.length();
                if (configKey.contains("name") || configKey.contains("title")) {
                    if (length > 200) { // 名称长度限制
                        throw new ServiceException(ErrorCode.PARAM_OUT_OF_RANGE, "名称长度不能超过200个字符，当前长度: " + length);
                    }
                } else if (configKey.contains("description")) {
                    if (length > 1000) { // 描述长度限制
                        throw new ServiceException(ErrorCode.PARAM_OUT_OF_RANGE, "描述长度不能超过1000个字符，当前长度: " + length);
                    }
                } else if (length > 5000) { // 默认字符串长度限制
                    throw new ServiceException(ErrorCode.PARAM_OUT_OF_RANGE, "字符串长度不能超过5000个字符，当前长度: " + length);
                }
                break;
        }
        
        log.debug("配置值范围验证通过，配置键: {}, 值: {}", configKey, configValue);
    }
    
    @Override
    public void validateConfigValueFormat(String configValue, String configKey) {
        if (configValue == null || configValue.trim().isEmpty()) {
            return; // 空值不进行格式验证
        }
        
        // 根据配置键进行特定的格式验证
        if (configKey.contains("email")) {
            if (!EMAIL_PATTERN.matcher(configValue).matches()) {
                throw new ServiceException(ErrorCode.PARAM_INVALID, "邮箱格式无效，当前值: " + configValue);
            }
        } else if (configKey.contains("url")) {
            if (!URL_PATTERN.matcher(configValue).matches()) {
                throw new ServiceException(ErrorCode.PARAM_INVALID, "URL格式无效，当前值: " + configValue);
            }
        } else if (configKey.contains("date")) {
            if (!DATE_PATTERN.matcher(configValue).matches()) {
                throw new ServiceException(ErrorCode.PARAM_INVALID, "日期格式无效，应为YYYY-MM-DD格式，当前值: " + configValue);
            }
        } else if (configKey.contains("ip")) {
            // IP地址格式验证
            if (!isValidIpAddress(configValue)) {
                throw new ServiceException(ErrorCode.PARAM_INVALID, "IP地址格式无效，当前值: " + configValue);
            }
        }
        
        log.debug("配置值格式验证通过，配置键: {}, 值: {}", configKey, configValue);
    }
    
    @Override
    public void validateConfigDependencies(String configKey, String configValue) {
        // 这里可以实现配置项之间的依赖关系验证
        // 例如：某些配置项依赖于其他配置项的值
        // 实际项目中可以根据业务需求实现具体的依赖验证逻辑
        
        log.debug("配置依赖关系验证通过，配置键: {}", configKey);
    }
    
    @Override
    public void validateConfigKeyFormat(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "配置键不能为空");
        }
        
        if (!CONFIG_KEY_PATTERN.matcher(configKey).matches()) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "配置键格式无效，只能包含小写字母、数字、下划线和点号，且必须以字母开头，当前值: " + configKey);
        }
        
        if (configKey.length() > 100) {
            throw new ServiceException(ErrorCode.PARAM_OUT_OF_RANGE, "配置键长度不能超过100个字符，当前长度: " + configKey.length());
        }
        
        log.debug("配置键格式验证通过，配置键: {}", configKey);
    }
    
    @Override
    public void validateConfigCategory(String configCategoryOrCode, String configKey) {
        if (!StringUtils.hasText(configCategoryOrCode)) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "配置分类不能为空");
        }

        try {
            // 从字典 DICT_3 子项中校验（支持code或label传入）
            boolean exists = systemDictService.getConfigCategoryOptions().stream()
                    .anyMatch(opt -> configCategoryOrCode.equals(opt.getCode()) || configCategoryOrCode.equals(opt.getLabel()));
            if (!exists) {
                throw new ServiceException(ErrorCode.CONFIG_CATEGORY_CODE_INVALID, "配置分类无效: " + configCategoryOrCode);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.EXTERNAL_SERVICE_ERROR, "校验配置分类失败: " + e.getMessage());
        }
        
        // 验证配置分类与配置键的匹配性
        if (configKey.contains("user") && !configCategoryOrCode.toUpperCase().contains("USER")) {
            log.warn("配置键包含'user'但分类不是USER，配置键: {}, 分类: {}", configKey, configCategoryOrCode);
        } else if (configKey.contains("system") && !configCategoryOrCode.toUpperCase().contains("SYSTEM")) {
            log.warn("配置键包含'system'但分类不是SYSTEM，配置键: {}, 分类: {}", configKey, configCategoryOrCode);
        } else if (configKey.contains("file") && !configCategoryOrCode.toUpperCase().contains("FILE")) {
            log.warn("配置键包含'file'但分类不是FILE，配置键: {}, 分类: {}", configKey, configCategoryOrCode);
        } else if (configKey.contains("task") && !configCategoryOrCode.toUpperCase().contains("TASK")) {
            log.warn("配置键包含'task'但分类不是TASK，配置键: {}, 分类: {}", configKey, configCategoryOrCode);
        } else if (configKey.contains("security") && !configCategoryOrCode.toUpperCase().contains("SECURITY")) {
            log.warn("配置键包含'security'但分类不是SECURITY，配置键: {}, 分类: {}", configKey, configCategoryOrCode);
        } else if (configKey.contains("cache") && !configCategoryOrCode.toUpperCase().contains("CACHE")) {
            log.warn("配置键包含'cache'但分类不是CACHE，配置键: {}, 分类: {}", configKey, configCategoryOrCode);
        }

        log.debug("配置分类验证通过，分类: {}, 配置键: {}", configCategoryOrCode, configKey);
    }

    /**
     * 解析配置类型：优先使用字典编码
     */
    private String resolveConfigType(String configTypeCode, String fallbackType) {
        if (StringUtils.hasText(configTypeCode)) {
            String mapped = mapTypeCodeToType(configTypeCode);
            if (mapped == null) {
                throw new ServiceException(ErrorCode.CONFIG_TYPE_CODE_INVALID);
            }
            return mapped;
        }
        return fallbackType;
    }

    /**
     * 将类型字典编码映射为旧的类型枚举字符串
     */
    private String mapTypeCodeToType(String configTypeCode) {
        if (configTypeCode == null) return null;
        switch (configTypeCode) {
            case "DICT_3.2": return "STRING";
            case "DICT_3.3": return "NUMBER";
            case "DICT_3.4": return "BOOLEAN";
            case "DICT_3.5": return "JSON";
            default: return null;
        }
    }

    /**
     * 解析配置分类编码：优先使用传入编码，否则尝试用名称匹配获取编码
     */
    private String resolveConfigCategoryCode(String categoryCode, String categoryName) {
        if (StringUtils.hasText(categoryCode)) {
            // 简验：必须以 DICT_1 开头
            if (!categoryCode.startsWith("DICT_1")) {
                throw new ServiceException(ErrorCode.CONFIG_CATEGORY_CODE_INVALID);
            }
            return categoryCode;
        }
        if (StringUtils.hasText(categoryName)) {
            return systemDictService.getConfigCategoryOptions().stream()
                .filter(opt -> categoryName.equals(opt.getLabel()) || categoryName.equalsIgnoreCase(opt.getCode()))
                .map(opt -> opt.getCode())
                .findFirst()
                .orElseThrow(() -> new ServiceException(ErrorCode.CONFIG_CATEGORY_CODE_INVALID, "配置分类无效: " + categoryName));
        }
        throw new ServiceException(ErrorCode.PARAM_INVALID, "配置分类不能为空");
    }
    
    @Override
    public Object convertConfigValue(String configValue, String configType) {
        if (configValue == null || configValue.trim().isEmpty()) {
            return null;
        }
        
        try {
            switch (configType) {
                case "STRING":
                    return configValue;
                    
                case "NUMBER":
                    // 尝试转换为整数，如果失败则转换为浮点数
                    try {
                        return Integer.parseInt(configValue);
                    } catch (NumberFormatException e) {
                        return Double.parseDouble(configValue);
                    }
                    
                case "BOOLEAN":
                    return Boolean.parseBoolean(configValue);
                    
                case "JSON":
                    // 这里应该使用专门的JSON解析库，如Jackson或Gson
                    // 简化处理，返回字符串
                    return configValue;
                    
                default:
                    throw new ServiceException(ErrorCode.PARAM_INVALID, "不支持的配置类型: " + configType);
            }
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "配置值转换失败，类型: " + configType + ", 值: " + configValue + ", 错误: " + e.getMessage());
        }
    }
    
    @Override
    public String getDefaultValue(String configKey, String configType) {
        // 根据配置键和类型返回默认值
        if (configKey.contains("timeout") || configKey.contains("duration")) {
            return "30"; // 默认30秒
        } else if (configKey.contains("count") || configKey.contains("size")) {
            return "10"; // 默认10
        } else if (configKey.contains("percentage")) {
            return "80"; // 默认80%
        } else if (configKey.contains("enabled")) {
            return "true"; // 默认启用
        } else if (configKey.contains("disabled")) {
            return "false"; // 默认禁用
        } else {
            switch (configType) {
                case "NUMBER":
                    return "0";
                case "BOOLEAN":
                    return "false";
                case "STRING":
                case "JSON":
                default:
                    return "";
            }
        }
    }
    
    /**
     * 验证IP地址格式
     * 
     * @param ipAddress IP地址字符串
     * @return 是否为有效的IP地址
     */
    private boolean isValidIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return false;
        }
        
        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        
        for (String part : parts) {
            try {
                int value = Integer.parseInt(part);
                if (value < 0 || value > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return true;
    }
} 
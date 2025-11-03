/**
 * 字典工具类：提供便捷的字典访问方法
 * 
 * 核心功能：
 * 1. 提供静态方法快速获取字典数据
 * 2. 支持字典编码和标签的转换，使用path作为dictCode确保唯一性
 * 3. 集成Redis缓存，提高访问性能
 * 4. 支持批量字典数据获取
 * 5. 提供字典数据的本地缓存机制
 * 
 * @author hanjor
 * @version 2.0
 * @date 2025-01-15 00:23:00
 */
package com.okbug.platform.common.utils;

import com.okbug.platform.service.system.SystemDictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 字典工具类
 * 提供便捷的字典访问方法，支持本地缓存和Redis缓存
 * 使用Spring的@PostConstruct进行初始化，确保依赖注入完成
 */
@Component
@Slf4j
public class DictUtils {
    
    @Autowired
    private SystemDictService systemDictService;
    
    /**
     * 本地缓存，用于提高字典访问性能
     * 缓存结构：{dictCode -> {dictValue -> dictLabel}}
     */
    private static final Map<String, Map<String, String>> LOCAL_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 反向本地缓存，用于根据标签获取值
     * 缓存结构：{dictCode -> {dictLabel -> dictValue}}
     */
    private static final Map<String, Map<String, String>> REVERSE_LOCAL_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 本地缓存过期时间（毫秒）
     */
    private static final long CACHE_EXPIRE_TIME = 5 * 60 * 1000; // 5分钟
    
    /**
     * 缓存时间戳
     */
    private static final Map<String, Long> CACHE_TIMESTAMP = new ConcurrentHashMap<>();
    
    /**
     * 静态实例，用于静态方法调用
     */
    private static DictUtils instance;
    
    /**
     * 初始化方法，在依赖注入完成后执行
     */
    @PostConstruct
    public void init() {
        instance = this;
        log.info("DictUtils初始化完成");
    }

    /**
     * 刷新字典缓存
     * 
     * 清除所有本地缓存和Redis缓存
     */
    public static void refreshCache() {
        if (instance == null) {
            log.warn("DictUtils未初始化，无法刷新缓存");
            return;
        }
        
        try {
            // 清除本地缓存
            LOCAL_CACHE.clear();
            REVERSE_LOCAL_CACHE.clear();
            CACHE_TIMESTAMP.clear();
            
            // 刷新Redis缓存
            instance.systemDictService.refreshDictCache();
            
            log.info("字典缓存刷新成功");
        } catch (Exception e) {
            log.error("刷新字典缓存失败，错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 清除指定字典的缓存
     * 
     * @param dictCode 字典编码
     */
    public static void clearCache(String dictCode) {
        if (instance == null) {
            log.warn("DictUtils未初始化，无法清除缓存，字典编码: {}", dictCode);
            return;
        }
        
        try {
            // 清除本地缓存
            LOCAL_CACHE.remove(dictCode);
            REVERSE_LOCAL_CACHE.remove(dictCode);
            CACHE_TIMESTAMP.remove(dictCode);
            
            // 清除Redis缓存
            instance.systemDictService.clearDictCache(dictCode);
            
            log.debug("字典缓存清除成功，字典编码: {}", dictCode);
        } catch (Exception e) {
            log.error("清除字典缓存失败，字典编码: {}, 错误: {}", dictCode, e.getMessage(), e);
        }
    }
    
    /**
     * 从本地缓存获取字典标签
     * 
     * @param dictCode 字典编码
     * @param dictValue 字典值
     * @return 字典标签，如果不存在或已过期则返回null
     */
    private static String getFromLocalCache(String dictCode, String dictValue) {
        // 检查缓存是否过期
        if (isCacheExpired(dictCode)) {
            return null;
        }
        
        Map<String, String> mapping = LOCAL_CACHE.get(dictCode);
        if (mapping != null) {
            return mapping.get(dictValue);
        }
        
        return null;
    }
    
    /**
     * 从反向本地缓存获取字典值
     * 
     * @param dictCode 字典编码
     * @param dictLabel 字典标签
     * @return 字典值，如果不存在或已过期则返回null
     */
    private static String getFromReverseLocalCache(String dictCode, String dictLabel) {
        // 检查缓存是否过期
        if (isCacheExpired(dictCode)) {
            return null;
        }
        
        Map<String, String> reverseMapping = REVERSE_LOCAL_CACHE.get(dictCode);
        if (reverseMapping != null) {
            return reverseMapping.get(dictLabel);
        }
        
        return null;
    }
    
    /**
     * 更新本地缓存
     * 
     * @param dictCode 字典编码
     * @param dictValue 字典值
     * @param dictLabel 字典标签
     */
    private static void updateLocalCache(String dictCode, String dictValue, String dictLabel) {
        // 更新正向缓存
        LOCAL_CACHE.computeIfAbsent(dictCode, k -> new ConcurrentHashMap<>())
            .put(dictValue, dictLabel);
        
        // 更新反向缓存
        REVERSE_LOCAL_CACHE.computeIfAbsent(dictCode, k -> new ConcurrentHashMap<>())
            .put(dictLabel, dictValue);
        
        // 更新缓存时间戳
        CACHE_TIMESTAMP.put(dictCode, System.currentTimeMillis());
    }
    
    /**
     * 检查缓存是否过期
     * 
     * @param dictCode 字典编码
     * @return 如果缓存已过期则返回true
     */
    private static boolean isCacheExpired(String dictCode) {
        Long timestamp = CACHE_TIMESTAMP.get(dictCode);
        if (timestamp == null) {
            return true;
        }
        
        return System.currentTimeMillis() - timestamp > CACHE_EXPIRE_TIME;
    }

} 
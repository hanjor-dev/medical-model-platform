/**
 * 权限缓存服务：提供权限数据的Redis缓存功能
 * 
 * 核心功能：
 * 1. 用户权限缓存
 * 2. 角色权限缓存
 * 3. 权限树缓存
 * 4. 缓存刷新机制
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 14:00:00
 */
package com.okbug.platform.common.cache;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okbug.platform.dto.permission.response.PermissionTreeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionCacheService {
    
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    // 缓存键前缀
    private static final String USER_PERMISSIONS_KEY = "user:permissions:";
    private static final String ROLE_PERMISSIONS_KEY = "role:permissions:";
    private static final String PERMISSION_TREE_KEY = "permission:tree";
    private static final String ROLE_PERMISSION_TREE_KEY = "role:permission:tree:";
    
    // 缓存过期时间
    private static final Duration CACHE_EXPIRE_TIME = Duration.ofMinutes(30);
    private static final Duration CACHE_EXPIRE_TIME_EMPTY = Duration.ofMinutes(2);
    
    /**
     * 获取用户权限缓存
     */
    public List<String> getUserPermissions(Long userId) {
        try {
            String key = USER_PERMISSIONS_KEY + userId;
            String value = redisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(value)) {
                log.debug("从缓存获取用户权限，用户ID: {}", userId);
                return objectMapper.readValue(value, new TypeReference<List<String>>() {});
            }
        } catch (Exception e) {
            log.warn("获取用户权限缓存失败，用户ID: {}", userId, e);
        }
        return null;
    }
    
    /**
     * 设置用户权限缓存
     */
    public void setUserPermissions(Long userId, List<String> permissions) {
        try {
            String key = USER_PERMISSIONS_KEY + userId;
            String value = objectMapper.writeValueAsString(permissions);
            // 空列表采用短TTL，避免长时间缓存空结果导致权限回填后仍旧取不到
            Duration ttl = (permissions == null || permissions.isEmpty()) ? CACHE_EXPIRE_TIME_EMPTY : CACHE_EXPIRE_TIME;
            redisTemplate.opsForValue().set(key, value, ttl);
            int size = (permissions == null) ? 0 : permissions.size();
            log.debug("设置用户权限缓存，用户ID: {}, 权限数量: {}", userId, size);
        } catch (Exception e) {
            log.warn("设置用户权限缓存失败，用户ID: {}", userId, e);
        }
    }
    
    /**
     * 获取角色权限缓存
     */
    public List<String> getRolePermissions(String role) {
        try {
            String key = ROLE_PERMISSIONS_KEY + role;
            String value = redisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(value)) {
                log.debug("从缓存获取角色权限，角色: {}", role);
                return objectMapper.readValue(value, new TypeReference<List<String>>() {});
            }
        } catch (Exception e) {
            log.warn("获取角色权限缓存失败，角色: {}", role, e);
        }
        return null;
    }
    
    /**
     * 设置角色权限缓存
     */
    public void setRolePermissions(String role, List<String> permissions) {
        try {
            String key = ROLE_PERMISSIONS_KEY + role;
            String value = objectMapper.writeValueAsString(permissions);
            redisTemplate.opsForValue().set(key, value, CACHE_EXPIRE_TIME);
            log.debug("设置角色权限缓存，角色: {}, 权限数量: {}", role, permissions.size());
        } catch (Exception e) {
            log.warn("设置角色权限缓存失败，角色: {}", role, e);
        }
    }
    
    /**
     * 获取权限树缓存
     */
    public List<PermissionTreeResponse> getPermissionTree() {
        try {
            String value = redisTemplate.opsForValue().get(PERMISSION_TREE_KEY);
            if (StrUtil.isNotBlank(value)) {
                log.debug("从缓存获取权限树");
                return objectMapper.readValue(value, new TypeReference<List<PermissionTreeResponse>>() {});
            }
        } catch (Exception e) {
            log.warn("获取权限树缓存失败", e);
        }
        return null;
    }
    
    /**
     * 设置权限树缓存
     */
    public void setPermissionTree(List<PermissionTreeResponse> permissionTree) {
        try {
            String value = objectMapper.writeValueAsString(permissionTree);
            redisTemplate.opsForValue().set(PERMISSION_TREE_KEY, value, CACHE_EXPIRE_TIME);
            log.debug("设置权限树缓存，节点数量: {}", permissionTree.size());
        } catch (Exception e) {
            log.warn("设置权限树缓存失败", e);
        }
    }
    
    /**
     * 获取角色权限树缓存
     */
    public List<PermissionTreeResponse> getRolePermissionTree(String role) {
        try {
            String key = ROLE_PERMISSION_TREE_KEY + role;
            String value = redisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(value)) {
                log.debug("从缓存获取角色权限树，角色: {}", role);
                return objectMapper.readValue(value, new TypeReference<List<PermissionTreeResponse>>() {});
            }
        } catch (Exception e) {
            log.warn("获取角色权限树缓存失败，角色: {}", role, e);
        }
        return null;
    }
    
    /**
     * 设置角色权限树缓存
     */
    public void setRolePermissionTree(String role, List<PermissionTreeResponse> permissionTree) {
        try {
            String key = ROLE_PERMISSION_TREE_KEY + role;
            String value = objectMapper.writeValueAsString(permissionTree);
            redisTemplate.opsForValue().set(key, value, CACHE_EXPIRE_TIME);
            log.debug("设置角色权限树缓存，角色: {}, 节点数量: {}", role, permissionTree.size());
        } catch (Exception e) {
            log.warn("设置角色权限树缓存失败，角色: {}", role, e);
        }
    }
    
    /**
     * 清除用户权限缓存
     */
    public void clearUserPermissions(Long userId) {
        try {
            String key = USER_PERMISSIONS_KEY + userId;
            redisTemplate.delete(key);
            log.debug("清除用户权限缓存，用户ID: {}", userId);
        } catch (Exception e) {
            log.warn("清除用户权限缓存失败，用户ID: {}", userId, e);
        }
    }
    
    /**
     * 批量清除用户权限缓存
     */
    public void clearUserPermissions(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        
        try {
            List<String> keys = userIds.stream()
                    .map(userId -> USER_PERMISSIONS_KEY + userId)
                    .collect(Collectors.toList());
            redisTemplate.delete(keys);
            log.debug("批量清除用户权限缓存，用户数量: {}", userIds.size());
        } catch (Exception e) {
            log.warn("批量清除用户权限缓存失败，用户数量: {}", userIds.size(), e);
        }
    }
    
    /**
     * 清除角色权限缓存
     */
    public void clearRolePermissions(String role) {
        try {
            String key = ROLE_PERMISSIONS_KEY + role;
            redisTemplate.delete(key);
            log.debug("清除角色权限缓存，角色: {}", role);
        } catch (Exception e) {
            log.warn("清除角色权限缓存失败，角色: {}", role, e);
        }
    }
    
    /**
     * 清除权限树缓存
     */
    public void clearPermissionTree() {
        try {
            redisTemplate.delete(PERMISSION_TREE_KEY);
            log.debug("清除权限树缓存");
        } catch (Exception e) {
            log.warn("清除权限树缓存失败", e);
        }
    }
    
    /**
     * 清除角色权限树缓存
     */
    public void clearRolePermissionTree(String role) {
        try {
            String key = ROLE_PERMISSION_TREE_KEY + role;
            redisTemplate.delete(key);
            log.debug("清除角色权限树缓存，角色: {}", role);
        } catch (Exception e) {
            log.warn("清除角色权限树缓存失败，角色: {}", role, e);
        }
    }
    
    /**
     * 刷新所有权限缓存
     */
    public void refreshAllPermissionCache() {
        try {
            // 清除所有权限相关缓存
            Set<String> userPermissionKeys = redisTemplate.keys(USER_PERMISSIONS_KEY + "*");
            Set<String> rolePermissionKeys = redisTemplate.keys(ROLE_PERMISSIONS_KEY + "*");
            Set<String> rolePermissionTreeKeys = redisTemplate.keys(ROLE_PERMISSION_TREE_KEY + "*");
            
            if (userPermissionKeys != null && !userPermissionKeys.isEmpty()) {
                redisTemplate.delete(userPermissionKeys);
            }
            if (rolePermissionKeys != null && !rolePermissionKeys.isEmpty()) {
                redisTemplate.delete(rolePermissionKeys);
            }
            if (rolePermissionTreeKeys != null && !rolePermissionTreeKeys.isEmpty()) {
                redisTemplate.delete(rolePermissionTreeKeys);
            }
            
            // 清除权限树缓存
            redisTemplate.delete(PERMISSION_TREE_KEY);
            
            log.info("权限缓存刷新完成");
        } catch (Exception e) {
            log.error("权限缓存刷新失败", e);
        }
    }
}
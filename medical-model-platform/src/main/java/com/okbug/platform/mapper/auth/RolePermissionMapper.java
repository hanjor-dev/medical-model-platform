/**
 * 角色权限关联数据访问接口：提供角色权限关联的数据库操作
 * 
 * 功能描述：
 * 1. 继承MyBatis-Plus BaseMapper提供基础CRUD
 * 2. 支持角色权限关联查询
 * 3. 用于权限验证和菜单构建
 * 4. 遵循项目规范，只使用MyBatis-Plus Java API
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 00:35:00
 */
package com.okbug.platform.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.okbug.platform.entity.auth.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * 角色权限关联Mapper接口
 * 
 * 说明：
 * - 继承BaseMapper获得基础的CRUD操作能力
 * - 角色权限查询逻辑通过Service层实现
 * - 不使用XML配置文件和注解SQL
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    
    /**
     * 物理删除指定角色的所有权限关联
     */
    @Delete("DELETE FROM role_permissions WHERE role = #{role}")
    int deleteByRolePhysical(@Param("role") String role);
    
} 
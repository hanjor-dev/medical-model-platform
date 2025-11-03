/**
 * 权限数据访问接口：提供权限相关的数据库操作
 * 
 * 功能描述：
 * 1. 继承MyBatis-Plus BaseMapper提供基础CRUD
 * 2. 支持权限查询和权限树构建
 * 3. 支持角色权限关联查询
 * 4. 遵循项目规范，只使用MyBatis-Plus Java API
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 00:20:00
 */
package com.okbug.platform.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.okbug.platform.entity.auth.Permission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限Mapper接口
 * 
 * 说明：
 * - 继承BaseMapper获得基础的CRUD操作能力
 * - 权限树构建和角色权限查询通过Service层实现
 * - 不使用XML配置文件和注解SQL
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    
    // 注意：
    // 1. 本接口只继承BaseMapper，不添加自定义方法
    // 2. 权限相关的复杂查询逻辑在Service层实现：
    //    - 根据角色查询权限列表：通过role_permissions表关联查询
    //    - 构建权限树结构：通过parent_id字段递归构建
    //    - 菜单权限过滤：通过permission_type字段过滤
    // 3. 遵循项目规范：禁止XML文件和注解SQL
    
} 
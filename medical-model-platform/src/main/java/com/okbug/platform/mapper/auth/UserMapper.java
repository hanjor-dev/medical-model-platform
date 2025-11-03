/**
 * 用户数据访问接口：提供用户相关的数据库操作
 * 
 * 功能描述：
 * 1. 继承MyBatis-Plus BaseMapper提供基础CRUD
 * 2. 提供用户查询的扩展方法
 * 3. 支持用户名、邮箱、推荐码等多种查询方式
 * 4. 遵循项目规范，只使用MyBatis-Plus Java API
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 00:15:00
 */
package com.okbug.platform.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.okbug.platform.entity.auth.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * 
 * 说明：
 * - 继承BaseMapper获得基础的CRUD操作能力
 * - 不使用XML配置文件和注解SQL
 * - 所有复杂查询通过Service层使用QueryWrapper实现
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    // 注意：
    // 1. 本接口只继承BaseMapper，不添加自定义方法
    // 2. 所有复杂查询逻辑在Service层通过QueryWrapper实现
    // 3. 遵循项目规范：禁止XML文件和注解SQL
    // 4. MyBatis-Plus提供的方法已足够满足用户管理的需求
    
    /*
     * BaseMapper提供的方法包括：
     * - insert(entity) : 插入一条记录
     * - deleteById(id) : 根据ID删除
     * - updateById(entity) : 根据ID更新
     * - selectById(id) : 根据ID查询
     * - selectOne(wrapper) : 根据条件查询一条记录
     * - selectList(wrapper) : 根据条件查询列表
     * - selectPage(page, wrapper) : 分页查询
     * - selectCount(wrapper) : 查询总数
     * 等等...
     */
    
} 
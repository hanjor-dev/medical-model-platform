/**
 * MyBatis-Plus配置类：配置分页插件、自动填充等功能
 * 
 * 核心功能：
 * 1. 分页插件配置
 * 2. 审计字段自动填充
 * 3. 逻辑删除配置
 * 4. 乐观锁配置
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-14 22:00:00
 */
package com.okbug.platform.config;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@Slf4j
public class MybatisPlusConfig {
    
    /**
     * MyBatis-Plus插件配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        
        // 乐观锁插件 - 必须在分页插件之后
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        // 防止全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        
        return interceptor;
    }
    
    /**
     * 自动填充配置
     * 自动设置创建时间、更新时间、创建人、更新人
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            
            /**
             * 插入时自动填充
             */
            @Override
            public void insertFill(MetaObject metaObject) {
                // 获取当前用户ID，如果未登录则为null
                Long currentUserId = getCurrentUserId();
                LocalDateTime now = LocalDateTime.now();
                
                // 自动填充创建时间（必须有值）
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
                // 自动填充更新时间（必须有值）
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
                
                // 自动填充创建人和更新人（可以为null）
                if (currentUserId != null) {
                    this.strictInsertFill(metaObject, "createBy", Long.class, currentUserId);
                    this.strictInsertFill(metaObject, "updateBy", Long.class, currentUserId);
                } else {
                    // 如果用户未登录，使用默认值或跳过填充，让数据库字段为null
                    log.debug("用户未登录，跳过创建人和更新人字段填充");
                }
            }
            
            /**
             * 更新时自动填充
             */
            @Override
            public void updateFill(MetaObject metaObject) {
                // 获取当前用户ID，如果未登录则为null
                Long currentUserId = getCurrentUserId();
                LocalDateTime now = LocalDateTime.now();
                
                // 自动填充更新时间
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
                // 自动填充更新人
                this.strictUpdateFill(metaObject, "updateBy", Long.class, currentUserId);
            }
            
            /**
             * 获取当前登录用户ID
             * 如果用户未登录，返回null
             */
            private Long getCurrentUserId() {
                try {
                    if (StpUtil.isLogin()) {
                        return StpUtil.getLoginIdAsLong();
                    }
                } catch (Exception e) {
                    log.debug("获取当前用户ID失败，用户可能未登录: {}", e.getMessage());
                }
                return null;
            }
        };
    }
} 
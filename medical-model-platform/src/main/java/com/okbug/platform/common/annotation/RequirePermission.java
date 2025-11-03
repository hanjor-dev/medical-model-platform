/**
 * 权限验证注解：用于方法级权限控制
 * 
 * 功能描述：
 * 1. 标注方法需要的权限
 * 2. 支持多个权限验证
 * 3. 支持AND/OR逻辑
 * 4. 与Sa-Token集成
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 02:20:00
 */
package com.okbug.platform.common.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    
    /**
     * 需要的权限编码
     */
    String[] value() default {};
    
    /**
     * 权限验证模式
     * AND: 需要拥有所有权限
     * OR: 只需要拥有其中一个权限
     */
    Mode mode() default Mode.AND;
    
    /**
     * 权限验证模式枚举
     */
    enum Mode {
        AND, OR
    }
} 
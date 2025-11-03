/**
 * 数据权限注解：用于数据级权限控制
 * 
 * 功能描述：
 * 1. 标注方法需要数据权限验证
 * 2. 指定用户ID参数名
 * 3. 自动验证数据访问权限
 * 4. 支持灵活的权限控制
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 02:25:00
 */
package com.okbug.platform.common.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {
    
    /**
     * 用户ID参数名，默认为"userId"
     */
    String userIdParam() default "userId";
    
    /**
     * 是否必须验证数据权限，默认为true
     */
    boolean required() default true;
    
    /**
     * 权限描述
     */
    String description() default "";
} 
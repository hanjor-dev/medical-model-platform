/**
 * 操作日志注解
 * 
 * 用于标记需要记录操作日志的方法
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 12:30:00
 */
package com.okbug.platform.common.annotation;

import java.lang.annotation.*;
import com.okbug.platform.common.enums.OperationModule;
import com.okbug.platform.common.enums.OperationType;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    /**
     * 操作模块
     */
    @Deprecated
    String module() default "";
    
    /**
     * 操作模块（推荐使用）
     */
    OperationModule moduleEnum() default OperationModule.NONE;
    
    /**
     * 操作类型
     */
    @Deprecated
    String type() default "";
    
    /**
     * 操作类型（推荐使用）
     */
    OperationType typeEnum() default OperationType.NONE;
    
    /**
     * 操作描述
     */
    String description() default "";
    
    /**
     * 是否记录请求参数
     */
    boolean recordParams() default true;
    
    /**
     * 是否记录响应结果
     */
    boolean recordResult() default true;
    
    /**
     * 是否异步记录（推荐使用，不影响业务性能）
     */
    boolean async() default true;
} 

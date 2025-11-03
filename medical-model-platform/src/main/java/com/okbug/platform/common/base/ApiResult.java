/**
 * 统一API响应结果：封装所有接口的返回结果
 * 
 * 设计原则：
 * 1. 简洁易用 - 只提供必要的静态方法
 * 2. 类型安全 - 充分利用泛型
 * 3. 语义清晰 - 方法名明确表达意图
 * 
 * 使用示例：
 * - ApiResult.success()                    // 成功，无数据
 * - ApiResult.success(data)                // 成功，返回数据
 * - ApiResult.error("操作失败")             // 失败，自定义消息
 * - ApiResult.error(ErrorCode.NOT_FOUND)   // 失败，使用错误码
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-14 22:15:00
 */
package com.okbug.platform.common.base;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {
    
    /**
     * 响应状态码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 请求时间戳
     */
    private Long timestamp;
    
    // ================ 构造方法 ================
    
    private ApiResult() {
        this.timestamp = System.currentTimeMillis();
    }
    
    private ApiResult(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }
    
    private ApiResult(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }
    
    // ================ 成功响应 ================
    
    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResult<T> success() {
        return new ApiResult<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage());
    }
    
    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }
    
    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResult<T> success(String message) {
        return new ApiResult<>(ErrorCode.SUCCESS.getCode(), message);
    }
    
    /**
     * 成功响应（自定义消息和数据）
     */
    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(ErrorCode.SUCCESS.getCode(), message, data);
    }
    
    // ================ 失败响应 ================
    
    /**
     * 失败响应（使用错误码）
     */
    public static <T> ApiResult<T> error(ErrorCode errorCode) {
        return new ApiResult<>(errorCode.getCode(), errorCode.getMessage());
    }
    
    /**
     * 失败响应（使用错误码和自定义消息）
     */
    public static <T> ApiResult<T> error(ErrorCode errorCode, String message) {
        return new ApiResult<>(errorCode.getCode(), message);
    }
    
    /**
     * 失败响应（自定义消息，使用默认错误码）
     */
    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>(ErrorCode.INTERNAL_ERROR.getCode(), message);
    }
    
    /**
     * 失败响应（完全自定义）
     */
    public static <T> ApiResult<T> error(Integer code, String message) {
        return new ApiResult<>(code, message);
    }
    
    // ================ 便利方法 ================
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ErrorCode.SUCCESS.getCode().equals(this.code);
    }
    
    /**
     * 判断是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
    
    // ================ Getter/Setter ================
    
    public Integer getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public T getData() {
        return data;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "ApiResult{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
} 
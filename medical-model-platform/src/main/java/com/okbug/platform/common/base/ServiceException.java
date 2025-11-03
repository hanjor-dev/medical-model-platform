/**
 * 服务异常：专门用于业务服务层异常处理
 * 
 * 设计原则：
 * 1. 职责单一 - 只处理业务服务层异常
 * 2. 使用简单 - 提供便捷的构造方法
 * 3. 信息完整 - 包含错误码、消息和数据
 * 
 * 使用场景：
 * - 业务规则验证失败
 * - 数据状态不符合预期
 * - 业务操作不被允许
 * 
 * 使用示例：
 * - throw new ServiceException("用户不存在")
 * - throw new ServiceException(ErrorCode.DATA_NOT_FOUND)
 * - throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "账户已被锁定")
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-14 22:15:00
 */
package com.okbug.platform.common.base;

public class ServiceException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 错误消息
     */
    private final String message;
    
    /**
     * 附加数据
     */
    private Object data;
    
    // ================ 构造方法 ================
    
    /**
     * 使用错误码构造异常
     */
    public ServiceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
    
    /**
     * 使用错误码和自定义消息构造异常
     */
    public ServiceException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
        this.message = customMessage;
    }
    
    /**
     * 使用自定义消息构造异常（使用默认错误码）
     */
    public ServiceException(String message) {
        super(message);
        this.code = ErrorCode.BUSINESS_RULE_VIOLATION.getCode();
        this.message = message;
    }
    
    /**
     * 使用自定义错误码和消息构造异常
     */
    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    /**
     * 使用错误码、自定义消息和附加数据构造异常
     */
    public ServiceException(ErrorCode errorCode, String customMessage, Object data) {
        this(errorCode, customMessage);
        this.data = data;
    }
    
    /**
     * 使用错误码和根异常构造异常
     */
    public ServiceException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    // ================ Getter方法 ================
    
    public Integer getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "ServiceException{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
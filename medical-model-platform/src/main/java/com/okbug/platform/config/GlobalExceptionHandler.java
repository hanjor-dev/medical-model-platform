/**
 * 全局异常处理器：统一处理系统中的所有异常
 * 
 * 设计原则：
 * 1. 分层处理 - 按异常类型分别处理
 * 2. 统一格式 - 所有异常都返回ApiResult格式
 * 3. 日志记录 - 记录异常信息便于排查
 * 4. 用户友好 - 对外屏蔽技术细节
 * 
 * 异常处理优先级：
 * 1. ServiceException - 服务异常，直接返回
 * 2. 参数验证异常 - 返回参数错误信息
 * 3. 系统异常 - 记录详细日志，返回通用错误
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-14 22:15:00
 */
package com.okbug.platform.config;

import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.common.base.ErrorCode;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    // ================ 业务异常处理 ================
    
    /**
     * 处理服务异常
     * 这是最常见的异常，直接返回异常中的错误信息
     */
    @ExceptionHandler(ServiceException.class)
    public ApiResult<Object> handleServiceException(ServiceException e, HttpServletRequest request) {
        log.warn("服务异常 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ApiResult.error(e.getCode(), e.getMessage());
    }
    
    // ================ 参数验证异常处理 ================
    
    /**
     * 处理@RequestBody参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Object> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("参数验证失败 [{}]: {}", request.getRequestURI(), e.getMessage());
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        return ApiResult.error(ErrorCode.PARAM_INVALID, "参数验证失败: " + errorMsg);
    }
    
    /**
     * 处理@RequestParam和表单参数验证异常
     */
    @ExceptionHandler(BindException.class)
    public ApiResult<Object> handleBindException(BindException e, HttpServletRequest request) {
        log.warn("参数绑定失败 [{}]: {}", request.getRequestURI(), e.getMessage());
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        return ApiResult.error(ErrorCode.PARAM_INVALID, "参数绑定失败: " + errorMsg);
    }
    
    /**
     * 处理@Validated单个参数验证异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResult<Object> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.warn("参数约束验证失败 [{}]: {}", request.getRequestURI(), e.getMessage());
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMsg = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        
        return ApiResult.error(ErrorCode.PARAM_INVALID, "参数验证失败: " + errorMsg);
    }
    
    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResult<Object> handleMissingParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少请求参数 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ApiResult.error(ErrorCode.PARAM_MISSING, "缺少必要参数: " + e.getParameterName());
    }
    
    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResult<Object> handleTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型错误 [{}]: {}", request.getRequestURI(), e.getMessage());
        Class<?> requiredType = e.getRequiredType();
        String expectedType = requiredType != null ? requiredType.getSimpleName() : "未知";
        return ApiResult.error(ErrorCode.PARAM_TYPE_MISMATCH, 
            String.format("参数 '%s' 类型错误，期望类型: %s", e.getName(), expectedType));
    }
    
    /**
     * 处理JSON格式错误异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResult<Object> handleJsonParseException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("JSON解析失败 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ApiResult.error(ErrorCode.PARAM_INVALID, "请求体格式错误，请检查JSON格式");
    }
    
    // ================ HTTP相关异常处理 ================
    
    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResult<Object>> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("接口不存在 [{}]: {}", request.getRequestURI(), e.getMessage());
        ApiResult<Object> result = ApiResult.error(ErrorCode.NOT_FOUND, "接口不存在: " + e.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }
    
    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResult<Object>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持 [{}]: {}", request.getRequestURI(), e.getMessage());
        ApiResult<Object> result = ApiResult.error(ErrorCode.METHOD_NOT_ALLOWED, 
            "请求方法不支持: " + e.getMethod() + "，支持的方法: " + String.join(", ", e.getSupportedMethods()));
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(result);
    }
    
    /**
     * 处理媒体类型不支持异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ApiResult<Object> handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        log.warn("媒体类型不支持 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ApiResult.error(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "不支持的媒体类型: " + e.getContentType());
    }
    
    /**
     * 处理文件大小超出限制异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResult<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.warn("文件大小超出限制 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ApiResult.error(ErrorCode.PAYLOAD_TOO_LARGE, "文件大小超出限制");
    }
    
    // ================ 数据库相关异常处理 ================
    
    /**
     * 处理数据库唯一约束冲突异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResult<Object> handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        log.warn("数据重复 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ApiResult.error(ErrorCode.DATA_ALREADY_EXISTS, "数据已存在，请检查输入");
    }
    
    /**
     * 处理数据库完整性约束异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResult<Object> handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        log.warn("数据完整性约束冲突 [{}]: {}", request.getRequestURI(), e.getMessage());
        return ApiResult.error(ErrorCode.DATA_CONSTRAINT_VIOLATION, "数据约束冲突，请检查数据完整性");
    }
    
    // ================ 系统异常处理 ================
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResult<Object>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常 [{}]: {}", request.getRequestURI(), e.getMessage(), e);
        ApiResult<Object> result = ApiResult.error(ErrorCode.INTERNAL_ERROR, "系统内部错误，请稍后重试");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
    
    // ================ Sa-Token权限异常处理 ================
    
    /**
     * 处理Sa-Token未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ApiResult<Object>> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        log.warn("用户未登录访问受保护接口 [{}]: {}", request.getRequestURI(), e.getMessage());
        ApiResult<Object> result = ApiResult.error(ErrorCode.USER_TOKEN_EXPIRED, "请先登录");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }
    
    /**
     * 处理Sa-Token角色权限不足异常
     */
    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity<ApiResult<Object>> handleNotRoleException(NotRoleException e, HttpServletRequest request) {
        log.warn("用户角色权限不足 [{}]: 需要角色 [{}]", request.getRequestURI(), e.getRole());
        ApiResult<Object> result = ApiResult.error(ErrorCode.FORBIDDEN, "角色权限不足，需要角色：" + e.getRole());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }
    
    /**
     * 处理Sa-Token权限不足异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<ApiResult<Object>> handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        log.warn("用户权限不足 [{}]: 需要权限 [{}]", request.getRequestURI(), e.getPermission());
        ApiResult<Object> result = ApiResult.error(ErrorCode.FORBIDDEN, "权限不足，需要权限：" + e.getPermission());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }
    
    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Object>> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 [{}]: {}", request.getRequestURI(), e.getMessage(), e);
        ApiResult<Object> result = ApiResult.error(ErrorCode.INTERNAL_ERROR, "系统异常，请联系管理员");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
} 
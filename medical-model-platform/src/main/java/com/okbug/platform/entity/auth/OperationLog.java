/**
 * 操作日志实体类：对应数据库operation_logs表
 * 
 * 功能描述：
 * 1. 记录用户的各种操作行为
 * 2. 支持请求参数和响应结果记录
 * 3. 支持操作耗时统计
 * 4. 用于系统审计和问题追溯
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:30:00
 */
package com.okbug.platform.entity.auth;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("operation_logs")
public class OperationLog {
    
    /**
     * 日志ID（主键）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 操作用户ID
     */
    @TableField(insertStrategy = FieldStrategy.IGNORED, updateStrategy = FieldStrategy.IGNORED)
    private Long userId;
    
    /**
     * 操作用户名（冗余字段，便于查询）
     */
    private String username;
    
    /**
     * 操作模块
     */
    private String operationModule;
    
    /**
     * 操作类型
     */
    private String operationType;
    
    /**
     * 操作描述
     */
    private String operationDesc;
    
    /**
     * 请求方法（GET、POST、PUT、DELETE等）
     */
    private String requestMethod;
    
    /**
     * 请求URL
     */
    private String requestUrl;
    
    /**
     * 请求参数
     */
    private String requestParams;
    
    /**
     * 响应结果
     */
    private String responseResult;
    
    /**
     * 操作IP地址
     */
    private String operationIp;
    
    /**
     * 操作地点（根据IP解析）
     */
    private String operationLocation;
    
    /**
     * 操作状态（0:失败 1:成功）
     */
    private Integer operationStatus;
    
    /**
     * 错误消息
     */
    private String errorMessage;
    
    /**
     * 操作时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime operationTime;
    
    /**
     * 操作耗时（毫秒）
     */
    private Long costTime;
    
    // ================ 常量定义 ================
    
    /**
     * 操作状态：失败
     */
    public static final int STATUS_FAILED = 0;
    
    /**
     * 操作状态：成功
     */
    public static final int STATUS_SUCCESS = 1;
    
    // ================ 操作模块常量 ================
    
    /**
     * 用户管理模块
     */
    public static final String MODULE_USER = "USER";
    
    /**
     * 权限管理模块
     */
    public static final String MODULE_PERMISSION = "PERMISSION";
    
    /**
     * 积分系统模块
     */
    public static final String MODULE_CREDIT = "CREDIT";
    
    /**
     * 系统配置模块
     */
    public static final String MODULE_SYSTEM = "SYSTEM";
    
    /**
     * 模型管理模块
     */
    public static final String MODULE_MODEL = "MODEL";
    
    /**
     * AI引擎模块
     */
    public static final String MODULE_AI_ENGINE = "AI_ENGINE";
    
    // ================ 操作类型常量 ================
    
    /**
     * 查询操作
     */
    public static final String TYPE_QUERY = "QUERY";
    
    /**
     * 创建操作
     */
    public static final String TYPE_CREATE = "CREATE";
    
    /**
     * 更新操作
     */
    public static final String TYPE_UPDATE = "UPDATE";
    
    /**
     * 删除操作
     */
    public static final String TYPE_DELETE = "DELETE";
    
    /**
     * 登录操作
     */
    public static final String TYPE_LOGIN = "LOGIN";
    
    /**
     * 登出操作
     */
    public static final String TYPE_LOGOUT = "LOGOUT";
    
    /**
     * 导出操作
     */
    public static final String TYPE_EXPORT = "EXPORT";
    
    /**
     * 导入操作
     */
    public static final String TYPE_IMPORT = "IMPORT";
    
    // ================ 便利方法 ================
    
    /**
     * 判断操作是否成功
     */
    public boolean isSuccess() {
        return STATUS_SUCCESS == this.operationStatus;
    }
    
    /**
     * 判断操作是否失败
     */
    public boolean isFailed() {
        return STATUS_FAILED == this.operationStatus;
    }
    
    /**
     * 创建成功操作日志
     */
    public static OperationLog createSuccessLog(Long userId, String username, String module, String type, 
                                               String description, String method, String url, String params, 
                                               String response, String ip, String location, Long costTime) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperationModule(module);
        log.setOperationType(type);
        log.setOperationDesc(description);
        log.setRequestMethod(method);
        log.setRequestUrl(url);
        log.setRequestParams(params);
        log.setResponseResult(response);
        log.setOperationIp(ip);
        log.setOperationLocation(location);
        log.setOperationStatus(STATUS_SUCCESS);
        log.setOperationTime(LocalDateTime.now());
        log.setCostTime(costTime);
        return log;
    }
    
    /**
     * 创建失败操作日志
     */
    public static OperationLog createFailedLog(Long userId, String username, String module, String type, 
                                              String description, String method, String url, String params, 
                                              String ip, String location, String errorMessage, Long costTime) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperationModule(module);
        log.setOperationType(type);
        log.setOperationDesc(description);
        log.setRequestMethod(method);
        log.setRequestUrl(url);
        log.setRequestParams(params);
        log.setOperationIp(ip);
        log.setOperationLocation(location);
        log.setOperationStatus(STATUS_FAILED);
        log.setErrorMessage(errorMessage);
        log.setOperationTime(LocalDateTime.now());
        log.setCostTime(costTime);
        return log;
    }
} 

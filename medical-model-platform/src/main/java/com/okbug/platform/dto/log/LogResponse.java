/**
 * 日志响应DTO（统一登录日志和操作日志）
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 11:10:00
 */
package com.okbug.platform.dto.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "日志响应")
public class LogResponse {
    
    @Schema(description = "日志ID")
    private Long id;
    
    @Schema(description = "日志类型：LOGIN-登录日志，OPERATION-操作日志")
    private String logType;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "用户昵称")
    private String userNickname;
    
    @Schema(description = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;
    
    @Schema(description = "操作模块")
    private String operationModule;
    
    @Schema(description = "操作类型")
    private String operationType;
    
    @Schema(description = "操作描述")
    private String operationDesc;
    
    @Schema(description = "操作状态：0-失败，1-成功")
    private Integer operationStatus;
    
    @Schema(description = "操作状态文本")
    private String operationStatusText;
    
    @Schema(description = "IP地址")
    private String ipAddress;
    
    @Schema(description = "地理位置")
    private String location;
    
    @Schema(description = "用户代理（登录日志）")
    private String userAgent;
    
    @Schema(description = "请求方法（操作日志）")
    private String requestMethod;
    
    @Schema(description = "请求URL（操作日志）")
    private String requestUrl;
    
    @Schema(description = "操作耗时（毫秒）")
    private Long costTime;
    
    @Schema(description = "错误信息")
    private String errorMessage;
    
    // ================ 详细信息字段 ================
    
    @Schema(description = "请求参数（详情查看时返回）")
    private String requestParams;
    
    @Schema(description = "响应结果（详情查看时返回）")
    private String responseResult;
    
    @Schema(description = "设备信息（详情查看时返回）")
    private String deviceInfo;
    
    /**
     * 获取操作状态文本
     */
    public String getOperationStatusText() {
        if (operationStatus == null) {
            return "未知";
        }
        return operationStatus == 1 ? "成功" : "失败";
    }
    
    /**
     * 获取日志类型文本
     */
    public String getLogTypeText() {
        if ("LOGIN".equals(logType)) {
            return "登录日志";
        } else if ("OPERATION".equals(logType)) {
            return "操作日志";
        }
        return "未知类型";
    }
    
    /**
     * 判断是否为登录日志
     */
    public boolean isLoginLog() {
        return "LOGIN".equals(logType);
    }
    
    /**
     * 判断是否为操作日志
     */
    public boolean isOperationLog() {
        return "OPERATION".equals(logType);
    }
    
    /**
     * 判断操作是否成功
     */
    public boolean isSuccess() {
        return operationStatus != null && operationStatus == 1;
    }
} 

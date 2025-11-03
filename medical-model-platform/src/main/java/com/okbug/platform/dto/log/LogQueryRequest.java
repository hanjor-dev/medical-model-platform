/**
 * 日志查询请求DTO
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 11:00:00
 */
package com.okbug.platform.dto.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

@Data
@Schema(description = "日志查询请求")
public class LogQueryRequest {
    
    // 移除日志类型筛选（已统一到一张表）
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户名（模糊搜索）")
    private String username;
    
    @Schema(description = "操作模块")
    private String operationModule;
    
    @Schema(description = "操作状态：0-失败，1-成功")
    private Integer status;
    
    // 移除IP地址筛选（前端已删除）
    
    @Schema(description = "关键词搜索（在描述和URL中搜索）")
    private String keyword;
    
    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    @Schema(description = "快速时间范围：TODAY-今天，YESTERDAY-昨天，WEEK-近7天，MONTH-近30天")
    private String quickTimeRange;
    
    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页大小", example = "20")
    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 500, message = "每页大小不能超过500")
    private Integer pageSize = 20;
    
    @Schema(description = "排序字段", example = "operation_time")
    private String sortField = "operation_time";
    
    @Schema(description = "排序方向：ASC-升序，DESC-降序", example = "DESC")
    private String sortOrder = "DESC";
    
    // ================ 常量定义（保留快速时间范围） ================
    
    /**
     * 快速时间范围：今天
     */
    public static final String QUICK_TIME_TODAY = "TODAY";
    
    /**
     * 快速时间范围：昨天
     */
    public static final String QUICK_TIME_YESTERDAY = "YESTERDAY";
    
    /**
     * 快速时间范围：近7天
     */
    public static final String QUICK_TIME_WEEK = "WEEK";
    
    /**
     * 快速时间范围：近30天
     */
    public static final String QUICK_TIME_MONTH = "MONTH";
} 

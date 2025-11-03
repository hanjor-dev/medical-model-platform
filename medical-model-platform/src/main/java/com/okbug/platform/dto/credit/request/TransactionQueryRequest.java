package com.okbug.platform.dto.credit.request;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.entity.credit.CreditTransaction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * 积分交易记录查询请求：查询当前会话用户自己的交易记录，管理员可以看自己和子账号的，超级管理员则可以看所有的
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-08-27 15:50:00
 */
@Data
@Schema(name = "积分交易记录查询请求", description = "支持按多种条件筛选积分交易记录")
public class TransactionQueryRequest {

    @Schema(description = "查询的用户ID（管理员和超级管理员可指定，普通用户只能查询自己）", example = "1")
    private Long userId;

    @Schema(description = "积分类型编码", example = "NORMAL")
    private String creditTypeCode;

    @Schema(description = "交易类型", example = "CONSUME", title = "REGISTER:注册奖励 REFERRAL:推荐奖励 TRANSFER:转账 CONSUME:消费 REFUND:退款")
    private String transactionType;

    @Schema(description = "场景编码", example = "AI_COMPUTE")
    private String scenarioCode;

    @Schema(description = "关联订单号", example = "ORDER123456")
    private String relatedOrderId;

    @Schema(description = "关联用户ID（转账、推荐等场景）", example = "2")
    private Long relatedUserId;

    @Schema(description = "开始时间", example = "2025-01-01 00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-12-31 23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "关键词搜索（交易描述）", example = "AI计算")
    private String keyword;

    @Schema(description = "是否包含关联交易（转账等双向交易）", example = "true", title = "true表示同时显示转账的双方记录")
    private Boolean includeRelatedTransactions = true;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小不能小于1")
    private long size = 10L;

    @Schema(description = "当前页码", example = "1")
    @Min(value = 1, message = "当前页码不能小于1")
    private long current = 1L;
    
    /**
     * 转换为Page对象
     */
    public Page<CreditTransaction> toPage() {
        return new Page<>(current, size);
    }
} 

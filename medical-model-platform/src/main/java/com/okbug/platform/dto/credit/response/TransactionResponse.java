package com.okbug.platform.dto.credit.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 积分交易记录查询响应：包含交易记录信息和关联信息
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-08-27 15:50:00
 */
@Data
@Schema(name = "积分交易记录响应", description = "积分交易记录的详细信息")
public class TransactionResponse {

    @Schema(description = "交易ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "积分类型编码", example = "NORMAL")
    private String creditTypeCode;

    @Schema(description = "积分类型名称", example = "普通积分")
    private String creditTypeName;

    @Schema(description = "积分单位", example = "积分")
    private String unitName;

    @Schema(description = "交易类型", example = "CONSUME", title = "REGISTER:注册奖励 REFERRAL:推荐奖励 TRANSFER:转账 CONSUME:消费 REFUND:退款")
    private String transactionType;

    @Schema(description = "交易类型描述", example = "消费")
    private String transactionTypeDesc;

    @Schema(description = "交易金额", example = "10.00", title = "正数为收入，负数为支出")
    private BigDecimal amount;

    @Schema(description = "交易前余额", example = "100.00")
    private BigDecimal balanceBefore;

    @Schema(description = "交易后余额", example = "90.00")
    private BigDecimal balanceAfter;

    @Schema(description = "关联用户ID（转账、推荐等场景）", example = "2")
    private Long relatedUserId;

    @Schema(description = "关联用户名", example = "lisi")
    private String relatedUsername;

    @Schema(description = "关联用户昵称", example = "李四")
    private String relatedNickname;

    @Schema(description = "关联订单号", example = "ORDER123456")
    private String relatedOrderId;

    @Schema(description = "场景编码", example = "AI_COMPUTE")
    private String scenarioCode;

    @Schema(description = "场景名称", example = "AI计算")
    private String scenarioName;

    @Schema(description = "交易描述", example = "AI模型计算消耗积分")
    private String description;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "是否为关联交易记录", example = "false", title = "true表示这是转账等双向交易中的关联记录")
    private Boolean isRelatedTransaction = false;

    @Schema(description = "关联交易ID", example = "2", title = "如果是关联交易，指向主交易记录ID")
    private Long relatedTransactionId;
} 

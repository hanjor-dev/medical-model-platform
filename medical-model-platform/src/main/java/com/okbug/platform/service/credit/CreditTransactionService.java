package com.okbug.platform.service.credit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.dto.credit.request.TransactionQueryRequest;
import com.okbug.platform.dto.credit.response.TransactionResponse;

import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 积分交易记录服务接口：提供交易记录的查询和管理功能
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-08-27 15:50:00
 */
public interface CreditTransactionService {

    /**
     * 分页查询积分交易记录
     * 权限控制：
     * - 普通用户只能查询自己的记录
     * - 管理员可以查询自己和子账号的记录
     * - 超级管理员可以查询所有记录
     * 
     * @param request 查询请求，包含分页和筛选条件
     * @return 分页的交易记录列表
     */
    Page<TransactionResponse> getTransactionPage(TransactionQueryRequest request);

    /**
     * 查询用户的积分交易记录列表（不分页）
     * 
     * @param userId 用户ID
     * @param creditTypeCode 积分类型编码（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 交易记录列表
     */
    List<TransactionResponse> getTransactionsByUser(Long userId,
                                                   String creditTypeCode,
                                                   String transactionType,
                                                   String scenarioCode,
                                                   String keyword,
                                                   LocalDateTime startTime,
                                                   LocalDateTime endTime);

    /**
     * 根据交易ID查询交易记录详情
     * 
     * @param transactionId 交易ID
     * @return 交易记录详情
     */
    TransactionResponse getTransactionById(Long transactionId);

    /**
     * 查询关联交易记录（转账等双向交易）
     * 
     * @param transactionId 主交易ID
     * @return 关联的交易记录列表
     */
    List<TransactionResponse> getRelatedTransactions(Long transactionId);

    /**
     * 获取用户的交易统计信息
     * 
     * @param userId 用户ID
     * @param creditTypeCode 积分类型编码（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 交易统计信息
     */
    TransactionStatistics getTransactionStatistics(Long userId, String creditTypeCode, 
                                                 LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据查询请求计算交易统计信息（与分页查询使用相同筛选条件）
     *
     * @param request 查询请求，包含筛选条件（用户、类型、交易类型、场景、时间范围、关键词等）
     * @return 交易统计信息
     */
    TransactionStatistics getTransactionStatistics(TransactionQueryRequest request);

    /**
     * 交易统计信息
     */
    class TransactionStatistics {
        private Long totalTransactions;
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal netChange;
        private Long incomeCount;
        private Long expenseCount;

        // getters and setters
        public Long getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(Long totalTransactions) { this.totalTransactions = totalTransactions; }
        
        public BigDecimal getTotalIncome() { return totalIncome; }
        public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
        
        public BigDecimal getTotalExpense() { return totalExpense; }
        public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }
        
        public BigDecimal getNetChange() { return netChange; }
        public void setNetChange(BigDecimal netChange) { this.netChange = netChange; }
        
        public Long getIncomeCount() { return incomeCount; }
        public void setIncomeCount(Long incomeCount) { this.incomeCount = incomeCount; }
        
        public Long getExpenseCount() { return expenseCount; }
        public void setExpenseCount(Long expenseCount) { this.expenseCount = expenseCount; }
    }
} 
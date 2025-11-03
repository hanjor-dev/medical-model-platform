/**
 * 积分交易记录Mapper接口：提供积分交易记录的数据访问方法
 * 
 * 功能描述：
 * 1. 积分交易记录的CRUD操作
 * 2. 交易记录查询和统计
 * 3. 支持分页和条件查询
 * 4. 使用MyBatis-Plus Java API
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:35:00
 */
package com.okbug.platform.mapper.credit;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.entity.credit.CreditTransaction;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CreditTransactionMapper extends BaseMapper<CreditTransaction> {
    
    /**
     * 分页查询用户积分交易记录
     * 
     * @param page 分页参数
     * @param userId 用户ID
     * @param creditTypeCode 积分类型编码（可选）
     * @param transactionType 交易类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     */
    default IPage<CreditTransaction> selectUserTransactionPage(Page<CreditTransaction> page, Long userId, 
                                                              String creditTypeCode, String transactionType, 
                                                              LocalDateTime startTime, LocalDateTime endTime) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditTransaction> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditTransaction>()
                        .eq("user_id", userId)
                        .eq(creditTypeCode != null, "credit_type_code", creditTypeCode)
                        .eq(transactionType != null, "transaction_type", transactionType)
                        .ge(startTime != null, "create_time", startTime)
                        .le(endTime != null, "create_time", endTime)
                        .orderByDesc("create_time");
        
        return selectPage(page, queryWrapper);
    }
    
    /**
     * 分页查询所有积分交易记录（管理员使用）
     * 
     * @param page 分页参数
     * @param userId 用户ID（可选）
     * @param creditTypeCode 积分类型编码（可选）
     * @param transactionType 交易类型（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     */
    default IPage<CreditTransaction> selectAllTransactionPage(Page<CreditTransaction> page, Long userId, 
                                                             String creditTypeCode, String transactionType, 
                                                             LocalDateTime startTime, LocalDateTime endTime) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditTransaction> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditTransaction>()
                        .eq(userId != null, "user_id", userId)
                        .eq(creditTypeCode != null, "credit_type_code", creditTypeCode)
                        .eq(transactionType != null, "transaction_type", transactionType)
                        .ge(startTime != null, "create_time", startTime)
                        .le(endTime != null, "create_time", endTime)
                        .orderByDesc("create_time");
        
        return selectPage(page, queryWrapper);
    }
    
    /**
     * 根据用户ID查询交易记录
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 交易记录列表
     */
    default List<CreditTransaction> selectByUserId(Long userId, Integer limit) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditTransaction> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditTransaction>()
                        .eq("user_id", userId)
                        .orderByDesc("create_time");
        
        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }
        
        return selectList(queryWrapper);
    }
    
    /**
     * 根据交易类型查询交易记录
     * 
     * @param transactionType 交易类型
     * @param limit 限制数量
     * @return 交易记录列表
     */
    default List<CreditTransaction> selectByTransactionType(String transactionType, Integer limit) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditTransaction> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditTransaction>()
                        .eq("transaction_type", transactionType)
                        .orderByDesc("create_time");
        
        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }
        
        return selectList(queryWrapper);
    }
    
    /**
     * 根据关联订单号查询交易记录
     * 
     * @param orderId 订单号
     * @return 交易记录列表
     */
    default List<CreditTransaction> selectByOrderId(String orderId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditTransaction>()
                .eq("related_order_id", orderId)
                .orderByDesc("create_time"));
    }
    
    /**
     * 根据关联用户ID查询交易记录
     * 
     * @param relatedUserId 关联用户ID
     * @return 交易记录列表
     */
    default List<CreditTransaction> selectByRelatedUserId(Long relatedUserId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditTransaction>()
                .eq("related_user_id", relatedUserId)
                .orderByDesc("create_time"));
    }
    
    /**
     * 根据使用场景编码查询交易记录
     * 
     * @param scenarioCode 使用场景编码
     * @param limit 限制数量
     * @return 交易记录列表
     */
    default List<CreditTransaction> selectByScenarioCode(String scenarioCode, Integer limit) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditTransaction> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditTransaction>()
                        .eq("scenario_code", scenarioCode)
                        .orderByDesc("create_time");
        
        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }
        
        return selectList(queryWrapper);
    }
    
    /**
     * 统计用户指定时间范围内的交易次数
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 交易次数
     */
    default Long countUserTransactionsInTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditTransaction>()
                .eq("user_id", userId)
                .ge("create_time", startTime)
                .le("create_time", endTime));
    }
    
    /**
     * 统计用户指定时间范围内的积分收入
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 积分收入总额
     */
    default BigDecimal sumUserIncomeInTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<CreditTransaction> transactions = selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditTransaction>()
                .eq("user_id", userId)
                .gt("amount", 0)
                .ge("create_time", startTime)
                .le("create_time", endTime));
        
        BigDecimal totalIncome = BigDecimal.ZERO;
        for (CreditTransaction transaction : transactions) {
            if (transaction.getAmount() != null) {
                totalIncome = totalIncome.add(transaction.getAmount());
            }
        }
        return totalIncome;
    }
    
    /**
     * 统计用户指定时间范围内的积分支出
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 积分支出总额
     */
    default BigDecimal sumUserExpenseInTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<CreditTransaction> transactions = selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditTransaction>()
                .eq("user_id", userId)
                .lt("amount", 0)
                .ge("create_time", startTime)
                .le("create_time", endTime));
        
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (CreditTransaction transaction : transactions) {
            if (transaction.getAmount() != null) {
                totalExpense = totalExpense.add(transaction.getAmount().abs());
            }
        }
        return totalExpense;
    }
} 
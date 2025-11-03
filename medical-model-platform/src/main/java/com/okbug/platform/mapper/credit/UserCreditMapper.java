/**
 * 用户积分账户Mapper接口：提供用户积分账户的数据访问方法
 * 
 * 功能描述：
 * 1. 用户积分账户的CRUD操作
 * 2. 积分账户查询和统计
 * 3. 支持乐观锁和并发控制
 * 4. 使用MyBatis-Plus Java API
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 10:30:00
 */
package com.okbug.platform.mapper.credit;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.okbug.platform.entity.credit.UserCredit;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserCreditMapper extends BaseMapper<UserCredit> {
    
    /**
     * 根据用户ID和积分类型编码查询积分账户
     * 
     * @param userId 用户ID
     * @param creditTypeCode 积分类型编码
     * @return 积分账户
     */
    default UserCredit selectByUserIdAndCreditType(Long userId, String creditTypeCode) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserCredit>()
                .eq("user_id", userId)
                .eq("credit_type_code", creditTypeCode));
    }
    
    /**
     * 根据用户ID查询所有积分账户
     * 
     * @param userId 用户ID
     * @return 积分账户列表
     */
    default List<UserCredit> selectByUserId(Long userId) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserCredit>()
                .eq("user_id", userId)
                .orderByAsc("credit_type_code"));
    }
    
    /**
     * 根据积分类型编码查询所有用户的积分账户
     * 
     * @param creditTypeCode 积分类型编码
     * @return 积分账户列表
     */
    default List<UserCredit> selectByCreditTypeCode(String creditTypeCode) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserCredit>()
                .eq("credit_type_code", creditTypeCode)
                .orderByDesc("balance"));
    }
    
    /**
     * 查询有余额的积分账户
     * 
     * @param creditTypeCode 积分类型编码（可选）
     * @return 有余额的积分账户列表
     */
    default List<UserCredit> selectAccountsWithBalance(String creditTypeCode) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserCredit> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserCredit>()
                        .gt("balance", 0);
        
        if (creditTypeCode != null) {
            queryWrapper.eq("credit_type_code", creditTypeCode);
        }
        
        return selectList(queryWrapper);
    }
    
    /**
     * 检查用户是否已有指定类型的积分账户
     * 
     * @param userId 用户ID
     * @param creditTypeCode 积分类型编码
     * @return 是否存在
     */
    default boolean existsByUserIdAndCreditType(Long userId, String creditTypeCode) {
        return selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserCredit>()
                .eq("user_id", userId)
                .eq("credit_type_code", creditTypeCode)) > 0;
    }
    
    /**
     * 根据用户ID统计积分账户数量
     * 
     * @param userId 用户ID
     * @return 积分账户数量
     */
    default Long countByUserId(Long userId) {
        return selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserCredit>()
                .eq("user_id", userId));
    }
    
    /**
     * 根据积分类型编码统计用户数量
     * 
     * @param creditTypeCode 积分类型编码
     * @return 用户数量
     */
    default Long countByCreditTypeCode(String creditTypeCode) {
        return selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserCredit>()
                .eq("credit_type_code", creditTypeCode));
    }
    
    /**
     * 查询指定积分类型下的总积分余额
     * 
     * @param creditTypeCode 积分类型编码
     * @return 总积分余额
     */
    default BigDecimal selectTotalBalanceByCreditType(String creditTypeCode) {
        List<UserCredit> accounts = selectByCreditTypeCode(creditTypeCode);
        BigDecimal totalBalance = BigDecimal.ZERO;
        for (UserCredit account : accounts) {
            if (account.getBalance() != null) {
                totalBalance = totalBalance.add(account.getBalance());
            }
        }
        return totalBalance;
    }
    
    /**
     * 查询指定积分类型下的总获得积分
     * 
     * @param creditTypeCode 积分类型编码
     * @return 总获得积分
     */
    default BigDecimal selectTotalEarnedByCreditType(String creditTypeCode) {
        List<UserCredit> accounts = selectByCreditTypeCode(creditTypeCode);
        BigDecimal totalEarned = BigDecimal.ZERO;
        for (UserCredit account : accounts) {
            if (account.getTotalEarned() != null) {
                totalEarned = totalEarned.add(account.getTotalEarned());
            }
        }
        return totalEarned;
    }
    
    /**
     * 查询指定积分类型下的总消费积分
     * 
     * @param creditTypeCode 积分类型编码
     * @return 总消费积分
     */
    default BigDecimal selectTotalConsumedByCreditType(String creditTypeCode) {
        List<UserCredit> accounts = selectByCreditTypeCode(creditTypeCode);
        BigDecimal totalConsumed = BigDecimal.ZERO;
        for (UserCredit account : accounts) {
            if (account.getTotalConsumed() != null) {
                totalConsumed = totalConsumed.add(account.getTotalConsumed());
            }
        }
        return totalConsumed;
    }
} 
/**
 * 引荐服务实现类：实现引荐码相关的业务逻辑
 * 
 * 核心功能：
 * 1. 实现用户引荐码信息查询
 * 2. 实现引荐码有效性验证
 * 3. 实现引荐链接生成
 * 4. 集成系统配置和用户管理服务
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 15:40:00
 */
package com.okbug.platform.service.referral.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.common.constants.SystemConfigKeys;

import com.okbug.platform.dto.referral.ReferralCodeResponse;

import com.okbug.platform.dto.referral.ReferralUserResponse;
import com.okbug.platform.entity.auth.User;
import com.okbug.platform.entity.credit.CreditTransaction;
import com.okbug.platform.common.enums.credit.CreditScenarioCode;

import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.mapper.credit.CreditTransactionMapper;
import com.okbug.platform.service.referral.ReferralService;
import com.okbug.platform.service.system.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 引荐服务实现类
 * 实现引荐码管理的所有业务逻辑
 * 包括引荐码信息查询、验证、链接生成等核心功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralServiceImpl implements ReferralService {
    
    private final UserMapper userMapper;
    private final SystemConfigService systemConfigService;
    private final CreditTransactionMapper creditTransactionMapper;
    

    
    /**
     * 获取当前登录用户的引荐码信息
     * 
     * 功能描述：
     * 1. 获取当前登录用户ID
     * 2. 查询用户引荐码信息
     * 3. 处理可能的异常并返回结果
     * 
     * @return 引荐码信息响应对象
     * @throws ServiceException 当获取失败时抛出
     */
    @Override
    /**
     * 获取当前登录用户的引荐码信息。
     *
     * @return 引荐码响应
     * @throws ServiceException 当用户不存在、被禁用或未配置引荐码时抛出
     */
    public ReferralCodeResponse getCurrentUserReferralCode() {
        // 1. 获取当前登录用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        log.info("获取当前用户引荐码信息，用户ID: {}", currentUserId);
        
        // 2. 参数验证
        
        try {
            // 3. 查询用户信息
            User user = userMapper.selectById(currentUserId);
            if (user == null) {
                log.error("用户不存在，用户ID: {}", currentUserId);
                throw new ServiceException(ErrorCode.USER_NOT_FOUND, "用户不存在");
            }
            
            // 4. 检查用户状态
            if (user.isDisabled()) {
                log.error("用户账户已禁用，用户ID: {}", currentUserId);
                throw new ServiceException(ErrorCode.USER_ACCOUNT_DISABLED, "用户账户已禁用");
            }
            
            // 5. 检查引荐码是否存在
            if (!StringUtils.hasText(user.getReferralCode())) {
                log.error("用户引荐码不存在，用户ID: {}", currentUserId);
                throw new ServiceException(ErrorCode.REFERRAL_CODE_NOT_FOUND, "用户引荐码不存在");
            }
            
            // 6. 获取引荐码基础链接配置
            String baseUrl = getReferralBaseUrl();
            
            // 7. 生成完整引荐链接
            String referralLink = baseUrl + user.getReferralCode();
            
            // 8. 构建响应对象
            ReferralCodeResponse response = ReferralCodeResponse.builder()
                    .userId(user.getId())
                    .referralCode(user.getReferralCode())
                    .referralLink(referralLink)
                    .shareDescription("分享给朋友，获得积分奖励")
                    .createTime(user.getCreateTime())
                    .build();
            
            log.info("成功获取当前用户引荐码信息，用户ID: {}, 引荐码: {}", currentUserId, response.getReferralCode());
            return response;
            
        } catch (ServiceException e) {
            // 重新抛出ServiceException
            throw e;
        } catch (Exception e) {
            log.error("查询当前用户引荐码信息时发生异常，用户ID: {}", currentUserId, e);
            throw new ServiceException(ErrorCode.REFERRAL_QUERY_FAILED, "查询引荐码信息失败: " + e.getMessage());
        }
    }
    

    
    @Override
    /**
     * 获取引荐码注册基础链接。
     *
     * @return 基础链接，以 '?' 或 '=' 结尾便于拼接引荐码
     * @throws ServiceException 当配置项缺失或异常时抛出
     */
    public String getReferralBaseUrl() {
        try {
            // 从系统配置获取引荐码基础链接，如果不存在则使用默认值
            String baseUrl = systemConfigService.getConfigValue(
                SystemConfigKeys.REFERRAL_BASE_URL,
                "http://localhost:8081/register?refCode="
            );
            
            if (!StringUtils.hasText(baseUrl)) {
                log.error("引荐码基础链接配置为空");
                throw new ServiceException(ErrorCode.REFERRAL_BASE_URL_NOT_CONFIGURED, "引荐码基础链接配置为空");
            }
            
            log.debug("获取引荐码基础链接配置: {}", baseUrl);
            return baseUrl;
            
        } catch (ServiceException e) {
            // 重新抛出ServiceException
            throw e;
        } catch (Exception e) {
            log.error("获取引荐码基础链接配置时发生异常", e);
            throw new ServiceException(ErrorCode.REFERRAL_BASE_URL_NOT_CONFIGURED, "获取引荐码基础链接配置失败: " + e.getMessage());
        }
    }
    
    @Override
    /**
     * 分页查询当前用户的引荐用户列表。
     *
     * @param page 页码，默认1，最小1
     * @param size 页大小，默认10，最大100
     * @return 引荐用户列表（当前页）
     */
    public List<ReferralUserResponse> getCurrentUserReferralUsers(Integer page, Integer size) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        log.info("获取当前用户引荐用户列表，用户ID: {}, 页码: {}, 大小: {}", currentUserId, page, size);

        // 1. 分页参数验证
        if (page == null || page < 1) {
            page = 1;
        }
        
        if (size == null || size < 1) {
            size = 10;
        }
        
        // 设置分页大小上限，防止性能问题
        if (size > 100) {
            size = 100;
        }
        
        try {
            // 2. 查询引荐用户列表
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getReferrerUserId, currentUserId); // 查询引荐人为当前用户的用户
            queryWrapper.orderByDesc(User::getCreateTime); // 按注册时间倒序
            
            // 3. 分页查询
            Page<User> pageParam = new Page<>(page, size);
            Page<User> result = userMapper.selectPage(pageParam, queryWrapper);
            
            // 4. 转换为响应对象
            List<ReferralUserResponse> referralUsers = result.getRecords().stream()
                    .map(this::convertToReferralUserResponse)
                    .collect(Collectors.toList());
            
            log.info("成功获取引荐用户列表，用户ID: {}, 总数: {}, 当前页: {}", currentUserId, result.getTotal(), page);
            return referralUsers;
            
        } catch (Exception e) {
            log.error("查询引荐用户列表时发生异常，用户ID: {}", currentUserId, e);
            throw new ServiceException(ErrorCode.REFERRAL_QUERY_FAILED, "查询引荐用户列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 将 User 实体转换为 ReferralUserResponse。
     */
    private ReferralUserResponse convertToReferralUserResponse(User user) {
        // 计算贡献积分（这里需要根据实际业务逻辑计算）
        Integer contributionCredits = calculateContributionCredits(user);
        
        // 确定引荐来源
        String source = determineReferralSource(user);
        
        return ReferralUserResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .registerTime(user.getCreateTime())
                .contributionCredits(contributionCredits)
                .source(source)
                .lastLoginTime(user.getLastLoginTime()) // 可能为空，表示从未登录
                .referralTime(user.getCreateTime()) // 注册时间即为引荐时间
                .build();
    }
    
    /**
     * 计算用户贡献的积分：从积分交易表中查询推荐奖励金额。
     */
    private Integer calculateContributionCredits(User user) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        
        try {
            // 查询当前用户因推荐该用户而获得的积分奖励
            LambdaQueryWrapper<CreditTransaction> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CreditTransaction::getUserId, currentUserId)  // 奖励给当前用户
                       .eq(CreditTransaction::getScenarioCode, CreditScenarioCode.USER_REFERRAL.getCode())  // 推荐奖励场景
                       .eq(CreditTransaction::getRelatedUserId, user.getId());  // 关联用户为被推荐用户
            
            CreditTransaction rewardTransaction = creditTransactionMapper.selectOne(queryWrapper);
            
            if (rewardTransaction != null) {
                // 返回实际的奖励金额
                return rewardTransaction.getAmount().intValue();
            } else {
                // 如果没有找到推荐奖励记录，可能是历史数据或其他情况
                log.warn("未找到推荐奖励记录，推荐人ID: {}, 被推荐用户ID: {}", currentUserId, user.getId());
                return 0;
            }
            
        } catch (Exception e) {
            log.error("查询推荐奖励积分失败，推荐人ID: {}, 被推荐用户ID: {}", currentUserId, user.getId(), e);
            return 0;  // 出错时返回0，避免影响整体查询
        }
    }
    
    /**
     * 确定引荐来源。
     */
    private String determineReferralSource(User user) {
        // 这里可以根据实际业务逻辑来确定来源
        // 比如通过用户注册时的设备信息、渠道信息等
        return "直接引荐";
    }
    

} 
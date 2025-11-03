package com.okbug.platform.service.credit.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.dto.credit.request.TransactionQueryRequest;
import com.okbug.platform.dto.credit.response.TransactionResponse;
import com.okbug.platform.entity.credit.CreditTransaction;
import com.okbug.platform.entity.credit.CreditType;
import com.okbug.platform.entity.credit.CreditUsageScenario;
import com.okbug.platform.entity.auth.User;
import com.okbug.platform.common.enums.credit.CreditScenarioCode;
import com.okbug.platform.common.enums.credit.CreditTransactionType;
import com.okbug.platform.mapper.credit.CreditTransactionMapper;
import com.okbug.platform.mapper.credit.CreditTypeMapper;
import com.okbug.platform.mapper.credit.CreditUsageScenarioMapper;
import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.service.credit.CreditTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 积分交易记录服务实现类：提供交易记录的查询和管理功能
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-08-27 15:50:00
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditTransactionServiceImpl implements CreditTransactionService {

    private final CreditTransactionMapper creditTransactionMapper;
    private final CreditTypeMapper creditTypeMapper;
    private final CreditUsageScenarioMapper creditScenarioMapper;
    private final UserMapper userMapper;

    @Override
    /**
     * 分页查询积分交易记录。
     *
     * - 根据请求参数与权限自动解析查询用户ID
     * - 支持类型/场景/关键词/时间范围等过滤
     *
     * @param request 查询请求，不能为空
     * @return 交易记录分页结果
     * @throws ServiceException 当权限不足或参数非法时抛出
     */
    public Page<TransactionResponse> getTransactionPage(TransactionQueryRequest request) {
        log.info("查询积分交易记录，请求参数：{}", request);
        if (request == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "查询请求不能为空");
        }
        
        // 权限验证和用户ID处理
        Long queryUserId = validateAndGetQueryUserId(request.getUserId());
        
        // 构建查询条件
        LambdaQueryWrapper<CreditTransaction> queryWrapper = buildQueryWrapper(request, queryUserId);
        
        // 执行分页查询
        Page<CreditTransaction> transactionPage = creditTransactionMapper.selectPage(
            request.toPage(), 
            queryWrapper
        );
        
        // 转换为响应DTO
        List<TransactionResponse> responses = convertToTransactionResponses(transactionPage.getRecords());
        
        // 处理关联交易记录
        if (Boolean.TRUE.equals(request.getIncludeRelatedTransactions())) {
            responses = addRelatedTransactions(responses);
        }
        
        // 构建响应分页对象
        Page<TransactionResponse> responsePage = new Page<>(request.getCurrent(), request.getSize());
        responsePage.setTotal(transactionPage.getTotal());
        responsePage.setRecords(responses);
        
        log.info("查询积分交易记录完成，总记录数：{}", responsePage.getTotal());
        return responsePage;
    }

    @Override
    /**
     * 按用户查询积分交易记录列表（不分页）。
     *
     * - 会依据当前登录用户权限校验是否可查看目标用户
     * - 支持多维过滤与时间范围筛选
     */
    public List<TransactionResponse> getTransactionsByUser(Long userId,
                                                          String creditTypeCode,
                                                          String transactionType,
                                                          String scenarioCode,
                                                          String keyword,
                                                          LocalDateTime startTime,
                                                          LocalDateTime endTime) {
        log.info("查询用户积分交易记录，用户ID：{}，积分类型：{}，交易类型：{}，场景：{}，关键词：{}，时间范围：{} - {}",
                userId, creditTypeCode, transactionType, scenarioCode, keyword, startTime, endTime);

        // 权限验证并获取实际查询用户ID
        Long queryUserId = validateAndGetQueryUserId(userId);

        // 构建查询条件
        LambdaQueryWrapper<CreditTransaction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditTransaction::getUserId, queryUserId);
        queryWrapper.eq(CreditTransaction::getIsDeleted, 0);

        if (StringUtils.hasText(creditTypeCode)) {
            queryWrapper.eq(CreditTransaction::getCreditTypeCode, creditTypeCode);
        }

        if (StringUtils.hasText(transactionType)) {
            queryWrapper.eq(CreditTransaction::getTransactionType, transactionType);
        }

        if (StringUtils.hasText(scenarioCode)) {
            queryWrapper.eq(CreditTransaction::getScenarioCode, scenarioCode);
        }

        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(CreditTransaction::getDescription, keyword);
        }

        if (startTime != null) {
            queryWrapper.ge(CreditTransaction::getCreateTime, startTime);
        }

        if (endTime != null) {
            queryWrapper.le(CreditTransaction::getCreateTime, endTime);
        }

        queryWrapper.orderByDesc(CreditTransaction::getCreateTime);

        List<CreditTransaction> transactions = creditTransactionMapper.selectList(queryWrapper);
        return convertToTransactionResponses(transactions);
    }

    @Override
    /**
     * 获取单条交易详情（含权限校验）。
     *
     * @param transactionId 交易ID
     * @return 交易响应
     * @throws ServiceException 当记录不存在或无权查看时抛出
     */
    public TransactionResponse getTransactionById(Long transactionId) {
        log.info("查询交易记录详情，交易ID：{}", transactionId);
        
        CreditTransaction transaction = creditTransactionMapper.selectById(transactionId);
        if (transaction == null || transaction.getIsDeleted() == 1) {
            throw new ServiceException(ErrorCode.CREDIT_TRANSACTION_NOT_FOUND, "交易记录不存在");
        }
        
        // 权限验证
        validateAndGetQueryUserId(transaction.getUserId());
        
        List<TransactionResponse> responses = convertToTransactionResponses(java.util.Arrays.asList(transaction));
        return responses.isEmpty() ? null : responses.get(0);
    }

    @Override
    /**
     * 查询指定交易的关联交易列表。
     *
     * - 转账：关联对端用户的相应记录
     * - 推荐奖励：关联推荐人与被推荐人的对应记录
     */
    public List<TransactionResponse> getRelatedTransactions(Long transactionId) {
        log.info("查询关联交易记录，交易ID：{}", transactionId);
        
        // 先查询主交易记录
        CreditTransaction mainTransaction = creditTransactionMapper.selectById(transactionId);
        if (mainTransaction == null || mainTransaction.getIsDeleted() == 1) {
            throw new ServiceException(ErrorCode.CREDIT_TRANSACTION_NOT_FOUND, "交易记录不存在");
        }
        
        // 权限验证
        validateAndGetQueryUserId(mainTransaction.getUserId());
        
        List<TransactionResponse> relatedTransactions = new ArrayList<>();
        
        // 根据交易类型查找关联记录
        CreditTransactionType transactionType = CreditTransactionType.fromCode(mainTransaction.getTransactionType());
        if (transactionType == CreditTransactionType.TRANSFER) {
            // 转账交易：查找关联用户ID相同的记录
            if (mainTransaction.getRelatedUserId() != null) {
                LambdaQueryWrapper<CreditTransaction> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(CreditTransaction::getRelatedUserId, mainTransaction.getUserId());
                queryWrapper.eq(CreditTransaction::getTransactionType, CreditTransactionType.TRANSFER.getCode());
                queryWrapper.eq(CreditTransaction::getIsDeleted, 0);
                queryWrapper.orderByDesc(CreditTransaction::getCreateTime);
                
                List<CreditTransaction> related = creditTransactionMapper.selectList(queryWrapper);
                relatedTransactions.addAll(convertToTransactionResponses(related));
            }
        } else if (CreditScenarioCode.USER_REFERRAL.getCode().equals(mainTransaction.getScenarioCode())) {
            // 推荐奖励：查找推荐人的记录
            if (mainTransaction.getRelatedUserId() != null) {
                LambdaQueryWrapper<CreditTransaction> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(CreditTransaction::getUserId, mainTransaction.getRelatedUserId());
                queryWrapper.eq(CreditTransaction::getScenarioCode, CreditScenarioCode.USER_REFERRAL.getCode());
                queryWrapper.eq(CreditTransaction::getRelatedUserId, mainTransaction.getUserId());
                queryWrapper.eq(CreditTransaction::getIsDeleted, 0);
                
                List<CreditTransaction> related = creditTransactionMapper.selectList(queryWrapper);
                relatedTransactions.addAll(convertToTransactionResponses(related));
            }
        }
        
        return relatedTransactions;
    }

    @Override
    /**
     * 统计用户在时间范围内的交易聚合数据（收入/支出/净变动等）。
     */
    public TransactionStatistics getTransactionStatistics(Long userId, String creditTypeCode, 
                                                        LocalDateTime startTime, LocalDateTime endTime) {
        log.info("查询用户交易统计，用户ID：{}，积分类型：{}，时间范围：{} - {}", 
                userId, creditTypeCode, startTime, endTime);
        
        // 权限验证
        validateAndGetQueryUserId(userId);
        
        // 构建查询条件
        LambdaQueryWrapper<CreditTransaction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditTransaction::getUserId, userId);
        queryWrapper.eq(CreditTransaction::getIsDeleted, 0);
        
        if (StringUtils.hasText(creditTypeCode)) {
            queryWrapper.eq(CreditTransaction::getCreditTypeCode, creditTypeCode);
        }
        
        if (startTime != null) {
            queryWrapper.ge(CreditTransaction::getCreateTime, startTime);
        }
        
        if (endTime != null) {
            queryWrapper.le(CreditTransaction::getCreateTime, endTime);
        }
        
        List<CreditTransaction> transactions = creditTransactionMapper.selectList(queryWrapper);
        
        // 计算统计信息
        TransactionStatistics statistics = new TransactionStatistics();
        statistics.setTotalTransactions((long) transactions.size());
        
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        long incomeCount = 0;
        long expenseCount = 0;
        
        for (CreditTransaction transaction : transactions) {
            if (transaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalIncome = totalIncome.add(transaction.getAmount());
                incomeCount++;
            } else {
                totalExpense = totalExpense.add(transaction.getAmount().abs());
                expenseCount++;
            }
        }
        
        statistics.setTotalIncome(totalIncome);
        statistics.setTotalExpense(totalExpense);
        statistics.setNetChange(totalIncome.subtract(totalExpense));
        statistics.setIncomeCount(incomeCount);
        statistics.setExpenseCount(expenseCount);
        
        return statistics;
    }

    @Override
    /**
     * 基于查询请求统计交易聚合数据。
     *
     * @param request 查询请求，不能为空
     * @return 交易统计
     * @throws ServiceException 当请求为空或权限不足时抛出
     */
    public TransactionStatistics getTransactionStatistics(TransactionQueryRequest request) {
        log.info("根据查询请求统计交易信息，请求参数：{}", request);
        if (request == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "查询请求不能为空");
        }

        // 权限验证和用户ID处理
        Long queryUserId = validateAndGetQueryUserId(request.getUserId());

        // 构建与分页相同的查询条件
        LambdaQueryWrapper<CreditTransaction> queryWrapper = buildQueryWrapper(request, queryUserId);

        List<CreditTransaction> transactions = creditTransactionMapper.selectList(queryWrapper);

        TransactionStatistics statistics = new TransactionStatistics();
        statistics.setTotalTransactions((long) transactions.size());

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        long incomeCount = 0;
        long expenseCount = 0;

        for (CreditTransaction transaction : transactions) {
            if (transaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalIncome = totalIncome.add(transaction.getAmount());
                incomeCount++;
            } else {
                totalExpense = totalExpense.add(transaction.getAmount().abs());
                expenseCount++;
            }
        }

        statistics.setTotalIncome(totalIncome);
        statistics.setTotalExpense(totalExpense);
        statistics.setNetChange(totalIncome.subtract(totalExpense));
        statistics.setIncomeCount(incomeCount);
        statistics.setExpenseCount(expenseCount);

        return statistics;
    }

    /**
     * 权限验证和用户ID处理
     * - 普通用户只能查询自己的记录
     * - 管理员可以查询自己和子账号的记录
     * - 超级管理员可以查询所有记录
     */
    private Long validateAndGetQueryUserId(Long requestUserId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        String currentUserRole = getCurrentUserRole();

        if (requestUserId == null) {
            return currentUserId;
        }

        if (User.ROLE_SUPER_ADMIN.equals(currentUserRole)) {
            return requestUserId;
        }

        if (User.ROLE_ADMIN.equals(currentUserRole)) {
            if (!requestUserId.equals(currentUserId) && !isSubUser(currentUserId, requestUserId)) {
                throw new ServiceException(ErrorCode.PERMISSION_DENIED, "权限不足：仅可查询自己或子账号的交易记录");
            }
            return requestUserId;
        }

        if (!requestUserId.equals(currentUserId)) {
            throw new ServiceException(ErrorCode.PERMISSION_DENIED, "权限不足：仅可查询自己的交易记录");
        }

        return requestUserId;
    }

    /**
     * 获取当前用户角色
     */
    private String getCurrentUserRole() {
        try {
            java.util.List<String> roles = StpUtil.getRoleList();
            if (roles == null || roles.isEmpty()) {
                return User.ROLE_USER;
            }
            String role = roles.get(0);
            if (User.ROLE_SUPER_ADMIN.equalsIgnoreCase(role)) {
                return User.ROLE_SUPER_ADMIN;
            }
            if (User.ROLE_ADMIN.equalsIgnoreCase(role)) {
                return User.ROLE_ADMIN;
            }
            return User.ROLE_USER;
        } catch (Exception e) {
            log.warn("获取用户角色失败，使用默认角色 USER", e);
            return User.ROLE_USER;
        }
    }

    /**
     * 判断是否为子账号
     */
    private boolean isSubUser(Long adminUserId, Long targetUserId) {
        if (adminUserId == null || targetUserId == null) {
            return false;
        }
        if (adminUserId.equals(targetUserId)) {
            return true;
        }
        try {
            int safety = 0;
            Long currentId = targetUserId;
            while (currentId != null && safety < 1000) {
                User user = userMapper.selectById(currentId);
                if (user == null) {
                    return false;
                }
                Long parentId = user.getParentUserId();
                if (parentId == null) {
                    return false;
                }
                if (adminUserId.equals(parentId)) {
                    return true;
                }
                currentId = parentId;
                safety++;
            }
            return false;
        } catch (Exception e) {
            log.warn("判断子账号失败 adminUserId={}, targetUserId={}", adminUserId, targetUserId, e);
            return false;
        }
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<CreditTransaction> buildQueryWrapper(TransactionQueryRequest request, Long queryUserId) {
        LambdaQueryWrapper<CreditTransaction> queryWrapper = new LambdaQueryWrapper<>();
        
        // 基础条件
        queryWrapper.eq(CreditTransaction::getIsDeleted, 0);
        
        // 用户ID条件
        if (queryUserId != null) {
            queryWrapper.eq(CreditTransaction::getUserId, queryUserId);
        }
        
        // 积分类型条件
        if (StringUtils.hasText(request.getCreditTypeCode())) {
            queryWrapper.eq(CreditTransaction::getCreditTypeCode, request.getCreditTypeCode());
        }
        
        // 交易类型条件
        if (StringUtils.hasText(request.getTransactionType())) {
            queryWrapper.eq(CreditTransaction::getTransactionType, request.getTransactionType());
        }
        
        // 场景编码条件
        if (StringUtils.hasText(request.getScenarioCode())) {
            queryWrapper.eq(CreditTransaction::getScenarioCode, request.getScenarioCode());
        }
        
        // 关联订单号条件
        if (StringUtils.hasText(request.getRelatedOrderId())) {
            queryWrapper.eq(CreditTransaction::getRelatedOrderId, request.getRelatedOrderId());
        }
        
        // 关联用户ID条件
        if (request.getRelatedUserId() != null) {
            queryWrapper.eq(CreditTransaction::getRelatedUserId, request.getRelatedUserId());
        }
        
        // 时间范围条件
        if (request.getStartTime() != null) {
            queryWrapper.ge(CreditTransaction::getCreateTime, request.getStartTime());
        }
        
        if (request.getEndTime() != null) {
            queryWrapper.le(CreditTransaction::getCreateTime, request.getEndTime());
        }
        
        // 关键词搜索
        if (StringUtils.hasText(request.getKeyword())) {
            queryWrapper.like(CreditTransaction::getDescription, request.getKeyword());
        }
        
        // 排序
        queryWrapper.orderByDesc(CreditTransaction::getCreateTime);
        
        return queryWrapper;
    }

    /**
     * 转换为交易记录响应DTO
     */
    private List<TransactionResponse> convertToTransactionResponses(List<CreditTransaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 收集所有需要的ID
        List<Long> userIds = new ArrayList<>();
        List<Long> relatedUserIds = new ArrayList<>();
        List<String> creditTypeCodes = new ArrayList<>();
        List<String> scenarioCodes = new ArrayList<>();
        
        for (CreditTransaction transaction : transactions) {
            userIds.add(transaction.getUserId());
            if (transaction.getRelatedUserId() != null) {
                relatedUserIds.add(transaction.getRelatedUserId());
            }
            creditTypeCodes.add(transaction.getCreditTypeCode());
            if (StringUtils.hasText(transaction.getScenarioCode())) {
                scenarioCodes.add(transaction.getScenarioCode());
            }
        }
        
        // 批量查询关联数据
        Map<Long, User> userMap = batchQueryUsers(userIds);
        Map<Long, User> relatedUserMap = batchQueryUsers(relatedUserIds);
        Map<String, CreditType> creditTypeMap = batchQueryCreditTypes(creditTypeCodes);
        Map<String, CreditUsageScenario> scenarioMap = batchQueryScenarios(scenarioCodes);
        
        // 转换DTO
        List<TransactionResponse> responses = new ArrayList<>();
        for (CreditTransaction transaction : transactions) {
            TransactionResponse response = new TransactionResponse();
            
            // 基本信息
            response.setId(transaction.getId());
            response.setUserId(transaction.getUserId());
            response.setCreditTypeCode(transaction.getCreditTypeCode());
            response.setTransactionType(transaction.getTransactionType());
            response.setTransactionTypeDesc(getTransactionTypeDesc(transaction.getTransactionType()));
            response.setAmount(transaction.getAmount());
            response.setBalanceBefore(transaction.getBalanceBefore());
            response.setBalanceAfter(transaction.getBalanceAfter());
            response.setRelatedOrderId(transaction.getRelatedOrderId());
            response.setScenarioCode(transaction.getScenarioCode());
            response.setDescription(transaction.getDescription());
            response.setCreateTime(transaction.getCreateTime());
            response.setUpdateTime(transaction.getUpdateTime());
            
            // 用户信息
            User user = userMap.get(transaction.getUserId());
            if (user != null) {
                response.setUsername(user.getUsername());
                response.setNickname(user.getNickname());
            }
            
            // 关联用户信息
            if (transaction.getRelatedUserId() != null) {
                response.setRelatedUserId(transaction.getRelatedUserId());
                User relatedUser = relatedUserMap.get(transaction.getRelatedUserId());
                if (relatedUser != null) {
                    response.setRelatedUsername(relatedUser.getUsername());
                    response.setRelatedNickname(relatedUser.getNickname());
                }
            }
            
            // 积分类型信息
            CreditType creditType = creditTypeMap.get(transaction.getCreditTypeCode());
            if (creditType != null) {
                response.setCreditTypeName(creditType.getTypeName());
                response.setUnitName(creditType.getUnitName());
            }
            
            // 场景信息
            if (StringUtils.hasText(transaction.getScenarioCode())) {
                CreditUsageScenario scenario = scenarioMap.get(transaction.getScenarioCode());
                if (scenario != null) {
                    response.setScenarioName(scenario.getScenarioName());
                }
            }
            
            responses.add(response);
        }
        
        return responses;
    }

    /**
     * 添加关联交易记录
     */
    private List<TransactionResponse> addRelatedTransactions(List<TransactionResponse> responses) {
        List<TransactionResponse> allResponses = new ArrayList<>(responses);
        
        for (TransactionResponse response : responses) {
            CreditTransactionType transactionType = CreditTransactionType.fromCode(response.getTransactionType());
            if (transactionType == CreditTransactionType.TRANSFER || CreditScenarioCode.USER_REFERRAL.getCode().equals(response.getScenarioCode())) {
                List<TransactionResponse> related = getRelatedTransactions(response.getId());
                for (TransactionResponse relatedResponse : related) {
                    relatedResponse.setIsRelatedTransaction(true);
                    relatedResponse.setRelatedTransactionId(response.getId());
                    allResponses.add(relatedResponse);
                }
            }
        }
        
        // 按时间排序
        allResponses.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        
        return allResponses;
    }

    /**
     * 批量查询用户信息
     */
    private Map<Long, User> batchQueryUsers(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return new HashMap<>();
        }
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, userIds);
        queryWrapper.eq(User::getIsDeleted, 0);
        
        List<User> users = userMapper.selectList(queryWrapper);
        return users.stream().collect(Collectors.toMap(User::getId, user -> user));
    }

    /**
     * 批量查询积分类型信息
     */
    private Map<String, CreditType> batchQueryCreditTypes(List<String> creditTypeCodes) {
        if (creditTypeCodes.isEmpty()) {
            return new HashMap<>();
        }
        
        LambdaQueryWrapper<CreditType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CreditType::getTypeCode, creditTypeCodes);
        queryWrapper.eq(CreditType::getStatus, CreditType.STATUS_ENABLED);
        queryWrapper.eq(CreditType::getIsDeleted, 0);
        
        List<CreditType> creditTypes = creditTypeMapper.selectList(queryWrapper);
        return creditTypes.stream().collect(Collectors.toMap(CreditType::getTypeCode, type -> type));
    }

    /**
     * 批量查询场景信息
     */
    private Map<String, CreditUsageScenario> batchQueryScenarios(List<String> scenarioCodes) {
        if (scenarioCodes.isEmpty()) {
            return new HashMap<>();
        }
        
        LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CreditUsageScenario::getScenarioCode, scenarioCodes);
        queryWrapper.eq(CreditUsageScenario::getStatus, CreditUsageScenario.STATUS_ENABLED);
        queryWrapper.eq(CreditUsageScenario::getIsDeleted, 0);
        
        List<CreditUsageScenario> scenarios = creditScenarioMapper.selectList(queryWrapper);
        return scenarios.stream().collect(Collectors.toMap(CreditUsageScenario::getScenarioCode, scenario -> scenario));
    }

    /**
     * 获取交易类型描述
     */
    private String getTransactionTypeDesc(String transactionTypeCode) {
        CreditTransactionType transactionType = CreditTransactionType.fromCode(transactionTypeCode);
        if (transactionType != null) {
            return transactionType.getDescription();
        }
        return transactionTypeCode;
    }
} 
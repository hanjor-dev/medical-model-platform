/**
 * 积分使用场景Service实现类：实现积分使用场景的管理操作
 * 
 * 功能描述：
 * 1. 积分使用场景的CRUD操作实现
 * 2. 使用场景状态管理
 * 3. 使用场景规则配置
 * 4. 使用场景查询和筛选
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 12:40:00
 */
package com.okbug.platform.service.credit.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.dto.credit.request.CreditScenarioCreateRequest;
import com.okbug.platform.dto.credit.request.CreditScenarioUpdateRequest;
import com.okbug.platform.entity.credit.CreditUsageScenario;
import com.okbug.platform.entity.credit.CreditType;
import com.okbug.platform.mapper.credit.CreditUsageScenarioMapper;
import com.okbug.platform.mapper.credit.CreditTypeMapper;
import com.okbug.platform.service.credit.CreditScenarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import com.okbug.platform.service.permission.PermissionService;
import com.okbug.platform.mapper.credit.CreditTransactionMapper;
import java.time.LocalDateTime;

import com.okbug.platform.entity.credit.CreditTransaction;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditScenarioServiceImpl implements CreditScenarioService {
    
    private final CreditUsageScenarioMapper creditScenarioMapper;
    private final CreditTypeMapper creditTypeMapper;
    private final PermissionService permissionService;
    private final CreditTransactionMapper creditTransactionMapper;
    private final com.okbug.platform.manager.credit.CreditScenarioManager creditScenarioManager;
    
    @Override
    public IPage<CreditUsageScenario> getScenarioPage(Page<CreditUsageScenario> page, String keyword, String creditTypeCode, Integer status) {
        log.debug("分页查询使用场景，页码: {}, 每页大小: {}, 关键词: {}, 积分类型: {}, 状态: {}", 
                page.getCurrent(), page.getSize(), keyword, creditTypeCode, status);
        
        LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                .like(CreditUsageScenario::getScenarioCode, keyword)
                .or()
                .like(CreditUsageScenario::getScenarioName, keyword)
            );
        }
        
        if (StringUtils.hasText(creditTypeCode)) {
            queryWrapper.eq(CreditUsageScenario::getCreditTypeCode, creditTypeCode);
        }
        
        if (status != null) {
            queryWrapper.eq(CreditUsageScenario::getStatus, status);
        }
        
        queryWrapper.orderByAsc(CreditUsageScenario::getId);
        
        return creditScenarioMapper.selectPage(page, queryWrapper);
    }
    
    @Override
    public List<CreditUsageScenario> getEnabledScenarios() {
        log.debug("查询启用的使用场景列表");
        
        LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditUsageScenario::getStatus, CreditUsageScenario.STATUS_ENABLED);
        queryWrapper.orderByAsc(CreditUsageScenario::getId);
        
        return creditScenarioMapper.selectList(queryWrapper);
    }
    
    @Override
    public CreditUsageScenario getScenarioById(Long id) {
        if (id == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "使用场景ID不能为空");
        }
        
        log.debug("根据ID查询使用场景，ID: {}", id);
        
        CreditUsageScenario scenario = creditScenarioMapper.selectById(id);
        if (scenario == null) {
            throw new ServiceException(ErrorCode.CREDIT_SCENARIO_NOT_FOUND, "使用场景不存在");
        }
        
        return scenario;
    }
    
    @Override
    public CreditUsageScenario getScenarioByCode(String scenarioCode) {
        if (!StringUtils.hasText(scenarioCode)) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "使用场景编码不能为空");
        }
        
        log.debug("根据编码查询使用场景，编码: {}", scenarioCode);
        
        LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditUsageScenario::getScenarioCode, scenarioCode);
        CreditUsageScenario scenario = creditScenarioMapper.selectOne(queryWrapper);
        if (scenario == null) {
            throw new ServiceException(ErrorCode.CREDIT_SCENARIO_NOT_FOUND, "使用场景不存在");
        }
        
        return scenario;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreditUsageScenario createScenario(CreditScenarioCreateRequest request) {
        log.info("创建使用场景，请求参数: {}", request);
        
        // 验证请求参数
        validateCreateRequest(request);
        
        // 校验积分类型存在且启用
        validateCreditTypeEnabled(request.getCreditTypeCode());
        
        // 检查场景编码是否已存在
        LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditUsageScenario::getScenarioCode, request.getScenarioCode());
        if (creditScenarioMapper.selectCount(queryWrapper) > 0) {
            throw new ServiceException(ErrorCode.CREDIT_SCENARIO_ALREADY_EXISTS, "使用场景编码已存在");
        }
        
        // 创建使用场景实体
        CreditUsageScenario scenario = new CreditUsageScenario();
        scenario.setScenarioCode(request.getScenarioCode());
        scenario.setScenarioName(request.getScenarioName());
        scenario.setCreditTypeCode(request.getCreditTypeCode());
        scenario.setCostPerUse(request.getCostPerUse());
        scenario.setDescription(request.getDescription());
        scenario.setDailyLimit(request.getDailyLimit());
        scenario.setUserRoles(request.getUserRoles());
        scenario.setStatus(CreditUsageScenario.STATUS_ENABLED);
        
        // 保存到数据库
        int result = creditScenarioMapper.insert(scenario);
        if (result <= 0) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "使用场景创建失败");
        }
        
        log.info("使用场景创建成功，ID: {}, 编码: {}", scenario.getId(), scenario.getScenarioCode());
        refreshScenarioCacheAfterCommit();
        return scenario;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreditUsageScenario updateScenario(Long id, CreditScenarioUpdateRequest request) {
        log.info("更新使用场景，ID: {}, 请求参数: {}", id, request);
        
        // 验证请求参数
        if (id == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "使用场景ID不能为空");
        }
        
        if (request == null || !request.hasUpdateFields()) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "更新请求不能为空且必须包含更新字段");
        }
        
        // 检查使用场景是否存在
        CreditUsageScenario existingScenario = getScenarioById(id);
        
        // 使用场景编码不允许修改，所以不需要检查编码重复
        
        // 更新字段
        boolean hasUpdates = false;
        
        if (StringUtils.hasText(request.getScenarioName())) {
            existingScenario.setScenarioName(request.getScenarioName());
            hasUpdates = true;
        }
        
        if (StringUtils.hasText(request.getCreditTypeCode())) {
            // 校验积分类型存在且启用
            validateCreditTypeEnabled(request.getCreditTypeCode());
            existingScenario.setCreditTypeCode(request.getCreditTypeCode());
            hasUpdates = true;
        }
        
        if (request.getCostPerUse() != null) {
            existingScenario.setCostPerUse(request.getCostPerUse());
            hasUpdates = true;
        }
        
        if (request.getDescription() != null) {
            existingScenario.setDescription(request.getDescription());
            hasUpdates = true;
        }
        
        if (request.getDailyLimit() != null) {
            existingScenario.setDailyLimit(request.getDailyLimit());
            hasUpdates = true;
        }
        
        if (request.getUserRoles() != null) {
            existingScenario.setUserRoles(request.getUserRoles());
            hasUpdates = true;
        }
        
        if (request.getStatus() != null) {
            existingScenario.setStatus(request.getStatus());
            // 若将场景置为启用，需校验其绑定的积分类型也为启用
            if (existingScenario.isEnabled()) {
                validateCreditTypeEnabled(existingScenario.getCreditTypeCode());
            }
            hasUpdates = true;
        }
        
        if (!hasUpdates) {
            log.warn("使用场景更新请求不包含任何有效字段，ID: {}", id);
            return existingScenario;
        }
        
        // 保存更新
        int result = creditScenarioMapper.updateById(existingScenario);
        if (result <= 0) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "使用场景更新失败");
        }
        
        log.info("使用场景更新成功，ID: {}, 编码: {}", existingScenario.getId(), existingScenario.getScenarioCode());
        refreshScenarioCacheAfterCommit();
        return existingScenario;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteScenario(Long id) {
        log.info("删除使用场景，ID: {}", id);
        
        if (id == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "使用场景ID不能为空");
        }
        
        // 检查使用场景是否存在
        CreditUsageScenario scenario = getScenarioById(id);
        
        // 为避免唯一索引 (scenario_code, is_deleted) 冲突：先临时修改唯一字段再执行逻辑删除
        String originalCode = scenario.getScenarioCode();
        String originalName = scenario.getScenarioName();
        String deletedSuffix = buildDeletedSuffix(scenario.getId());
        CreditUsageScenario renameBeforeDelete = new CreditUsageScenario();
        renameBeforeDelete.setId(scenario.getId());
        renameBeforeDelete.setScenarioCode(originalCode + deletedSuffix);
        renameBeforeDelete.setScenarioName((originalName != null ? originalName : originalCode) + deletedSuffix);
        creditScenarioMapper.updateById(renameBeforeDelete);
        
        // 执行软删除
        int result = creditScenarioMapper.deleteById(id);
        if (result <= 0) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "使用场景删除失败");
        }
        
        log.info("使用场景删除成功，ID: {}, 编码: {}", scenario.getId(), scenario.getScenarioCode());
        refreshScenarioCacheAfterCommit();
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableScenario(Long id) {
        log.info("启用使用场景，ID: {}", id);
        
        return updateScenarioStatus(id, true);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableScenario(Long id) {
        log.info("禁用使用场景，ID: {}", id);
        
        return updateScenarioStatus(id, false);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateScenarioStatus(Long id, boolean enabled) {
        if (id == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "使用场景ID不能为空");
        }
        
        // 检查使用场景是否存在
        CreditUsageScenario scenario = getScenarioById(id);
        
        // 启用前校验积分类型启用状态
        if (enabled) {
            validateCreditTypeEnabled(scenario.getCreditTypeCode());
        }
        
        // 更新状态
        scenario.setStatus(enabled ? CreditUsageScenario.STATUS_ENABLED : CreditUsageScenario.STATUS_DISABLED);
        
        int result = creditScenarioMapper.updateById(scenario);
        if (result <= 0) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "使用场景状态更新失败");
        }
        
        log.info("使用场景状态更新成功，ID: {}, 编码: {}, 状态: {}", 
                scenario.getId(), scenario.getScenarioCode(), enabled ? "启用" : "禁用");
        refreshScenarioCacheAfterCommit();
        return true;
    }
    
    @Override
    public List<CreditUsageScenario> getScenariosByCreditType(String creditTypeCode) {
        if (!StringUtils.hasText(creditTypeCode)) {
            return java.util.Collections.emptyList();
        }
        
        log.debug("根据积分类型查询使用场景，积分类型: {}", creditTypeCode);
        
        return creditScenarioMapper.selectByCreditTypeCode(creditTypeCode);
    }
    
    @Override
    public List<CreditUsageScenario> getConsumptionScenarios() {
        log.debug("查询消费场景列表");
        
        return creditScenarioMapper.selectConsumptionScenarios();
    }
    
    @Override
    public List<CreditUsageScenario> getRewardScenarios() {
        log.debug("查询奖励场景列表");
        
        return creditScenarioMapper.selectRewardScenarios();
    }
    
    @Override
    public List<CreditUsageScenario> getScenariosByUserRole(String userRole) {
        if (!StringUtils.hasText(userRole)) {
            return java.util.Collections.emptyList();
        }
        
        log.debug("根据用户角色查询使用场景，用户角色: {}", userRole);
        
        LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(CreditUsageScenario::getUserRoles, userRole);
        queryWrapper.eq(CreditUsageScenario::getStatus, CreditUsageScenario.STATUS_ENABLED);
        queryWrapper.orderByAsc(CreditUsageScenario::getId);
        
        return creditScenarioMapper.selectList(queryWrapper);
    }
    
    @Override
    public boolean existsByScenarioCode(String scenarioCode, Long excludeId) {
        if (!StringUtils.hasText(scenarioCode)) {
            return false;
        }
        
        LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditUsageScenario::getScenarioCode, scenarioCode);
        if (excludeId != null) {
            queryWrapper.ne(CreditUsageScenario::getId, excludeId);
        }
        
        return creditScenarioMapper.selectCount(queryWrapper) > 0;
    }
    
    @Override
    public boolean isValidScenario(Long id) {
        if (id == null) {
            return false;
        }
        
        try {
            CreditUsageScenario scenario = creditScenarioMapper.selectById(id);
            return scenario != null && scenario.isEnabled();
        } catch (Exception e) {
            log.warn("验证使用场景有效性时发生异常，ID: {}, 错误: {}", id, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean isValidScenarioCode(String scenarioCode) {
        if (!StringUtils.hasText(scenarioCode)) {
            return false;
        }
        
        try {
            LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CreditUsageScenario::getScenarioCode, scenarioCode);
            CreditUsageScenario scenario = creditScenarioMapper.selectOne(queryWrapper);
            return scenario != null && scenario.isEnabled();
        } catch (Exception e) {
            log.warn("验证使用场景编码有效性时发生异常，编码: {}, 错误: {}", scenarioCode, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean canUserUseScenario(Long userId, String scenarioCode) {
        log.debug("检查用户是否可以使用指定场景，用户ID: {}, 场景编码: {}", userId, scenarioCode);
        
        if (userId == null || !StringUtils.hasText(scenarioCode)) {
            return false;
        }
        
        // 获取使用场景信息
        LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditUsageScenario::getScenarioCode, scenarioCode);
        CreditUsageScenario scenario = creditScenarioMapper.selectOne(queryWrapper);
        if (scenario == null || !scenario.isEnabled()) {
            return false;
        }
        
        // 获取用户角色并验证权限
        String[] allowedRoles = scenario.getUserRoles().split(",");
        for (String role : allowedRoles) {
            if (permissionService.hasRole(role.trim())) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean isDailyLimitExceeded(Long userId, String scenarioCode) {
        log.debug("检查用户是否超过每日使用限制，用户ID: {}, 场景编码: {}", userId, scenarioCode);
        
        if (userId == null || !StringUtils.hasText(scenarioCode)) {
            return false;
        }
        
        // 获取使用场景信息
        LambdaQueryWrapper<CreditUsageScenario> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditUsageScenario::getScenarioCode, scenarioCode);
        CreditUsageScenario scenario = creditScenarioMapper.selectOne(queryWrapper);
        if (scenario == null || !scenario.isEnabled() || !scenario.hasDailyLimit()) {
            return false;
        }
        
        // 查询用户今日使用该场景的次数
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime tomorrow = today.plusDays(1);
        
        LambdaQueryWrapper<CreditTransaction> transactionQueryWrapper = new LambdaQueryWrapper<>();
        transactionQueryWrapper.eq(CreditTransaction::getUserId, userId);
        transactionQueryWrapper.eq(CreditTransaction::getScenarioCode, scenarioCode);
        transactionQueryWrapper.between(CreditTransaction::getCreateTime, today, tomorrow);
        
        Long todayUsageCount = creditTransactionMapper.selectCount(transactionQueryWrapper);
        
        return todayUsageCount >= scenario.getDailyLimit();
    }
    
    // ================ 私有方法 ================
    
    /**
     * 验证创建请求参数
     */
    private void validateCreateRequest(CreditScenarioCreateRequest request) {
        if (request == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "创建请求不能为空");
        }
        
        if (!StringUtils.hasText(request.getScenarioCode())) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "使用场景编码不能为空");
        }
        
        if (!StringUtils.hasText(request.getScenarioName())) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "使用场景名称不能为空");
        }
        
        if (!StringUtils.hasText(request.getCreditTypeCode())) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "积分类型编码不能为空");
        }
        
        if (request.getCostPerUse() == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "每次使用消耗积分数不能为空");
        }
        
        if (!StringUtils.hasText(request.getUserRoles())) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "允许的用户角色不能为空");
        }
        
        // 验证编码格式
        if (!request.getScenarioCode().matches("^[A-Z_]+$")) {
            throw new ServiceException(ErrorCode.CREDIT_SCENARIO_CODE_INVALID, "使用场景编码只能包含大写字母和下划线");
        }
        
        // 验证每日使用限制
        if (!request.isValidDailyLimit()) {
            throw new ServiceException(ErrorCode.PARAM_OUT_OF_RANGE, "每日使用次数限制必须大于0");
        }
        
        // 验证用户角色格式
        if (!request.isValidUserRoles()) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "用户角色格式不正确");
        }
    }

    /**
     * 校验积分类型存在且处于启用状态
     */
    private void validateCreditTypeEnabled(String creditTypeCode) {
        if (!StringUtils.hasText(creditTypeCode)) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "积分类型编码不能为空");
        }
        LambdaQueryWrapper<CreditType> typeQuery = new LambdaQueryWrapper<>();
        typeQuery.eq(CreditType::getTypeCode, creditTypeCode);
        CreditType creditType = creditTypeMapper.selectOne(typeQuery);
        if (creditType == null) {
            throw new ServiceException(ErrorCode.CREDIT_TYPE_NOT_FOUND, "积分类型不存在");
        }
        if (!creditType.isEnabled()) {
            throw new ServiceException(ErrorCode.CREDIT_TYPE_DISABLED, "积分类型已禁用");
        }
    }

    /**
     * 生成删除后缀，确保同一唯一键在软删除后也不会冲突。
     */
    private String buildDeletedSuffix(Long id) {
        String unique = String.valueOf(id != null ? id : System.currentTimeMillis());
        return "__deleted__" + unique;
    }

    /**
     * 在事务提交后刷新场景缓存；若无事务则立即刷新。
     */
    private void refreshScenarioCacheAfterCommit() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        creditScenarioManager.refreshCache();
                    } catch (Exception e) {
                        log.error("事务提交后刷新积分场景缓存失败", e);
                    }
                }
            });
        } else {
            creditScenarioManager.refreshCache();
        }
    }
} 
/**
 * 积分类型管理Service实现类：实现积分类型的管理操作
 * 
 * 功能描述：
 * 1. 积分类型的CRUD操作实现
 * 2. 积分类型状态管理
 * 3. 积分类型排序管理
 * 4. 积分类型查询和筛选
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 12:00:00
 */
package com.okbug.platform.service.credit.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.dto.credit.request.CreditTypeCreateRequest;
import com.okbug.platform.dto.credit.request.CreditTypeUpdateRequest;
import com.okbug.platform.entity.credit.CreditType;
import com.okbug.platform.entity.credit.UserCredit;
import com.okbug.platform.entity.credit.CreditUsageScenario;
import com.okbug.platform.entity.auth.User;
import com.okbug.platform.mapper.credit.CreditTypeMapper;
import com.okbug.platform.mapper.credit.UserCreditMapper;
import com.okbug.platform.mapper.credit.CreditUsageScenarioMapper;
import com.okbug.platform.manager.credit.CreditScenarioManager;
import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.service.credit.CreditTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditTypeServiceImpl implements CreditTypeService {
    
    private final CreditTypeMapper creditTypeMapper;
    private final UserMapper userMapper;
    private final UserCreditMapper userCreditMapper;
    private final CreditUsageScenarioMapper creditUsageScenarioMapper;
    private final CreditScenarioManager creditScenarioManager;
    
    @Override
    public IPage<CreditType> getCreditTypePage(Page<CreditType> page, String keyword, Integer status) {
        log.debug("分页查询积分类型，页码: {}, 每页大小: {}, 关键词: {}, 状态: {}", 
                page.getCurrent(), page.getSize(), keyword, status);
        
        LambdaQueryWrapper<CreditType> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                .like(CreditType::getTypeCode, keyword)
                .or()
                .like(CreditType::getTypeName, keyword)
            );
        }
        
        if (status != null) {
            queryWrapper.eq(CreditType::getStatus, status);
        }
        
        queryWrapper.orderByAsc(CreditType::getSortOrder);
        
        return creditTypeMapper.selectPage(page, queryWrapper);
    }
    
    @Override
    public List<CreditType> getEnabledCreditTypes() {
        log.debug("查询启用的积分类型列表");
        
        LambdaQueryWrapper<CreditType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditType::getStatus, CreditType.STATUS_ENABLED);
        queryWrapper.orderByAsc(CreditType::getSortOrder);
        
        return creditTypeMapper.selectList(queryWrapper);
    }
    
    @Override
    public CreditType getCreditTypeById(Long id) {
        if (id == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "积分类型ID不能为空");
        }
        
        log.debug("根据ID查询积分类型，ID: {}", id);
        
        CreditType creditType = creditTypeMapper.selectById(id);
        if (creditType == null) {
            throw new ServiceException(ErrorCode.CREDIT_TYPE_NOT_FOUND, "积分类型不存在");
        }
        
        return creditType;
    }
    
    @Override
    public CreditType getCreditTypeByCode(String typeCode) {
        if (!StringUtils.hasText(typeCode)) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "积分类型编码不能为空");
        }
        
        log.debug("根据编码查询积分类型，编码: {}", typeCode);
        
        LambdaQueryWrapper<CreditType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditType::getTypeCode, typeCode);
        CreditType creditType = creditTypeMapper.selectOne(queryWrapper);
        if (creditType == null) {
            throw new ServiceException(ErrorCode.CREDIT_TYPE_NOT_FOUND, "积分类型不存在");
        }
        
        return creditType;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreditType createCreditType(CreditTypeCreateRequest request) {
        log.info("创建积分类型，请求参数: {}", request);
        
        // 验证请求参数
        validateCreateRequest(request);
        
        // 检查类型编码是否已存在
        LambdaQueryWrapper<CreditType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditType::getTypeCode, request.getTypeCode());
        if (creditTypeMapper.selectCount(queryWrapper) > 0) {
            throw new ServiceException(ErrorCode.CREDIT_TYPE_ALREADY_EXISTS, "积分类型编码已存在");
        }
        
        // 检查类型名称是否已存在
        LambdaQueryWrapper<CreditType> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(CreditType::getTypeName, request.getTypeName());
        if (creditTypeMapper.selectCount(nameWrapper) > 0) {
            throw new ServiceException(ErrorCode.CREDIT_TYPE_NAME_ALREADY_EXISTS, "积分类型名称已存在");
        }
        
        // 创建积分类型实体
        CreditType creditType = new CreditType();
        creditType.setTypeCode(request.getTypeCode());
        creditType.setTypeName(request.getTypeName());
        creditType.setDescription(request.getDescription());
        creditType.setUnitName(request.getUnitName());
        creditType.setIconUrl(request.getIconUrl());
        creditType.setColorCode(request.getColorCode());
        creditType.setDecimalPlaces(request.getDecimalPlaces());
        creditType.setIsTransferable(request.getIsTransferable() ? CreditType.TRANSFER_ENABLED : CreditType.TRANSFER_DISABLED);
        creditType.setStatus(CreditType.STATUS_ENABLED);
        creditType.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        
        // 保存到数据库
        int result = creditTypeMapper.insert(creditType);
        if (result <= 0) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "积分类型创建失败");
        }
        
        log.info("积分类型创建成功，ID: {}, 编码: {}", creditType.getId(), creditType.getTypeCode());
        
        // 为所有启用用户初始化该积分类型账户
        initializeUserCreditsForType(creditType.getTypeCode());
        
        return creditType;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreditType updateCreditType(Long id, CreditTypeUpdateRequest request) {
        log.info("更新积分类型，ID: {}, 请求参数: {}", id, request);
        
        // 验证请求参数
        if (id == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "积分类型ID不能为空");
        }
        
        if (request == null || !request.hasUpdateFields()) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "更新请求不能为空且必须包含更新字段");
        }
        
        // 检查积分类型是否存在
        CreditType existingCreditType = getCreditTypeById(id);
        
        // 积分类型编码不允许修改，所以不需要检查编码重复
        
        // 更新字段
        boolean hasUpdates = false;
        
        if (StringUtils.hasText(request.getTypeName())) {
            // 名称唯一性校验（排除自己）
            LambdaQueryWrapper<CreditType> checkNameWrapper = new LambdaQueryWrapper<>();
            checkNameWrapper.eq(CreditType::getTypeName, request.getTypeName());
            checkNameWrapper.ne(CreditType::getId, id);
            if (creditTypeMapper.selectCount(checkNameWrapper) > 0) {
                throw new ServiceException(ErrorCode.CREDIT_TYPE_NAME_ALREADY_EXISTS, "积分类型名称已存在");
            }
            existingCreditType.setTypeName(request.getTypeName());
            hasUpdates = true;
        }
        
        if (request.getDescription() != null) {
            existingCreditType.setDescription(request.getDescription());
            hasUpdates = true;
        }
        
        if (StringUtils.hasText(request.getUnitName())) {
            existingCreditType.setUnitName(request.getUnitName());
            hasUpdates = true;
        }
        
        if (request.getIconUrl() != null) {
            existingCreditType.setIconUrl(request.getIconUrl());
            hasUpdates = true;
        }
        
        if (request.getColorCode() != null) {
            existingCreditType.setColorCode(StringUtils.hasText(request.getColorCode()) ? request.getColorCode() : null);
            hasUpdates = true;
        }
        
        if (request.getDecimalPlaces() != null) {
            existingCreditType.setDecimalPlaces(request.getDecimalPlaces());
            hasUpdates = true;
        }
        
        if (request.getIsTransferable() != null) {
            existingCreditType.setIsTransferable(request.getIsTransferable() ? 
                    CreditType.TRANSFER_ENABLED : CreditType.TRANSFER_DISABLED);
            hasUpdates = true;
        }
        
        if (request.getSortOrder() != null) {
            existingCreditType.setSortOrder(request.getSortOrder());
            hasUpdates = true;
        }
        
        if (request.getStatus() != null) {
            existingCreditType.setStatus(request.getStatus());
            hasUpdates = true;
        }
        
        if (!hasUpdates) {
            log.warn("积分类型更新请求不包含任何有效字段，ID: {}", id);
            return existingCreditType;
        }
        
        // 保存更新
        int result = creditTypeMapper.updateById(existingCreditType);
        if (result <= 0) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "积分类型更新失败");
        }
        
        log.info("积分类型更新成功，ID: {}, 编码: {}", existingCreditType.getId(), existingCreditType.getTypeCode());
        
        return existingCreditType;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCreditType(Long id) {
        log.info("删除积分类型，ID: {}", id);
        
        if (id == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "积分类型ID不能为空");
        }
        
        // 检查积分类型是否存在
        CreditType creditType = getCreditTypeById(id);
        
        // 删除前校验：若存在非零余额账户，则禁止删除
        Long nonZeroBalanceCount = userCreditMapper.selectCount(new QueryWrapper<UserCredit>()
                .eq("credit_type_code", creditType.getTypeCode())
                .gt("balance", java.math.BigDecimal.ZERO));
        if (nonZeroBalanceCount != null && nonZeroBalanceCount > 0) {
            throw new ServiceException(ErrorCode.CREDIT_TYPE_DELETE_FORBIDDEN_HAS_BALANCE,
                    "存在 " + nonZeroBalanceCount + " 个非零余额账户，禁止删除该积分类型");
        }

        // 批量软删除该类型的用户积分账户
        int deletedAccounts = bulkSoftDeleteUserCreditsByTypeCode(creditType.getTypeCode());
        log.info("删除积分类型前软删除用户积分账户，类型编码: {}，删除账户数: {}", creditType.getTypeCode(), deletedAccounts);
        
        // 直接执行逻辑删除
        int result = creditTypeMapper.deleteById(id);
        if (result <= 0) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "积分类型删除失败");
        }
        
        log.info("积分类型删除成功，ID: {}, 编码: {}", creditType.getId(), creditType.getTypeCode());
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableCreditType(Long id) {
        log.info("启用积分类型，ID: {}", id);
        
        return updateCreditTypeStatus(id, true);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableCreditType(Long id) {
        log.info("禁用积分类型，ID: {}", id);
        
        return updateCreditTypeStatus(id, false);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCreditTypeStatus(Long id, boolean enabled) {
        if (id == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "积分类型ID不能为空");
        }
        
        // 检查积分类型是否存在
        CreditType creditType = getCreditTypeById(id);
        
        // 更新状态
        creditType.setStatus(enabled ? CreditType.STATUS_ENABLED : CreditType.STATUS_DISABLED);
        
        int result = creditTypeMapper.updateById(creditType);
        if (result <= 0) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "积分类型状态更新失败");
        }
        
        log.info("积分类型状态更新成功，ID: {}, 编码: {}, 状态: {}", 
                creditType.getId(), creditType.getTypeCode(), enabled ? "启用" : "禁用");
        
        // 若禁用类型，则联动禁用其所有使用场景
        if (!enabled) {
            int affected = creditUsageScenarioMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<CreditUsageScenario>()
                            .eq("credit_type_code", creditType.getTypeCode())
                            .eq("status", CreditUsageScenario.STATUS_ENABLED)
                            .set("status", CreditUsageScenario.STATUS_DISABLED));
            log.info("联动禁用使用场景，类型编码: {}，受影响场景数: {}", creditType.getTypeCode(), affected);
        }

        // 事务提交后刷新场景缓存，确保内存与数据库一致
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        creditScenarioManager.refreshCache();
                        log.info("已在事务提交后刷新积分场景缓存");
                    } catch (Exception e) {
                        log.warn("刷新积分场景缓存失败: {}", e.getMessage());
                    }
                }
            });
        } else {
            // 非事务环境，直接刷新
            try {
                creditScenarioManager.refreshCache();
            } catch (Exception e) {
                log.warn("刷新积分场景缓存失败: {}", e.getMessage());
            }
        }
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBatchSort(List<CreditType> creditTypes) {
        if (creditTypes == null || creditTypes.isEmpty()) {
            log.warn("批量更新排序请求为空");
            return 0;
        }
        
        log.info("批量更新积分类型排序，数量: {}", creditTypes.size());
        
        int count = 0;
        for (CreditType creditType : creditTypes) {
            if (creditType.getId() != null && creditType.getSortOrder() != null) {
                CreditType updateType = new CreditType();
                updateType.setId(creditType.getId());
                updateType.setSortOrder(creditType.getSortOrder());
                int result = creditTypeMapper.updateById(updateType);
                if (result > 0) {
                    count++;
                }
            }
        }
        
        log.info("积分类型排序更新完成，成功更新数量: {}", count);
        
        return count;
    }
    
    @Override
    public boolean existsByTypeCode(String typeCode, Long excludeId) {
        if (!StringUtils.hasText(typeCode)) {
            return false;
        }
        
        LambdaQueryWrapper<CreditType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditType::getTypeCode, typeCode);
        if (excludeId != null) {
            queryWrapper.ne(CreditType::getId, excludeId);
        }
        
        return creditTypeMapper.selectCount(queryWrapper) > 0;
    }
    
    @Override
    public List<CreditType> getTransferableCreditTypes() {
        log.debug("查询支持转账的积分类型");
        
        LambdaQueryWrapper<CreditType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CreditType::getIsTransferable, CreditType.TRANSFER_ENABLED);
        queryWrapper.eq(CreditType::getStatus, CreditType.STATUS_ENABLED);
        queryWrapper.orderByAsc(CreditType::getSortOrder);
        
        return creditTypeMapper.selectList(queryWrapper);
    }
    
    @Override
    public boolean isValidCreditType(Long id) {
        if (id == null) {
            return false;
        }
        
        try {
            CreditType creditType = creditTypeMapper.selectById(id);
            return creditType != null && creditType.isEnabled();
        } catch (Exception e) {
            log.warn("验证积分类型有效性时发生异常，ID: {}, 错误: {}", id, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean isValidCreditTypeCode(String typeCode) {
        if (!StringUtils.hasText(typeCode)) {
            return false;
        }
        
        try {
            LambdaQueryWrapper<CreditType> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CreditType::getTypeCode, typeCode);
            CreditType creditType = creditTypeMapper.selectOne(queryWrapper);
            return creditType != null && creditType.isEnabled();
        } catch (Exception e) {
            log.warn("验证积分类型编码有效性时发生异常，编码: {}, 错误: {}", typeCode, e.getMessage());
            return false;
        }
    }
    
    // ================ 私有方法 ================
    
    /**
     * 验证创建请求参数
     */
    private void validateCreateRequest(CreditTypeCreateRequest request) {
        if (request == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "创建请求不能为空");
        }
        
        if (!StringUtils.hasText(request.getTypeCode())) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "积分类型编码不能为空");
        }
        
        if (!StringUtils.hasText(request.getTypeName())) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "积分类型名称不能为空");
        }
        
        if (!StringUtils.hasText(request.getUnitName())) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "积分单位名称不能为空");
        }
        
        if (request.getDecimalPlaces() == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "小数位数不能为空");
        }
        
        if (request.getIsTransferable() == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "是否支持转账不能为空");
        }
        
        // 验证小数位数范围
        if (request.getDecimalPlaces() < 0 || request.getDecimalPlaces() > 4) {
            throw new ServiceException(ErrorCode.PARAM_OUT_OF_RANGE, "小数位数必须在0-4之间");
        }
        
        // 验证编码格式
        if (!request.getTypeCode().matches("^[A-Z_]+$")) {
            throw new ServiceException(ErrorCode.CREDIT_TYPE_CODE_INVALID, "积分类型编码只能包含大写字母和下划线");
        }
    }

    /**
     * 为所有启用用户初始化某积分类型账户（幂等）
     */
    private void initializeUserCreditsForType(String typeCode) {
        if (!StringUtils.hasText(typeCode)) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "积分类型编码不能为空");
        }
        log.info("为所有启用用户初始化积分账户，类型: {}", typeCode);
        
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getStatus, User.STATUS_ENABLED);
        List<User> users = userMapper.selectList(userQuery);
        if (users == null || users.isEmpty()) {
            return;
        }
        int success = 0;
        for (User user : users) {
            if (user == null || user.getId() == null) {
                continue;
            }
            boolean exists = userCreditMapper.existsByUserIdAndCreditType(user.getId(), typeCode);
            if (exists) {
                continue;
            }
            UserCredit userCredit = new UserCredit();
            userCredit.setUserId(user.getId());
            userCredit.setCreditTypeCode(typeCode);
            userCredit.setBalance(java.math.BigDecimal.ZERO);
            userCredit.setTotalEarned(java.math.BigDecimal.ZERO);
            userCredit.setTotalConsumed(java.math.BigDecimal.ZERO);
            userCredit.setVersion(0);
            try {
                int insert = userCreditMapper.insert(userCredit);
                if (insert > 0) {
                    success++;
                }
            } catch (Exception e) {
                log.warn("初始化用户积分账户失败/已存在，用户ID: {}，类型: {}，错误: {}", user.getId(), typeCode, e.getMessage());
            }
        }
        log.info("初始化完成，类型: {}，新增账户数: {} / {}", typeCode, success, users.size());
    }

    /**
     * 软删除指定类型的所有用户积分账户（直接执行逻辑删除）。
     */
    

    /**
     * 批量软删除指定类型的所有用户积分账户（单条 UPDATE 语句）。
     */
    private int bulkSoftDeleteUserCreditsByTypeCode(String creditTypeCode) {
        if (!StringUtils.hasText(creditTypeCode)) {
            return 0;
        }
        // 使用 UpdateWrapper 批量逻辑删除
        return userCreditMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<UserCredit>()
                .set("is_deleted", 1)
                .eq("credit_type_code", creditTypeCode)
                .eq("is_deleted", 0));
    }
} 
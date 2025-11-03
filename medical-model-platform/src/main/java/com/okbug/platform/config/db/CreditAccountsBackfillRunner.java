package com.okbug.platform.config.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.okbug.platform.entity.auth.User;
import com.okbug.platform.entity.credit.CreditType;
import com.okbug.platform.entity.credit.UserCredit;
import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.mapper.credit.CreditTypeMapper;
import com.okbug.platform.mapper.credit.UserCreditMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 启动回填：为已存在的用户初始化缺失的积分账户记录（幂等执行）。
 *
 * 规则：
 * 1) 仅针对启用状态的积分类型（credit_types.status = 1）
 * 2) 对于 users 中每个用户，如 user_credits 不存在对应 typeCode，则创建一条 0 余额记录
 * 3) 幂等：重复执行不会产生重复记录（依赖唯一索引和存在性检查）
 */
@Component
@Order(120)
public class CreditAccountsBackfillRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CreditAccountsBackfillRunner.class);

    
    private final UserMapper userMapper;
    private final CreditTypeMapper creditTypeMapper;
    private final UserCreditMapper userCreditMapper;

    public CreditAccountsBackfillRunner(UserMapper userMapper,
                                        CreditTypeMapper creditTypeMapper,
                                        UserCreditMapper userCreditMapper) {
        this.userMapper = userMapper;
        this.creditTypeMapper = creditTypeMapper;
        this.userCreditMapper = userCreditMapper;
    }

    @Override
    public void run(String... args) {
        try {
            List<CreditType> enabledTypes = creditTypeMapper.selectEnabledCreditTypes();
            if (enabledTypes == null || enabledTypes.isEmpty()) {
                logger.info("CreditAccountsBackfillRunner: no enabled credit types, skip.");
                return;
            }

            // 已移除团队积分账户回填

            // 回填用户积分账户
            List<User> users = userMapper.selectList(new QueryWrapper<>());
            int userCreated = 0;
            for (User user : users) {
                if (user == null || User.STATUS_DISABLED == user.getStatus()) {
                    continue;
                }
                Long userId = user.getId();
                for (CreditType type : enabledTypes) {
                    String typeCode = type.getTypeCode();
                    if (typeCode == null || typeCode.isEmpty()) {
                        continue;
                    }
                    boolean exists = userCreditMapper.selectCount(new QueryWrapper<UserCredit>()
                            .eq("user_id", userId)
                            .eq("credit_type_code", typeCode)) > 0;
                    if (!exists) {
                        UserCredit uc = new UserCredit();
                        uc.setUserId(userId);
                        uc.setCreditTypeCode(typeCode);
                        uc.setBalance(BigDecimal.ZERO);
                        uc.setTotalEarned(BigDecimal.ZERO);
                        uc.setTotalConsumed(BigDecimal.ZERO);
                        try {
                            userCreditMapper.insert(uc);
                            userCreated++;
                        } catch (Exception e) {
                            // 约束/并发导致的已存在等，忽略
                        }
                    }
                }
            }

            logger.info("CreditAccountsBackfillRunner completed: userAccountsCreated={}", userCreated);
        } catch (Exception e) {
            logger.warn("CreditAccountsBackfillRunner skipped due to error: {}", e.getMessage());
        }
    }
}



package com.okbug.platform.service.system.message;

import com.okbug.platform.entity.auth.User;
import com.okbug.platform.entity.team.Team;
import java.util.List;

/**
 * 站内消息编排门面：集中构造与发送各业务场景消息，避免散落到各处。
 */
public interface NotificationFacade {

    // 通用系统消息（立即发送）
    void sendSystemMessage(Long targetUserId, String title, String content, Long operatorUserId, String operatorUsername);

    // 通用系统消息（事件来源，保持与原 createFromEvent 语义一致）
    void sendSystemMessageFromEvent(Long targetUserId, String title, String content, Long operatorUserId);

    // 批量系统消息（事件来源）
    void sendSystemMessagesFromEvent(List<Long> targetUserIds, String title, String content, Long operatorUserId);

    // 积分类消息
    void sendCreditMessage(Long targetUserId, String title, String content);

    // ====== 积分类消息封装 ======
    void notifyCreditSpent(Long userId, String scenarioName, String creditTypeName, String unitName,
                           java.math.BigDecimal amount, String orderId,
                           java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter);

    void notifyCreditRewarded(Long targetUserId, String scenarioName, String creditTypeName, String unitName,
                              java.math.BigDecimal amount, Long relatedUserId, String relatedUserName,
                              java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter);

    void notifyCreditRefunded(Long userId, String orderId, String creditTypeName, String unitName,
                              java.math.BigDecimal amount, java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter);

    void notifyCreditTransferOut(Long fromUserId, String toDisplayName, String creditTypeName, String unitName,
                                 java.math.BigDecimal amount, java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter);

    void notifyCreditTransferIn(Long toUserId, String fromDisplayName, String creditTypeName, String unitName,
                                java.math.BigDecimal amount, java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter);

    void notifyCreditAllocatedOut(Long adminUserId, String targetDisplayName, String creditTypeName, String unitName,
                                  java.math.BigDecimal amount, String remark, java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter);

    void notifyCreditAllocatedIn(Long targetUserId, String adminDisplayName, String creditTypeName, String unitName,
                                 java.math.BigDecimal amount, String remark, java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter);

    void notifyRedeemCodeEarned(Long userId, String codeKey, String creditTypeName, String unitName,
                                java.math.BigDecimal amount, java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter);

    // 认证/注册相关编排
    void sendLoginSuccess(User user, String clientIp);
    void notifyReferrerOnRegister(User referrerUser, User newUser);
    void notifyTeamOwnerOnRegister(Team team, User newUser, boolean requireApproval);

    // 团队加入申请流程
    void notifyJoinRequestProcessed(Long applicantUserId, String teamName, boolean approved, String reason, Long operatorUserId);
    void notifyJoinRequestSubmittedToAdmin(Long targetAdminUserId, String teamName, String applicantDisplayName, Long applicantUserId);
    void notifyJoinRequestCancelledToAdmin(Long targetAdminUserId, String teamName, String applicantDisplayName, Long applicantUserId);

    // 团队/成员事件
    void notifyTeamCreated(Long ownerUserId, String teamName);
    void notifyTeamUpdated(Long targetUserId, String teamName, Long operatorUserId);
    void notifyMemberAdded(Long targetUserId, String teamName, String role, Long operatorUserId);
    void notifyMemberRemoved(Long targetUserId, String teamName, Long operatorUserId);
    void notifyOwnerPromoted(Long toUserId, String teamName, Long operatorUserId);
    void notifyOwnerTransferredNoticeOldOwner(Long fromOwnerUserId, String teamName, String newOwnerDisplayName, Long operatorUserId);
    void notifyTeamDissolved(Long targetUserId, String teamName, Long operatorUserId);
    void notifyMemberExitedToAdmin(Long targetAdminUserId, String teamName, String leaverDisplayName, Long leaverUserId);

    // 团队被超级管理员禁用时通知拥有者
    void notifyTeamDisabledToOwner(Long ownerUserId, String teamName, Long operatorUserId);

    // 团队被超级管理员重新启用时通知拥有者
    void notifyTeamEnabledToOwner(Long ownerUserId, String teamName, Long operatorUserId);
}



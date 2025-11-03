package com.okbug.platform.service.system.message.impl;

import com.okbug.platform.domain.notify.MessageType;
import com.okbug.platform.domain.event.events.MessageSendEvent;
import com.okbug.platform.domain.event.publisher.DomainEventPublisher;
import com.okbug.platform.entity.auth.User;
import com.okbug.platform.entity.team.Team;
import com.okbug.platform.service.system.message.NotificationFacade;
import org.springframework.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationFacadeImpl implements NotificationFacade {

    private final DomainEventPublisher domainEventPublisher;

	/**
	 * 发送通用「系统消息」。
	 *
	 * 适用场景：任意需要以系统身份向单个用户发送即时系统消息的业务场景。
	 *
	 * 行为描述：
	 * - 封装并转发到底层消息服务的 create（操作人为真实操作者身份）
	 * - 入参空用户ID时直接忽略返回
	 * - 出错仅记录 warn 日志，不影响主流程
	 *
	 * @param targetUserId  接收用户ID，必填；为空时忽略
	 * @param title         消息标题，可为空
	 * @param content       消息内容，建议非空
	 * @param operatorUserId 操作人用户ID，用于审计链路（可为空）
	 * @param operatorUsername 操作人用户名，用于审计链路（可为空）
	 */
	@Override
	public void sendSystemMessage(Long targetUserId, String title, String content, Long operatorUserId, String operatorUsername) {
		if (targetUserId == null) return;
		try {
			domainEventPublisher.publish(new MessageSendEvent(java.util.Collections.singletonList(targetUserId), title, content, operatorUserId, operatorUsername, MessageType.SYSTEM));
		} catch (Exception e) {
			log.warn("publish MessageSendEvent failed, targetUserId={}, title={}, err={}", targetUserId, title, e.getMessage());
		}
	}

	/**
	 * 发送通用「系统消息（事件来源版）」。
	 *
	 * 适用场景：从某个业务事件触发，沿用 createFromEvent 语义（无显式操作者用户名）。
	 *
	 * 行为描述：
	 * - 封装并转发到消息服务的 createFromEvent
	 * - 入参空用户ID时直接忽略返回
	 * - 出错仅记录 warn 日志
	 *
	 * @param targetUserId   接收用户ID，必填；为空时忽略
	 * @param title          消息标题
	 * @param content        消息内容
	 * @param operatorUserId 触发事件的用户ID（如审批人/申请人等），可为空
	 */
	@Override
    public void sendSystemMessageFromEvent(Long targetUserId, String title, String content, Long operatorUserId) {
        if (targetUserId == null) return;
        try {
            domainEventPublisher.publish(new MessageSendEvent(java.util.Collections.singletonList(targetUserId), title, content, operatorUserId));
        } catch (Exception e) {
            log.warn("publish MessageSendEvent failed, targetUserId={}, title={}, err={}", targetUserId, title, e.getMessage());
        }
    }

	/**
	 * 批量发送「系统消息（事件来源版）」。
	 *
	 * 适用场景：同一业务事件需要通知多个用户（如群体通知/团队管理员群发）。
	 *
	 * 行为描述：
	 * - 对目标ID列表进行空值/空集合校验后，逐个调用单发接口
	 * - 单个发送失败不影响其它用户的发送
	 *
	 * @param targetUserIds  接收用户ID列表，空或无元素将被忽略
	 * @param title          消息标题
	 * @param content        消息内容
	 * @param operatorUserId 触发事件的用户ID（可为空）
	 */
	@Override
    public void sendSystemMessagesFromEvent(List<Long> targetUserIds, String title, String content, Long operatorUserId) {
	if (targetUserIds == null || targetUserIds.isEmpty()) return;
	try {
		domainEventPublisher.publish(new MessageSendEvent(targetUserIds, title, content, operatorUserId, null, MessageType.SYSTEM));
	} catch (Exception e) {
		log.warn("publish batch MessageSendEvent failed, size={}, title={}, err={}", targetUserIds.size(), title, e.getMessage());
	}
    }

	/**
	 * 发送「积分类」消息。
	 *
	 * 适用场景：积分扣减/增加/转账等与积分相关的提醒。
	 *
	 * 行为描述：
	 * - 封装并转发到消息服务的 create（操作人使用固定 system）
	 * - 入参空用户ID时忽略
	 * - 出错仅记录 warn 日志
	 *
	 * @param targetUserId 接收用户ID，必填；为空时忽略
	 * @param title        消息标题
	 * @param content      消息内容（建议包含余额变动、场景等关键信息）
	 */
	@Override
	public void sendCreditMessage(Long targetUserId, String title, String content) {
	if (targetUserId == null) return;
	try {
		domainEventPublisher.publish(new MessageSendEvent(java.util.Collections.singletonList(targetUserId), title, content, 0L, "system", MessageType.CREDIT));
	} catch (Exception e) {
		log.warn("publish credit MessageSendEvent failed, targetUserId={}, title={}, err={}", targetUserId, title, e.getMessage());
	}
	}

    // ====== 积分类消息封装实现 ======
    @Override
    public void notifyCreditSpent(Long userId, String scenarioName, String creditTypeName, String unitName,
                                  java.math.BigDecimal amount, String orderId,
                                  java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter) {
        if (userId == null) return;
        String amountText = amount == null ? "" : amount.stripTrailingZeros().toPlainString();
        String unit = unitName == null ? "" : unitName;
        String type = creditTypeName == null ? "" : creditTypeName;
        String title = "积分消费提醒";
        String content = "您在【" + safe(scenarioName) + "】消费 " + amountText + " " + unit
                + "（" + type + "）"
                + (org.springframework.util.StringUtils.hasText(orderId) ? ("，订单号：" + orderId) : "")
                + "，余额：" + strip(balanceBefore) + " -> " + strip(balanceAfter) + "。";
        sendCreditMessage(userId, title, content);
    }

    @Override
    public void notifyCreditRewarded(Long targetUserId, String scenarioName, String creditTypeName, String unitName,
                                     java.math.BigDecimal amount, Long relatedUserId, String relatedUserName,
                                     java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter) {
        if (targetUserId == null) return;
        String amountText = amount == null ? "" : amount.stripTrailingZeros().toPlainString();
        String unit = unitName == null ? "" : unitName;
        String type = creditTypeName == null ? "" : creditTypeName;
        String title = "积分到账提醒";
        String content = "您的账户收到 " + amountText + " " + unit
                + "（" + type + "）"
                + "，来源：" + safe(scenarioName)
                + (relatedUserId != null ? ("，关联用户：" + safe(relatedUserName)) : "")
                + "，余额：" + strip(balanceBefore) + " -> " + strip(balanceAfter) + "。";
        sendCreditMessage(targetUserId, title, content);
    }

    @Override
    public void notifyCreditRefunded(Long userId, String orderId, String creditTypeName, String unitName,
                                     java.math.BigDecimal amount, java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter) {
        if (userId == null) return;
        String amountText = amount == null ? "" : amount.stripTrailingZeros().toPlainString();
        String unit = unitName == null ? "" : unitName;
        String type = creditTypeName == null ? "" : creditTypeName;
        String title = "积分退款提醒";
        String content = "订单号【" + safe(orderId) + "】退款 " + amountText + " " + unit
                + "（" + type + "）"
                + "，余额：" + strip(balanceBefore) + " -> " + strip(balanceAfter) + "。";
        sendCreditMessage(userId, title, content);
    }

    @Override
    public void notifyCreditTransferOut(Long fromUserId, String toDisplayName, String creditTypeName, String unitName,
                                        java.math.BigDecimal amount, java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter) {
        if (fromUserId == null) return;
        String amountText = amount == null ? "" : amount.stripTrailingZeros().toPlainString();
        String unit = unitName == null ? "" : unitName;
        String type = creditTypeName == null ? "" : creditTypeName;
        String title = "积分转账支出提醒";
        String content = "您向用户[" + safe(toDisplayName) + "] 转出 " + amountText + " " + unit
                + "（" + type + "）"
                + "，余额：" + strip(balanceBefore) + " -> " + strip(balanceAfter) + "。";
        sendCreditMessage(fromUserId, title, content);
    }

    @Override
    public void notifyCreditTransferIn(Long toUserId, String fromDisplayName, String creditTypeName, String unitName,
                                       java.math.BigDecimal amount, java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter) {
        if (toUserId == null) return;
        String amountText = amount == null ? "" : amount.stripTrailingZeros().toPlainString();
        String unit = unitName == null ? "" : unitName;
        String type = creditTypeName == null ? "" : creditTypeName;
        String title = "积分转账收入提醒";
        String content = "您收到来自用户[" + safe(fromDisplayName) + "] 的转入 " + amountText + " " + unit
                + "（" + type + "）"
                + "，余额：" + strip(balanceBefore) + " -> " + strip(balanceAfter) + "。";
        sendCreditMessage(toUserId, title, content);
    }

    @Override
    public void notifyCreditAllocatedOut(Long adminUserId, String targetDisplayName, String creditTypeName, String unitName,
                                         java.math.BigDecimal amount, String remark, java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter) {
        if (adminUserId == null) return;
        String amountText = amount == null ? "" : amount.stripTrailingZeros().toPlainString();
        String unit = unitName == null ? "" : unitName;
        String type = creditTypeName == null ? "" : creditTypeName;
        String title = "积分分配支出提醒";
        String content = "您向用户[" + safe(targetDisplayName) + "] 分配 " + amountText + " " + unit
                + "（" + type + "）"
                + "，余额：" + strip(balanceBefore) + " -> " + strip(balanceAfter)
                + (org.springframework.util.StringUtils.hasText(remark) ? ("，备注：" + remark) : "")
                + "。";
        sendCreditMessage(adminUserId, title, content);
    }

    @Override
    public void notifyCreditAllocatedIn(Long targetUserId, String adminDisplayName, String creditTypeName, String unitName,
                                        java.math.BigDecimal amount, String remark, java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter) {
        if (targetUserId == null) return;
        String amountText = amount == null ? "" : amount.stripTrailingZeros().toPlainString();
        String unit = unitName == null ? "" : unitName;
        String type = creditTypeName == null ? "" : creditTypeName;
        String title = "积分分配到账提醒";
        String content = "您收到来自管理员[" + safe(adminDisplayName) + "] 的分配 " + amountText + " " + unit
                + "（" + type + "）"
                + "，余额：" + strip(balanceBefore) + " -> " + strip(balanceAfter)
                + (org.springframework.util.StringUtils.hasText(remark) ? ("，备注：" + remark) : "")
                + "。";
        sendCreditMessage(targetUserId, title, content);
    }

    @Override
    public void notifyRedeemCodeEarned(Long userId, String codeKey, String creditTypeName, String unitName,
                                       java.math.BigDecimal amount, java.math.BigDecimal balanceBefore, java.math.BigDecimal balanceAfter) {
        if (userId == null) return;
        String amountText = amount == null ? "" : amount.stripTrailingZeros().toPlainString();
        String unit = unitName == null ? "" : unitName;
        String type = creditTypeName == null ? "" : creditTypeName;
        String title = "积分到账提醒";
        String content = "您兑换了兑换码【" + safe(codeKey) + "】, 获得 " + amountText + " " + unit
                + "（" + type + "）"
                + "，余额：" + strip(balanceBefore) + " -> " + strip(balanceAfter) + "。";
        sendCreditMessage(userId, title, content);
    }

    /**
     * 将 BigDecimal 去除无效小数后缀，转换为可读字符串；null 返回空串。
     */
    private String strip(java.math.BigDecimal v) { return v == null ? "" : v.stripTrailingZeros().toPlainString(); }

	/**
	 * 登录成功提示。
	 *
	 * 适用场景：用户完成登录后，进行安全性提示（含IP信息）。
	 *
	 * 行为描述：
	 * - 根据传入客户端IP动态拼接内容
	 * - 以当前用户为操作者发送一条系统消息
	 *
	 * @param user    登录用户，必填；为空或ID为空时忽略
	 * @param clientIp 客户端IP，可为空
	 */
	@Override
	public void sendLoginSuccess(User user, String clientIp) {
		if (user == null || user.getId() == null) return;
		String ip = clientIp != null ? clientIp : "";
		String content = "您的账号已成功登录" + (ip.isEmpty() ? "" : ("，IP：" + ip)) + "。若非本人操作，请及时修改密码。";
		sendSystemMessage(user.getId(), "登录成功", content, user.getId(), user.getUsername());
	}

	/**
	 * 引荐人注册成功通知。
	 *
	 * 适用场景：新用户填写引荐码注册，通知引荐人。
	 *
	 * 行为描述：
	 * - 自动格式化新用户展示名（昵称[用户名]）
	 * - 以新用户作为操作者，向引荐人发送系统消息
	 *
	 * @param referrerUser 引荐人用户，必填；为空或ID为空时忽略
	 * @param newUser      新注册用户，必填；为空时忽略
	 */
	@Override
	public void notifyReferrerOnRegister(User referrerUser, User newUser) {
		if (referrerUser == null || referrerUser.getId() == null || newUser == null) return;
		String content = "您引荐的用户 " + formatDisplayName(newUser) + " 已注册成功。";
		sendSystemMessage(referrerUser.getId(), "引荐成功通知", content, newUser.getId(), newUser.getUsername());
	}

	/**
	 * 团队拥有者注册通知：新用户注册与团队加入/申请提醒。
	 *
	 * 适用场景：新用户注册绑定团队，且可选择是否需要团队审核。
	 *
	 * 行为描述：
	 * - requireApproval=true：提示拥有者有新的加入申请
	 * - requireApproval=false：提示拥有者有新成员已加入
	 * - 以新用户为操作者发送系统消息
	 *
	 * @param team            目标团队，必填；为空或 ownerUserId 为空时忽略
	 * @param newUser         新注册用户，必填；为空时忽略
	 * @param requireApproval 是否需要团队审核
	 */
	@Override
	public void notifyTeamOwnerOnRegister(Team team, User newUser, boolean requireApproval) {
		if (team == null || team.getOwnerUserId() == null || newUser == null) return;
		String teamName = team.getTeamName() == null ? "" : team.getTeamName();
		if (requireApproval) {
			String content = "用户 " + formatDisplayName(newUser) + " 注册并申请加入您的团队 " + teamName + "。请在团队管理中处理申请。";
			sendSystemMessage(team.getOwnerUserId(), "团队加入申请", content, newUser.getId(), newUser.getUsername());
		} else {
			String content = "用户 " + formatDisplayName(newUser) + " 注册并已加入您的团队 " + teamName + "。";
			sendSystemMessage(team.getOwnerUserId(), "团队新成员加入", content, newUser.getId(), newUser.getUsername());
		}
	}

	/**
	 * 将用户展示名称格式化为「昵称[用户名]」。
	 *
	 * 规则：
	 * - 昵称存在且非空时使用「昵称[用户名]」
	 * - 否则仅返回用户名
	 *
	 * @param user 用户对象
	 * @return 可读展示名
	 */
    /**
     * 格式化用户展示名为「昵称[用户名]」。
     *
     * @param user 用户对象
     * @return 展示名
     */
    private String formatDisplayName(User user) {
		String nickname = user.getNickname();
		String username = user.getUsername();
		if (nickname == null || nickname.trim().isEmpty()) {
			return username;
		}
		return nickname + "[" + username + "]";
	}

	// ==================== 团队加入申请流程 ====================

	@Override
	public void notifyJoinRequestProcessed(Long applicantUserId, String teamName, boolean approved, String reason, Long operatorUserId) {
		if (applicantUserId == null) return;
		String title = approved ? "加入申请已通过" : "加入申请被拒绝";
		String content = approved ? ("您加入团队" + safe(teamName) + "的申请已通过。")
				: ("您加入团队" + safe(teamName) + "的申请被拒绝。原因：" + (StringUtils.hasText(reason) ? reason : "无"));
		sendSystemMessageFromEvent(applicantUserId, title, content, operatorUserId);
	}

	@Override
	public void notifyJoinRequestSubmittedToAdmin(Long targetAdminUserId, String teamName, String applicantDisplayName, Long applicantUserId) {
		if (targetAdminUserId == null) return;
		String title = "新的加入申请";
		String content = "团队" + safe(teamName) + " 收到来自用户[" + safe(applicantDisplayName) + "] 的加入申请。";
		sendSystemMessageFromEvent(targetAdminUserId, title, content, applicantUserId);
	}

	@Override
	public void notifyJoinRequestCancelledToAdmin(Long targetAdminUserId, String teamName, String applicantDisplayName, Long applicantUserId) {
		if (targetAdminUserId == null) return;
		String title = "加入申请已撤销";
		String content = "团队" + safe(teamName) + " 的加入申请已由申请人[" + safe(applicantDisplayName) + "] 撤销。";
		sendSystemMessageFromEvent(targetAdminUserId, title, content, applicantUserId);
	}

	// ==================== 团队/成员事件 ====================

	@Override
	public void notifyTeamCreated(Long ownerUserId, String teamName) {
		if (ownerUserId == null) return;
		sendSystemMessageFromEvent(ownerUserId, "团队创建成功", "您已创建团队：" + safe(teamName) + "，现在你可以在 [成员管理]、[加入申请] 页面管理您的团队。", ownerUserId);
	}

	@Override
	public void notifyTeamUpdated(Long targetUserId, String teamName, Long operatorUserId) {
		if (targetUserId == null) return;
		sendSystemMessageFromEvent(targetUserId, "团队信息已更新", "您所在的团队已更新信息：" + safe(teamName), operatorUserId);
	}

	@Override
	public void notifyMemberAdded(Long targetUserId, String teamName, String role, Long operatorUserId) {
		if (targetUserId == null) return;
		String content = "您已被加入团队：" + safe(teamName) + "，角色：" + (role == null ? "member" : role);
		sendSystemMessageFromEvent(targetUserId, "加入团队通知", content, operatorUserId);
	}

	@Override
	public void notifyMemberRemoved(Long targetUserId, String teamName, Long operatorUserId) {
		if (targetUserId == null) return;
		sendSystemMessageFromEvent(targetUserId, "移出团队通知", "您已被移出团队：" + safe(teamName), operatorUserId);
	}

	@Override
	public void notifyOwnerPromoted(Long toUserId, String teamName, Long operatorUserId) {
		if (toUserId == null) return;
		sendSystemMessageFromEvent(toUserId, "已成为团队拥有者", "您已成为团队" + safe(teamName) + "的拥有者，现在你可以在 [成员管理] [加入申请] 页面管理您的团队。", operatorUserId);
	}

	@Override
	public void notifyOwnerTransferredNoticeOldOwner(Long fromOwnerUserId, String teamName, String newOwnerDisplayName, Long operatorUserId) {
		if (fromOwnerUserId == null) return;
		String content = "您已将团队" + safe(teamName) + "的拥有者转移给用户[" + safe(newOwnerDisplayName) + "]";
		sendSystemMessageFromEvent(fromOwnerUserId, "拥有者已转移", content, operatorUserId);
	}

	@Override
	public void notifyTeamDissolved(Long targetUserId, String teamName, Long operatorUserId) {
		if (targetUserId == null) return;
		sendSystemMessageFromEvent(targetUserId, "团队已解散", "您所在的团队已解散：" + safe(teamName), operatorUserId);
	}

	@Override
	public void notifyMemberExitedToAdmin(Long targetAdminUserId, String teamName, String leaverDisplayName, Long leaverUserId) {
		if (targetAdminUserId == null) return;
		String title = "成员退出团队";
		String content = "用户[" + safe(leaverDisplayName) + "] 已退出团队" + safe(teamName);
		sendSystemMessageFromEvent(targetAdminUserId, title, content, leaverUserId);
	}

    /**
     * 空安全处理：null -> 空串。
     */
    private String safe(String v) {
		return v == null ? "" : v;
	}

    @Override
    public void notifyTeamDisabledToOwner(Long ownerUserId, String teamName, Long operatorUserId) {
        if (ownerUserId == null) return;
        String title = "团队已被禁用";
        String content = "您的团队" + safe(teamName) + "已被超级管理员禁用。如需恢复，请联系平台管理员。";
        sendSystemMessageFromEvent(ownerUserId, title, content, operatorUserId);
    }

    @Override
    public void notifyTeamEnabledToOwner(Long ownerUserId, String teamName, Long operatorUserId) {
        if (ownerUserId == null) return;
        String title = "团队已恢复正常";
        String content = "您的团队" + safe(teamName) + "已被超级管理员恢复启用，相关功能现已可用。";
        sendSystemMessageFromEvent(ownerUserId, title, content, operatorUserId);
    }
}



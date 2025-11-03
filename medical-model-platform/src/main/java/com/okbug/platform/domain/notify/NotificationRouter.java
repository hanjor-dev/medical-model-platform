package com.okbug.platform.domain.notify;

import com.okbug.platform.service.system.message.UserNotifyPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 通知路由：根据模板允许渠道 ∩ 用户偏好，决定最终推送渠道
 *
 * 说明：用户偏好采用新版两表（channel/type）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRouter {

    private final UserNotifyPreferenceService preferenceService;
    private final NotifyDictionary notifyDictionary;

    /**
     * 计算有效渠道（站内/邮件/短信）
     * @param userId 用户ID
     * @param allowedChannelsByTemplate 模板允许渠道，如 "inbox,email"
     * @param typeCode 消息类型编码（如 system/task/credit/marketing 等）
     */
    public Set<String> resolveEffectiveChannels(Long userId,
                                                String allowedChannelsByTemplate,
                                                String typeCode) {
        boolean typeEnabled = preferenceService.isTypeEnabled(userId, typeCode);
        if (!typeEnabled) {
            log.info("[Router] type disabled -> no channels");
            return Collections.emptySet();
        }
        Set<String> allowed = toSet(allowedChannelsByTemplate);
        Set<String> result = new HashSet<>();
        // 动态渠道集合中不存在该渠道时，也不启用
        Set<String> dynamicChannels = notifyDictionary.channelCodes();
        if (allowed.contains(MessageChannel.INBOX.code()) && dynamicChannels.contains(MessageChannel.INBOX.code()) && preferenceService.isChannelEnabled(userId, MessageChannel.INBOX.code())) {
            result.add(MessageChannel.INBOX.code());
        }
        if (allowed.contains(MessageChannel.EMAIL.code()) && dynamicChannels.contains(MessageChannel.EMAIL.code()) && preferenceService.isChannelEnabled(userId, MessageChannel.EMAIL.code())) {
            result.add(MessageChannel.EMAIL.code());
        }
        if (allowed.contains(MessageChannel.SMS.code()) && dynamicChannels.contains(MessageChannel.SMS.code()) && preferenceService.isChannelEnabled(userId, MessageChannel.SMS.code())) {
            result.add(MessageChannel.SMS.code());
        }
        return result;
    }

    private Set<String> toSet(String csv) {
        if (csv == null || csv.trim().isEmpty()) return Collections.emptySet();
        java.util.Set<String> set = new java.util.HashSet<>();
        for (String s : csv.split(",")) {
            if (s == null) continue;
            String v = s.trim().toLowerCase();
            if (!v.isEmpty()) set.add(v);
        }
        return set;
    }
}



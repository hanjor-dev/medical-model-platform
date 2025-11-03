package com.okbug.platform.service.system.message.dispatcher.impl;

import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.domain.notify.NotificationRouter;
import com.okbug.platform.domain.notify.NotifyDictionary;
import com.okbug.platform.entity.system.message.Message;
import com.okbug.platform.service.system.message.dispatcher.MessageDispatcher;
import com.okbug.platform.service.system.message.sender.EmailSenderService;
import com.okbug.platform.ws.RealtimeWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import com.okbug.platform.domain.notify.MessageChannel;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultMessageDispatcher implements MessageDispatcher {

    private final NotificationRouter notificationRouter;
    private final NotifyDictionary notifyDictionary;
    private final RealtimeWebSocketHandler websocketHandler;
    private final EmailSenderService emailSenderService;

    @Override
    /**
     * 按用户偏好与模板可用渠道，分发单条消息到各渠道。
     *
     * @param message 待分发消息
     * @return 是否至少有一个渠道分发成功
     * @throws ServiceException 当 message 或 userId 缺失时抛出
     */
    public boolean dispatch(Message message) {
        if (message == null || message.getUserId() == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "消息或用户不能为空");
        }

        String typeCode = message.getMessageType();

        // 模板允许的渠道：动态从系统字典加载（DICT_4.1）
        String allowedChannels = notifyDictionary.channelCodesCsv();
        Set<String> effective = notificationRouter.resolveEffectiveChannels(
                message.getUserId(), allowedChannels, typeCode
        );
        if (effective == null || effective.isEmpty()) {
            // 不抛异常，避免影响业务流程；仅记录日志并忽略
            log.info("[Dispatcher] 无可用渠道，已忽略: userId={}, messageId={}, type={}", message.getUserId(), message.getId(), typeCode);
            return true; // 视为已处理，避免标记失败
        }

        boolean anySuccess = false;
        for (String channel : effective) {
            try {
                MessageChannel ch = MessageChannel.fromCode(channel);
                if (ch == null) {
                    log.warn("[Dispatcher] 未识别渠道: {}", channel);
                    continue;
                }
                switch (ch) {
                    case INBOX:
                        // 站内：仅通知前端刷新，由前端再拉取列表
                        websocketHandler.sendNewMessageNotification(message.getUserId());
                        log.info("[Dispatcher] inbox 推送成功: userId={}, messageId={}", message.getUserId(), message.getId());
                        anySuccess = true;
                        break;
                    case EMAIL:
                        // 邮件发送
                        try {
                            emailSenderService.sendMessageEmail(message);
                            log.info("[Dispatcher] email 发送成功: userId={}, messageId={}", message.getUserId(), message.getId());
                            anySuccess = true;
                        } catch (Exception e) {
                            log.warn("[Dispatcher] email 发送失败: userId={}, messageId={}, error={}", message.getUserId(), message.getId(), e.getMessage());
                        }
                        break;
                    case SMS:
                        // 短信模拟：日志输出
                        log.info("[Dispatcher] sms 模拟发送: userId={}, content={}", message.getUserId(), message.getContent());
                        anySuccess = true;
                        break;
                }
            } catch (Exception e) {
                log.warn("[Dispatcher] 渠道推送失败: channel={}, userId={}, messageId={}, error={}", channel, message.getUserId(), message.getId(), e.getMessage());
            }
        }

        return anySuccess;
    }
}



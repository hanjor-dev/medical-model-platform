package com.okbug.platform.domain.event.events;

import com.okbug.platform.domain.event.DomainEvent;
import com.okbug.platform.domain.notify.MessageType;

import java.util.Collections;
import java.util.List;

/**
 * 事件：批量发送站内消息（提交后异步处理）
 */
public class MessageSendEvent implements DomainEvent {

    private final List<Long> targetUserIds;
    private final String title;
    private final String content;
    private final Long operatorUserId;
    private final String operatorUsername;
    private final MessageType messageType;

    public MessageSendEvent(List<Long> targetUserIds, String title, String content, Long operatorUserId) {
        this.targetUserIds = targetUserIds == null ? Collections.emptyList() : targetUserIds;
        this.title = title;
        this.content = content;
        this.operatorUserId = operatorUserId;
        this.operatorUsername = null;
        this.messageType = MessageType.SYSTEM;
    }

    public MessageSendEvent(List<Long> targetUserIds, String title, String content,
                            Long operatorUserId, String operatorUsername, MessageType messageType) {
        this.targetUserIds = targetUserIds == null ? Collections.emptyList() : targetUserIds;
        this.title = title;
        this.content = content;
        this.operatorUserId = operatorUserId;
        this.operatorUsername = operatorUsername;
        this.messageType = messageType == null ? MessageType.SYSTEM : messageType;
    }

    public List<Long> getTargetUserIds() {
        return targetUserIds;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Long getOperatorUserId() {
        return operatorUserId;
    }

    public String getOperatorUsername() {
        return operatorUsername;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public String getEventKey() {
        int size = targetUserIds == null ? 0 : targetUserIds.size();
        long firstId = 0L;
        if (targetUserIds != null && !targetUserIds.isEmpty()) {
            Long v = targetUserIds.get(0);
            firstId = v == null ? 0L : v;
        }
        return "MESSAGE_SEND:" + firstId + ":" + size + ":" + (operatorUserId == null ? 0L : operatorUserId) + ":" + (messageType == null ? "SYSTEM" : messageType.name());
    }
}



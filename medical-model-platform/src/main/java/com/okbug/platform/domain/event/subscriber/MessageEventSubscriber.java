package com.okbug.platform.domain.event.subscriber;

import com.okbug.platform.domain.event.events.MessageSendEvent;
import com.okbug.platform.dto.system.message.request.MessageCreateRequest;
import com.okbug.platform.domain.notify.MessageType;
import com.okbug.platform.service.system.message.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventSubscriber {

    private final MessageService messageService;

    /**
     * 提交后异步分片发送站内消息
     */
    @Async("notificationAsyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMessageSend(MessageSendEvent event) {
        List<Long> userIds = event.getTargetUserIds();
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        final int batchSize = 500;
        int from = 0;
        while (from < userIds.size()) {
            int to = Math.min(from + batchSize, userIds.size());
            List<Long> batch = new ArrayList<>(userIds.subList(from, to));
            sendBatch(batch, event);
            from = to;
        }
    }

    private void sendBatch(List<Long> batchUserIds, MessageSendEvent event) {
        for (Long uid : batchUserIds) {
            if (uid == null) continue;
            try {
                MessageCreateRequest req = new MessageCreateRequest();
                req.setUserId(uid);
                req.setMessageType(event.getMessageType() == null ? MessageType.SYSTEM : event.getMessageType());
                req.setTitle(event.getTitle());
                req.setContent(event.getContent());
                messageService.createFromEvent(req, event.getOperatorUserId(), event.getOperatorUsername());
            } catch (Exception e) {
                log.warn("async message send failed, uid={}, title={}, err={}", uid, event.getTitle(), e.getMessage());
            }
        }
    }
}



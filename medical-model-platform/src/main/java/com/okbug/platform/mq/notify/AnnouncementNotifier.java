package com.okbug.platform.mq.notify;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.okbug.platform.config.RabbitAnnouncementConfig.EXCHANGE_DELAY;
import static com.okbug.platform.config.RabbitAnnouncementConfig.RK_DELAY;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnnouncementNotifier {

    private final RabbitTemplate rabbitTemplate;

    public void scheduleFirstPush(Long id, String title, LocalDateTime activeFrom) {
        LocalDateTime now = LocalDateTime.now();
        long delayMs = Math.max(0, Duration.between(now, activeFrom).toMillis());
        Map<String, Object> payload = new HashMap<>();
        payload.put("announcementId", id);
        payload.put("title", title);
        payload.put("activeFrom", activeFrom.toString());
        rabbitTemplate.convertAndSend(EXCHANGE_DELAY, RK_DELAY, payload, message -> {
            message.getMessageProperties().setExpiration(String.valueOf(delayMs));
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
        log.info("[Announcement] 延时推送入队: id={}, delayMs={}", id, delayMs);
    }
}



package com.okbug.platform.mq.notify;

import com.okbug.platform.entity.system.message.Announcement;
import com.okbug.platform.mapper.system.message.AnnouncementMapper;
import com.okbug.platform.ws.RealtimeWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

import static com.okbug.platform.config.RabbitAnnouncementConfig.QUEUE_READY;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnnouncementReadyConsumer {

    private final AnnouncementMapper announcementMapper;
    private final RealtimeWebSocketHandler permissionSocketHandler;

    @RabbitListener(queues = QUEUE_READY)
    public void onReady(@Payload Map<String, Object> body) {
        try {
            if (body == null || !body.containsKey("announcementId")) {
                return;
            }
            Long id = ((Number) body.get("announcementId")).longValue();
            Announcement a = announcementMapper.selectById(id);
            if (a == null) return;

            LocalDateTime now = LocalDateTime.now();
            // 状态必须为已发布
            if (a.getStatus() == null || a.getStatus() != 1) return;
            // 生效窗口校验
            if (a.getActiveFrom() != null && a.getActiveFrom().isAfter(now)) return;
            if (a.getActiveTo() != null && a.getActiveTo().isBefore(now)) return;
            // 幂等：仅第一次推送
            if (a.getNotified() != null && a.getNotified() == 1) return;

            permissionSocketHandler.sendNewAnnouncementNotification(0L, a.getId(), a.getTitle(), a.getContent());
            log.info("[Announcement] 到点推送: id={}, title={}", a.getId(), a.getTitle());

            Announcement upd = new Announcement();
            upd.setId(a.getId());
            upd.setNotified(1);
            upd.setFirstPushTime(now);
            upd.setUpdateTime(now);
            announcementMapper.updateById(upd);
        } catch (Exception e) {
            log.error("[Announcement] 到点推送处理失败", e);
        }
    }
}


